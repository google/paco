package com.google.sampling.experiential.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.Channels;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.datastore.EventServerColumns;
import com.pacoapp.paco.shared.util.ErrorMessages;

public class CloudSQLReportsDaoImpl implements CloudSQLReportsDao {
  public static final Logger log = Logger.getLogger(CloudSQLReportsDaoImpl.class.getName());
  private static final String GET_PARTICIPANTS_QUERY = "select who from expwho where " + EventServerColumns.EXPERIMENT_ID+ " =?";
  
  @Override
  public List<String> getParticipants(Long expId, String who) throws SQLException {
    List<String> userLst = null;
    Connection conn = null;
    PreparedStatement statementSelectParticipants = null;
    try {
      if (who == null) {
        conn = CloudSQLConnectionManager.getInstance().getConnection();
        setNames(conn);
        statementSelectParticipants = conn.prepareStatement(GET_PARTICIPANTS_QUERY);
        statementSelectParticipants.setLong(1, expId);
        ResultSet rs = statementSelectParticipants.executeQuery();
        userLst = Lists.newArrayList();
        while(rs.next()){
          userLst.add(rs.getString(EventServerColumns.WHO));
        }
      } else {
        userLst = Lists.newArrayList(who);
      }
    } finally {
      try {
        if (statementSelectParticipants != null) {
          statementSelectParticipants.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
      
    return userLst;
  }
  
  public boolean setNames(Connection conn) throws SQLException { 
    boolean isDone = false;
    java.sql.Statement statementSetNames = null;
    try {
      statementSetNames = conn.createStatement();
      final String setNamesSql = "SET NAMES  'utf8mb4'";
      statementSetNames.execute(setNamesSql);
      isDone = true;
    } finally {
      try {
        if (statementSetNames != null) {
          statementSetNames.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
      }
    }
    return isDone;
  }
  
  @Override
  public String storeCompleteStatusInCloudStorage(String jobId, Long experimentId, String who) throws SQLException, JSONException, FileNotFoundException, IOException {
    Connection conn = CloudSQLConnectionManager.getInstance().getConnection();
    List<String> participantsLst = null;
    BlobKey blobKey = null;
    PreparedStatement pStmt = null;
    int partCt = 1;
    String query = "select experiment_id, who, text,count(0) noOfRecords from events e join outputs o on e._id = o.event_id " + 
                   " where experiment_id=? and who=? group by experiment_id, who, text";
    try {
      participantsLst = getParticipants(experimentId, who);
      GcsOutputChannel writeChannel = csfw.getCSWriterChannel(jobId, "application/json", "project-private", jobId);
      PrintWriter writer = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));
      pStmt = conn.prepareStatement(query);
      for(String eachParticipant : participantsLst) {
        pStmt.setLong(1, experimentId);
        pStmt.setString(2, eachParticipant);
        log.info("CompleteStatus:" + pStmt.toString());
        ResultSet rs = pStmt.executeQuery();
        // traverse result set and create response json object
        rs.beforeFirst();
        JSONObject eachRecord = new JSONObject();
        eachRecord.put("participantCount", partCt++);
        eachRecord.put("participantEmail", eachParticipant);
        while (rs.next()) {
          String colName = rs.getString("text");
          String noOfRecords = rs.getString("noOfRecords");
          eachRecord.put(colName, noOfRecords);
        }// while loop
        //to cloud storage
        writer.println(eachRecord);
        writer.flush();
        writeChannel.waitForOutstandingWrites();
      }// for loop
     
      writeChannel.close();
      blobKey = blobstoreService.createGsBlobKey("/gs/" + bucketName + "/" + jobId);
      log.info("JSON data - complete status finished");
    } finally {
      try {
        if (pStmt != null) {
          pStmt.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
    return blobKey.getKeyString();
  }

  @Override
  public String storeQuickStatusInCloudStorage(String jobId, Long experimentId, String who) throws SQLException, JSONException,
                                                                            FileNotFoundException, IOException {
    Connection conn = null;
    BlobKey blobKey = null;
    List<String> participantsLst = null;
    int partCt = 1;
    PreparedStatement pStmt = null;
    String query = "SELECT who,count(case when text in ('apps_used', 'apps_used_raw') then 1 end) AS appusage, " +
                   " count(case when text in ('joined') then 1 end) AS joined, " +
                   " count(case when text not in ('apps_used', 'apps_used_raw','joined') then 1 end) as esm " + 
                   " from events e " +
                   " join outputs o on e._id = o.event_id where experiment_id=? and who =? group by who";
    
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      participantsLst = getParticipants(experimentId, who);
      GcsOutputChannel writeChannel = csfw.getCSWriterChannel(jobId, "application/json", "project-private", jobId);
      PrintWriter writer = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));
      
      pStmt = conn.prepareStatement(query);
      for(String eachParticipant : participantsLst) {
        pStmt.setLong(1, experimentId);
        pStmt.setString(2, eachParticipant);
        log.info("QuickStatus:" + pStmt.toString());
        ResultSet rs = pStmt.executeQuery();
        // traverse result set and create response json object
        rs.beforeFirst();
        JSONObject eachRecord = new JSONObject();
        while (rs.next()) {
          eachRecord.put("participantCount", partCt++);
          eachRecord.put("participantEmail", rs.getString("who"));
          eachRecord.put("appUsage", rs.getString("appusage"));
          eachRecord.put("joined", rs.getString("joined"));
          eachRecord.put("esm", rs.getString("esm"));
        }// while loop
        //to cloud storage
        writer.println(eachRecord);
        writer.flush();
        writeChannel.waitForOutstandingWrites();
        
      }// for loop
      writeChannel.close();
      blobKey = blobstoreService.createGsBlobKey("/gs/" + bucketName + "/" + jobId);
      log.info("JSON data - quick status finished");
    } finally {
      try {
        if (pStmt != null) {
          pStmt.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
    return blobKey.getKeyString();
  }
  
  @Override
  public String storeQuickStatusBySPInCloudStorage(String jobId, Long expId, String who) throws SQLException, JSONException, FileNotFoundException, IOException {
    ResultSetMetaData rsmd = null;
    CallableStatement cStmt = null;
    BlobKey blobKey = null;
    List<String> participantsLst = null;
    Connection conn = CloudSQLConnectionManager.getInstance().getConnection();
    CloudSQLReportsDaoImpl daoImpl = new CloudSQLReportsDaoImpl();
    int ctr = 0;
    int colCount = 0;
   
    Long startTime = System.currentTimeMillis();
    participantsLst = daoImpl.getParticipants(expId, who);
    GcsOutputChannel writeChannel = csfw.getCSWriterChannel(jobId, "application/json", "project-private", jobId);
    PrintWriter writer = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));
        
    try {
      cStmt = conn.prepareCall("call ExpQuickStatus(?,?)");
      for(String eachParticipant : participantsLst) {
        cStmt.setLong(1, expId);
        cStmt.setString(2, eachParticipant);
        ResultSet rs = cStmt.executeQuery();
        if(ctr++ == 0) {
          rsmd = rs.getMetaData();
          colCount = rsmd.getColumnCount();
        }
        // traverse result set and create response json object
        rs.beforeFirst();
        while (rs.next()) {
          JSONObject eachRecord = new JSONObject();
          for (int i=0; i<colCount; i++) {
            String colName = rsmd.getColumnName(i+1);
            switch (rsmd.getColumnType(i+1)) {
              case java.sql.Types.BIGINT:
                eachRecord.put(colName, rs.getBigDecimal(colName));
                break;
              case java.sql.Types.INTEGER:
                eachRecord.put(colName, rs.getInt(colName));
                break;
              case java.sql.Types.VARCHAR:
                eachRecord.put(colName, rs.getString(colName));
                break;
              case java.sql.Types.DATE:
                eachRecord.put(colName, rs.getDate(colName));
                break;
              case java.sql.Types.TINYINT:
                eachRecord.put(colName, rs.getShort(colName));
                break;
              default: 
                eachRecord.put(colName, rs.getObject(colName));
            }
          }// for loop
          //to cloud storage
          writer.println(eachRecord);
          writer.flush();
          writeChannel.waitForOutstandingWrites();
        }// while loop
        
        long diff = startTime - System.currentTimeMillis();
        log.info(ctr+ "quick status took"+ diff);
      }
    } finally {
      try {
        if (cStmt != null) {
          cStmt.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
      log.info("stoptime"+ System.currentTimeMillis());
    }
    writeChannel.close();
    blobKey = blobstoreService.createGsBlobKey("/gs/" + bucketName + "/" + jobId);
    return blobKey.getKeyString();
  }
  
  @Override
  public String storeCompleteStatusBySPInCloudStorage(String jobId, Long expId, String who) throws SQLException, JSONException, FileNotFoundException, IOException {
    BlobKey blobKey = null;
    List<String> participantsLst = null;
    Connection conn = CloudSQLConnectionManager.getInstance().getConnection();
    CloudSQLReportsDaoImpl daoImpl = new CloudSQLReportsDaoImpl();
    participantsLst = daoImpl.getParticipants(expId, who);
    log.info("participant list size"+ participantsLst.size());
    
    GcsOutputChannel writeChannel = csfw.getCSWriterChannel(jobId, "application/json", "project-private", jobId);
    PrintWriter writer = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));
    
    CallableStatement cStmt = conn.prepareCall("call ExpCompleteStatus(?,?)");
    for(String eachPart : participantsLst) {
      cStmt.setLong(1, expId);
      cStmt.setString(2, eachPart);
      log.info(cStmt.toString());
      ResultSet rs = cStmt.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();
      int colCount = rsmd.getColumnCount();
     
      // traverse result set and create response json object
      rs.beforeFirst();
      while (rs.next()) {
        JSONObject eachUserRecord = new JSONObject();
        for (int i=0; i<colCount; i++) {
          String colName = rsmd.getColumnName(i+1);
          int colType = rsmd.getColumnType(i+1);
          switch (colType) {
            case java.sql.Types.BIGINT:
              eachUserRecord.put(colName, rs.getBigDecimal(colName));
              break;
            case java.sql.Types.INTEGER:
              eachUserRecord.put(colName, rs.getInt(colName));
              break;
            case java.sql.Types.VARCHAR:
              eachUserRecord.put(colName, rs.getString(colName));
              break;
            case java.sql.Types.LONGVARCHAR:
              eachUserRecord.put(colName, rs.getString(colName));
              break;
            case java.sql.Types.DATE:
              eachUserRecord.put(colName, rs.getDate(colName));
              break;
            case java.sql.Types.TINYINT:
              eachUserRecord.put(colName, rs.getShort(colName));
              break;
            default: 
              Object obj =  rs.getObject(colName);
              eachUserRecord.put(colName,  obj!=null?obj.toString():"");
          }//switch
        }//col count forloop
        //to cloud storage
        writer.println(eachUserRecord);
        writer.flush();
        writeChannel.waitForOutstandingWrites();
      }//while
    }
    writeChannel.close();
    blobKey = blobstoreService.createGsBlobKey("/gs/" + bucketName + "/" + jobId);
    log.info("JSON data - complete status finished");
    return blobKey.getKeyString();
  }

  
}
