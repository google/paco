package com.google.sampling.experiential.server.reports;

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
import com.google.sampling.experiential.cloudsql.columns.EventServerColumns;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.ExperimentAccessManager;
import com.google.sampling.experiential.server.QueryConstants;
import com.pacoapp.paco.shared.model2.OutputBaseColumns;
import com.pacoapp.paco.shared.util.ErrorMessages;

public class CloudSQLReportsDaoImpl implements CloudSQLReportsDao {
  public static final Logger log = Logger.getLogger(CloudSQLReportsDaoImpl.class.getName());
  private static final String PARTICIPANT_COUNT = "participantCount";
  private static final String PARTICIPANT_EMAIL = "participantEmail";
  private static final String NUMBER_OF_RECORDS = "noOfRecords";
  private static final String APP_USAGE = "appusage";
  private static final String ESM = "esm";
  
  @Override
  public List<String> getACLedParticipants(Long expId, String who) throws SQLException {
    List<String> userLst = null;
    Connection conn = null;
    PreparedStatement statementSelectParticipants = null;
    try {
      Boolean isAdmin = ExperimentAccessManager.isUserAdmin(expId, who);
      // if he is an admin, we will give data for all participants in this expt
      // if not we will just show his data. If he is not valid participant, he will not get any data
      if (isAdmin) {
        conn = CloudSQLConnectionManager.getInstance().getConnection();
        setNames(conn);
        statementSelectParticipants = conn.prepareStatement(QueryConstants.GET_PARTICIPANTS_QUERY.toString());
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
      statementSetNames.execute(QueryConstants.SET_NAMES.toString());
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
   
    try {
      participantsLst = getACLedParticipants(experimentId, who);
      GcsOutputChannel writeChannel = csfw.getCSWriterChannel(jobId, "application/json", "project-private", jobId);
      PrintWriter writer = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));
      pStmt = conn.prepareStatement(QueryConstants.GET_COMPLETE_STATUS.toString());
      for(String eachParticipant : participantsLst) {
        pStmt.setLong(1, experimentId);
        pStmt.setString(2, eachParticipant);
        log.info("CompleteStatus:" + pStmt.toString());
        ResultSet rs = pStmt.executeQuery();
        // traverse result set and create response json object
        rs.beforeFirst();
        JSONObject eachRecord = new JSONObject();
        eachRecord.put(PARTICIPANT_COUNT, partCt++);
        eachRecord.put(PARTICIPANT_EMAIL, eachParticipant);
        while (rs.next()) {
          String colName = rs.getString(OutputBaseColumns.NAME);
          String noOfRecords = rs.getString(NUMBER_OF_RECORDS);
          eachRecord.put(colName, noOfRecords);
        }// while loop
        //to cloud storage
        writer.println(eachRecord);
        writer.flush();
        writeChannel.waitForOutstandingWrites();
      }// for loop
     
      writeChannel.close();
      blobKey = csfw.getBlobKey(blobstoreService, jobId);
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
    
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      participantsLst = getACLedParticipants(experimentId, who);
      GcsOutputChannel writeChannel = csfw.getCSWriterChannel(jobId, "application/json", "project-private", jobId);
      PrintWriter writer = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));
      pStmt = conn.prepareStatement(QueryConstants.GET_QUICK_STATUS.toString());
      for(String eachParticipant : participantsLst) {
        pStmt.setLong(1, experimentId);
        pStmt.setString(2, eachParticipant);
        log.info("QuickStatus:" + pStmt.toString());
        ResultSet rs = pStmt.executeQuery();
        // traverse result set and create response json object
        rs.beforeFirst();
        JSONObject eachRecord = new JSONObject();
        while (rs.next()) {
          eachRecord.put(PARTICIPANT_COUNT, partCt++);
          eachRecord.put(PARTICIPANT_EMAIL, rs.getString(EventServerColumns.WHO));
          eachRecord.put(APP_USAGE, rs.getString(APP_USAGE));
          eachRecord.put(EventServerColumns.JOINED, rs.getString(EventServerColumns.JOINED));
          eachRecord.put(ESM, rs.getString(ESM));
        }// while loop
        //to cloud storage
        writer.println(eachRecord);
        writer.flush();
        writeChannel.waitForOutstandingWrites();
        
      }// for loop
      writeChannel.close();
      blobKey = csfw.getBlobKey(blobstoreService, jobId);
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
  public String storeStatusByStoredProcInCloudStorage(String statusType, String jobId, Long expId, String who) throws SQLException, JSONException, FileNotFoundException, IOException {
    ResultSetMetaData rsmd = null;
    CallableStatement cStmt = null;
    BlobKey blobKey = null;
    List<String> participantsLst = null;
    Connection conn = CloudSQLConnectionManager.getInstance().getConnection();
    CloudSQLReportsDaoImpl daoImpl = new CloudSQLReportsDaoImpl();
    int ctr = 0;
    int colCount = 0;
   
    Long startTime = System.currentTimeMillis();
    participantsLst = daoImpl.getACLedParticipants(expId, who);
    GcsOutputChannel writeChannel = csfw.getCSWriterChannel(jobId, "application/json", "project-private", jobId);
    PrintWriter writer = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));
        
    try {
      if (statusType != null && statusType.equalsIgnoreCase(ReportJob.COMPLETE)) {
        cStmt = conn.prepareCall(QueryConstants.GET_COMPLETE_STATUS_STORED_PROC.toString());
      } else if (statusType != null && statusType.equalsIgnoreCase(ReportJob.QUICK))  {
        cStmt = conn.prepareCall(QueryConstants.GET_QUICK_STATUS_STORED_PROC.toString());
      }
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
    blobKey = csfw.getBlobKey(blobstoreService, jobId);
    return blobKey.getKeyString();
  }
}
