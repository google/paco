package com.google.sampling.experiential.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import com.google.sampling.experiential.dao.CSPivotHelperDao;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.QueryConstants;
import com.pacoapp.paco.shared.util.ErrorMessages;

public class CSPivotHelperDaoImpl implements CSPivotHelperDao {
  public static final Logger log = Logger.getLogger(CSPivotHelperDaoImpl.class.getName());
  
  @Override
  public void incrementUpdateCtByOne(Long evmId, Integer anonWho, List<Long> inputIds) throws SQLException {
    Connection conn = null;
    PreparedStatement statementUpdateEvent = null;
    // this uses upsert command, which inserts first time, and then increments events ct by 1
    String updateQuery = QueryConstants.INSERT_TO_PIVOT_HELPER_WITH_ON_DUPLICATE_CLAUSE.toString() ;

    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementUpdateEvent = conn.prepareStatement(updateQuery);
      for (Long inputId : inputIds) {
        statementUpdateEvent.setLong(1, evmId);
        statementUpdateEvent.setInt(2, anonWho);
        statementUpdateEvent.setLong(3, inputId);
        statementUpdateEvent.setInt(4, 1);
        statementUpdateEvent.setBoolean(5, true);
        
        statementUpdateEvent.addBatch();
      }
      statementUpdateEvent.executeBatch();
      
    } finally {
      try {
        if (statementUpdateEvent != null) {
          statementUpdateEvent.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
    
  }
}
