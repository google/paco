package com.google.sampling.experiential.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import com.google.cloud.sql.jdbc.Statement;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.cloudsql.columns.DataTypeColumns;
import com.google.sampling.experiential.dao.CSDataTypeDao;
import com.google.sampling.experiential.dao.dataaccess.DataType;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.ExceptionUtil;
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
  public DataType getMatchingDataType(List<DataType> fullList, String name, Boolean isNumeric, Boolean isMultiSelect) throws SQLException {
    DataType matchingId = null;
    for(DataType dataType : fullList) { 
      if (dataType.getName().equalsIgnoreCase(name) && dataType.isNumeric() == isNumeric && dataType.isMultiSelect() == isMultiSelect) {
        matchingId = dataType;
        break;
      }
    }
    if (matchingId == null) {
      try {
        matchingId = insertNewDataType(name, isNumeric, isMultiSelect);
      } catch (SQLException e) {
        log.warning("no matching data type "+ name + "--" + isNumeric + "--"+ isMultiSelect);
        matchingId = null;
        throw e;
      }
    }
    return matchingId;
  }
  
  @Override
  public DataType insertNewDataType(String responseTypeName, Boolean isNumeric, boolean isMultiSelect) throws SQLException {
    final String insertDataTypeSql = "INSERT INTO `pacodb`.`data_type` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('"+responseTypeName+"',"+ isNumeric +","+ isMultiSelect +", 0)";
    Connection conn = null;
    PreparedStatement statementModifyExisting = null;
    DataType newDataType = null; 
    ResultSet rs = null;
    Integer dataTypeId = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementModifyExisting = conn.prepareStatement(insertDataTypeSql, Statement.RETURN_GENERATED_KEYS);
      log.info(insertDataTypeSql);
      statementModifyExisting.execute();
      rs = statementModifyExisting.getGeneratedKeys();
      if (rs.next()) {
        dataTypeId = rs.getInt(1);
      }
      newDataType = new DataType();
      newDataType.setDataTypeId(new PacoId(dataTypeId, true));
      newDataType.setMultiSelect(isMultiSelect);
      newDataType.setNumeric(isNumeric);
      newDataType.setResponseMappingRequired(false);
    } catch (SQLException sqle) {
      log.warning("SQLException while adding new cols" + ExceptionUtil.getStackTraceAsString(sqle));
      throw sqle;
    } catch (Exception e) {
      log.warning("GException while adding new cols" + ExceptionUtil.getStackTraceAsString(e));
      throw e;
    } finally {
      try {
        if (statementModifyExisting != null) {
          statementModifyExisting.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
          log.warning("Exception in finally block" + ExceptionUtil.getStackTraceAsString(e));
      }
    }
    return newDataType;
  }
}
