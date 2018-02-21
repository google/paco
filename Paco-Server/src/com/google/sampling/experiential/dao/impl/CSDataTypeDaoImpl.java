package com.google.sampling.experiential.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.cloudsql.columns.DataTypeColumns;
import com.google.sampling.experiential.dao.CSDataTypeDao;
import com.google.sampling.experiential.dao.dataaccess.DataType;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.PacoId;
import com.google.sampling.experiential.server.QueryConstants;
import com.pacoapp.paco.shared.util.ErrorMessages;

public class CSDataTypeDaoImpl implements CSDataTypeDao {
  public static final Logger log = Logger.getLogger(CSDataTypeDaoImpl.class.getName());
 
  @Override
  public List<DataType> getAllDataTypes() throws SQLException {
    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement statementSelectAllDataTypes = null;
    List<DataType> dataTypes = Lists.newArrayList();
    DataType eachDataType = null;
    PacoId id = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementSelectAllDataTypes = conn.prepareStatement(QueryConstants.GET_ALL_DATATYPES.toString());
      rs = statementSelectAllDataTypes.executeQuery();
      while (rs.next()) {
        eachDataType = new DataType();
        id = new PacoId();
        id.setId(new Long(rs.getInt(DataTypeColumns.DATA_TYPE_ID)));
        id.setIsCreatedWithThisCall(false);
        eachDataType.setDataTypeId(id);
        eachDataType.setMultiSelect(rs.getBoolean(DataTypeColumns.MULTI_SELECT));
        eachDataType.setNumeric(rs.getBoolean(DataTypeColumns.IS_NUMERIC));
        eachDataType.setName(rs.getString(DataTypeColumns.NAME));
        eachDataType.setResponseMappingRequired(rs.getBoolean(DataTypeColumns.RESPONSE_MAPPING_REQUIRED));
        
        dataTypes.add(eachDataType);
      }
    } finally {
      try {
        if ( rs != null) {
          rs.close();
        }
        if (statementSelectAllDataTypes != null) {
          statementSelectAllDataTypes.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
    return dataTypes;
  }
  
  // utility method, to avoid hitting the db for every input on an experiment
  public DataType getMatchingDataType(List<DataType> fullList, String name, Boolean isNumeric, Boolean isMultiSelect) {
    DataType matchingId = null;
    for(DataType dataType : fullList) { 
      if (dataType.getName().equalsIgnoreCase(name) && dataType.isNumeric() == isNumeric && dataType.isMultiSelect() == isMultiSelect) {
        matchingId = dataType;
        break;
      }
    }
    return matchingId;
  }
}
