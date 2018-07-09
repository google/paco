package com.google.sampling.experiential.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.logging.Logger;

import com.google.cloud.sql.jdbc.Statement;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.cloudsql.columns.ExternStringInputColumns;
import com.google.sampling.experiential.cloudsql.columns.InputColumns;
import com.google.sampling.experiential.dao.CSDataTypeDao;
import com.google.sampling.experiential.dao.CSExternStringInputDao;
import com.google.sampling.experiential.dao.CSInputDao;
import com.google.sampling.experiential.dao.dataaccess.DataType;
import com.google.sampling.experiential.dao.dataaccess.ExternStringInput;
import com.google.sampling.experiential.dao.dataaccess.Input;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.PacoId;
import com.google.sampling.experiential.server.QueryConstants;
import com.pacoapp.paco.shared.util.ErrorMessages;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;

public class CSInputDaoImpl implements CSInputDao {
  public static final Logger log = Logger.getLogger(CSInputDaoImpl.class.getName());
  private CSDataTypeDao dataTypeDao = new CSDataTypeDaoImpl();
  private CSExternStringInputDao externStringDao = new CSExternStringInputDaoImpl();
  private static List<Column> inputColList = Lists.newArrayList();
  List<DataType> allDataTypes = null;

  static {
    inputColList.add(new Column(InputColumns.NAME_ID));
    inputColList.add(new Column(InputColumns.REQUIRED));
    inputColList.add(new Column(InputColumns.CONDITIONAL));
    inputColList.add(new Column(InputColumns.RESPONSE_DATA_TYPE_ID));
    inputColList.add(new Column(InputColumns.TEXT_ID));
    inputColList.add(new Column(InputColumns.LIKERT_STEPS));
    inputColList.add(new Column(InputColumns.LEFT_LABEL));
    inputColList.add(new Column(InputColumns.RIGHT_LABEL));
  }
  
  @Override
  public void insertInput(Input input) throws Exception {
    insertInput(Lists.newArrayList(input));
  }
  
  @Override
  public List<Input> insertVariableNames(List<String> variableNames) throws Exception {
    List<Input> inputLst = Lists.newArrayList();
    Input input = null;
    CSDataTypeDao dataTypeDaoImpl =  new CSDataTypeDaoImpl();
   
    if (allDataTypes == null) {
      allDataTypes = dataTypeDaoImpl.getAllDataTypes();
    }
    DataType undefinedDataType = dataTypeDaoImpl.getMatchingDataType(allDataTypes, "undefined", false, false);
    
    for (String variableName : variableNames) {
      input = new Input();
      input.setName(new ExternStringInput(variableName));
      input.setText(new ExternStringInput(variableName));
      input.setResponseDataType(undefinedDataType);
      inputLst.add(input);
    }
    insertInput(inputLst);
    return inputLst;
  }
  
  @Override
  public String getLabelForInputId(Long inputId) throws SQLException {
   
    String label = null;
    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement statementSelectGetLabel = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementSelectGetLabel = conn.prepareStatement(QueryConstants.GET_LABEL_FOR_INPUT_ID.toString());
      statementSelectGetLabel.setLong(1, inputId);
      log.info(statementSelectGetLabel.toString());
      rs = statementSelectGetLabel.executeQuery();
      while(rs.next()){
        label = rs.getString(ExternStringInputColumns.LABEL);
      }
    } finally {
      try {
        if ( rs != null) {
          rs.close();
        }
        if (statementSelectGetLabel != null) {
          statementSelectGetLabel.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }

    return label;
    
  }
  
  @Override
  public void insertInput(List<Input> inputs) throws Exception {
    Connection conn = null;
    PreparedStatement statementCreateInput = null;
    ResultSet rs = null;
    ExpressionList insertInputExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    Insert inputInsert = new Insert();
    boolean isAnyInputInserted = false;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      conn.setAutoCommit(false);
      inputInsert.setTable(new Table(InputColumns.TABLE_NAME));
      inputInsert.setUseValues(true);
      insertInputExprList.setExpressions(exp);
      inputInsert.setItemsList(insertInputExprList);
      inputInsert.setColumns(inputColList);
      // Adding ? for prepared stmt
      for (Column c : inputColList) {
        ((ExpressionList) inputInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }

      statementCreateInput = conn.prepareStatement(inputInsert.toString(), Statement.RETURN_GENERATED_KEYS);
      List<DataType> allDataTypes = dataTypeDao.getAllDataTypes();
      for (Input input : inputs) {
        if (input.getInputId() == null || input.getInputId().getId() == null) {
          isAnyInputInserted = true;
          PacoId variableNameId = externStringDao.getTextAndCreate(input.getName().getLabel(), true);
          PacoId textId = externStringDao.getTextAndCreate(input.getText().getLabel(), true);
          statementCreateInput.setLong(1, variableNameId.getId());
          statementCreateInput.setBoolean(2, input.isRequired());
          statementCreateInput.setString(3, input.getConditional());
          DataType responseDataType = dataTypeDao.getMatchingDataType(allDataTypes, input.getResponseDataType().getName(), input.getResponseDataType().isNumeric(), input.getResponseDataType().isMultiSelect());
          input.setResponseDataType(responseDataType);
          Integer dataTypeId = responseDataType.getDataTypeId().getId().intValue();
          statementCreateInput.setObject(4, dataTypeId, Types.INTEGER);
          statementCreateInput.setLong(5, textId.getId());
          statementCreateInput.setInt(6, input.getLikertSteps()==null ? 0:input.getLikertSteps());
          statementCreateInput.setString(7, input.getLeftLabel());
          statementCreateInput.setString(8, input.getRightLabel());
          statementCreateInput.addBatch();
        }
        
      } //for
      if (isAnyInputInserted) {
        statementCreateInput.executeBatch();
        ResultSet generatedKeys = statementCreateInput.getGeneratedKeys();
        
        for (Input input : inputs) {
          if ( generatedKeys == null || ! generatedKeys.next()) {
            log.warning("Unable to retrieve all generated keys");
            throw new Exception("Input not persisted");
          } else {
            input.setInputId(new PacoId(generatedKeys.getLong(1), true));
          }
        }
        conn.commit();
      }
      
    } catch(SQLException sqle) {
      log.warning("Exception while inserting to input table:" +  sqle);
      throw sqle;
    } finally {
      try {
        if( rs != null) { 
          rs.close();
        }
        if (statementCreateInput != null) {
          statementCreateInput.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.info(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
      }
    }
  }
  
  @Override
  public boolean deleteAllInputs(List<Long> inputIds) throws SQLException {
   
    Connection conn = null;
    PreparedStatement deleteAllInputs = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      deleteAllInputs = conn.prepareStatement(QueryConstants.DELETE_FROM_INPUT.toString());
      for (Long eachInputId : inputIds) {
        deleteAllInputs.setLong(1, eachInputId);
        deleteAllInputs.addBatch();
      }
      deleteAllInputs.executeBatch();
      
    } finally {
      try {
        if (deleteAllInputs != null) {
          deleteAllInputs.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }

    return true;
  }
}
