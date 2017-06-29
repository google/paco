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

public class ExperimentCompleteStatusSP implements StoredProc {
    public static final Logger log = Logger.getLogger(ExperimentCompleteStatusSP.class.getName());
    List<String> participantsLst = null;
    @Override
    public String callStoredProc(String jobId, Long expId, String who) throws SQLException, JSONException, FileNotFoundException, IOException{
      String key = null;
      Connection conn = CloudSQLConnectionManager.getInstance().getConnection();
      CloudSQLReportsDaoImpl daoImpl = new CloudSQLReportsDaoImpl();
      JSONBlobWriter blobWriter = new JSONBlobWriter();
      if (who == null) {
        participantsLst = daoImpl.getParticipants(expId);
      } else {
        participantsLst = Lists.newArrayList(who);
      }
      log.info("participant list size"+ participantsLst.size());
      CallableStatement cStmt = conn.prepareCall("call ExpCompleteStatus(?,?)");
      JSONArray multipleUserRecords = new JSONArray();
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
          multipleUserRecords.put(eachUserRecord);
        }//while
        
      }
      key = blobWriter.writeBlobUsingNewApi(jobId, multipleUserRecords.toString()).getKeyString();
      log.info("finished json blob"+ key);
      return key;
    }
}
