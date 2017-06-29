package com.google.sampling.experiential.server.migration.jobs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.CloudSQLReportsDaoImpl;
import com.google.sampling.experiential.server.JSONBlobWriter;
import com.google.sampling.experiential.server.migration.StoredProc;
import com.pacoapp.paco.shared.util.ErrorMessages;

public class ExperimentQuickStatusSP implements StoredProc {
    public static final Logger log = Logger.getLogger(ExperimentQuickStatusSP.class.getName());

    @Override
    public String callStoredProc(String jobId, Long expId, String who) throws SQLException, JSONException, FileNotFoundException, IOException {
      Connection conn = CloudSQLConnectionManager.getInstance().getConnection();
      CloudSQLReportsDaoImpl daoImpl = new CloudSQLReportsDaoImpl();
      int ctr = 0;
      int colCount = 0;
      ResultSetMetaData rsmd = null;
      CallableStatement cStmt = null;
      JSONBlobWriter blobWriter = new JSONBlobWriter();
      JSONArray multipleRecords = new JSONArray();
      List<String> participantsLst = null;
      if (who == null) {
        participantsLst = daoImpl.getParticipants(expId);
      } else {
        participantsLst = Lists.newArrayList(who);
      }
          
      Long startTime = System.currentTimeMillis();

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
            multipleRecords.put(eachRecord);
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
      String key = blobWriter.writeBlobUsingNewApi(jobId, multipleRecords.toString()).getKeyString();
      return key;
    }
}
