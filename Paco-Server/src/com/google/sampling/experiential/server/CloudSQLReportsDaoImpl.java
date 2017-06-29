package com.google.sampling.experiential.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.datastore.EventServerColumns;
import com.pacoapp.paco.shared.util.ErrorMessages;

public class CloudSQLReportsDaoImpl implements CloudSQLReportsDao {
  public static final Logger log = Logger.getLogger(CloudSQLReportsDaoImpl.class.getName());
  
  private static final String GET_PARTICIPANTS_QUERY = "select who from expwho where " + EventServerColumns.EXPERIMENT_ID+ " =?";
  
  
  @Override
  public List<String> getParticipants(Long expId) throws SQLException {
    List<String> userLst = Lists.newArrayList();
    Connection conn = null;
    PreparedStatement statementSelectParticipants = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      setNames(conn);
      statementSelectParticipants = conn.prepareStatement(GET_PARTICIPANTS_QUERY);
      statementSelectParticipants.setLong(1, expId);
      ResultSet rs = statementSelectParticipants.executeQuery();
      while(rs.next()){
        userLst.add(rs.getString(EventServerColumns.WHO));
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
  public String getCompleteStatus(String jobId, Long experimentId, String who) throws SQLException, JSONException, FileNotFoundException, IOException {
    String key = null;
    JSONBlobWriter blobWriter = new JSONBlobWriter();
    Connection conn = CloudSQLConnectionManager.getInstance().getConnection();
    JSONArray multipleRecords = new JSONArray();
    List<String> participantsLst = null;
    int partCt = 1;
    PreparedStatement pStmt = null;
    String query = "select experiment_id, who, text,count(0) noOfRecords from events e join outputs o on e._id = o.event_id " + 
                   " where experiment_id=? and who=? group by experiment_id, who, text";
    try {
      if (who == null) {
        participantsLst = getParticipants(experimentId);
      } else {
        participantsLst = Lists.newArrayList(who);
      }
      
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
        multipleRecords.put(eachRecord);
      }// for loop
      key = blobWriter.writeBlobUsingNewApi(jobId, multipleRecords.toString()).getKeyString();
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
    return key;
  }

  @Override
  public String getQuickStatus(String jobId, Long experimentId, String who) throws SQLException, JSONException,
                                                                            FileNotFoundException, IOException {
    String key = null;
    JSONBlobWriter blobWriter = new JSONBlobWriter();
    Connection conn = CloudSQLConnectionManager.getInstance().getConnection();
    JSONArray multipleRecords = new JSONArray();
    List<String> participantsLst = null;
    int partCt = 1;
    PreparedStatement pStmt = null;
    String query = "SELECT who,count(case when text in ('apps_used', 'apps_used_raw') then 1 end) AS appusage, " +
                   " count(case when text in ('joined') then 1 end) AS joined, " +
                   " count(case when text not in ('apps_used', 'apps_used_raw','joined') then 1 end) as esm " + 
                   " from events e " +
                   " join outputs o on e._id = o.event_id where experiment_id=? and who =? group by who";
    
    try {
      if (who == null) {
        participantsLst = getParticipants(experimentId);
      } else {
        participantsLst = Lists.newArrayList(who);
      }
      
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
        multipleRecords.put(eachRecord);
      }// for loop
      key = blobWriter.writeBlobUsingNewApi(jobId, multipleRecords.toString()).getKeyString();
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
    return key;
  }
  
}
