package com.google.sampling.experiential.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;

import com.google.common.collect.Maps;
import com.google.sampling.experiential.cloudsql.columns.GroupTypeColumns;
import com.google.sampling.experiential.dao.CSGroupTypeDao;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.QueryConstants;
import com.pacoapp.paco.shared.util.ErrorMessages;

public class CSGroupTypeDaoImpl implements CSGroupTypeDao {
  public static final Logger log = Logger.getLogger(CSGroupTypeDaoImpl.class.getName());
  static Map<String, Integer> groupTypeNameIdMap = null;

  @Override
  public Integer getGroupTypeId(String groupTypeName) throws SQLException {
   if (groupTypeNameIdMap == null) {
     groupTypeNameIdMap = Maps.newHashMap();
     Connection conn = null;
     PreparedStatement statementSelectAllGroupTypes = null;
     try {
       conn = CloudSQLConnectionManager.getInstance().getConnection();
       statementSelectAllGroupTypes = conn.prepareStatement(QueryConstants.GET_ALL_GROUP_TYPE.toString());
       ResultSet rs = statementSelectAllGroupTypes.executeQuery();
       while(rs.next()){
        
         groupTypeNameIdMap.put(rs.getString(GroupTypeColumns.GROUP_TYPE_NAME), rs.getInt(GroupTypeColumns.GROUP_TYPE_ID));
       }
     } finally {
       try {
         if (statementSelectAllGroupTypes != null) {
           statementSelectAllGroupTypes.close();
         }
         if (conn != null) {
           conn.close();
         }
       } catch (SQLException ex1) {
         log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
       }
     }
    
   }
   return groupTypeNameIdMap.get(groupTypeName);
  }

}
