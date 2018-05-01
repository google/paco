package com.google.sampling.experiential.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.cloudsql.columns.DataTypeColumns;
import com.google.sampling.experiential.cloudsql.columns.ExternStringInputColumns;
import com.google.sampling.experiential.cloudsql.columns.GroupTypeColumns;
import com.google.sampling.experiential.cloudsql.columns.GroupTypeInputMappingColumns;
import com.google.sampling.experiential.cloudsql.columns.InputColumns;
import com.google.sampling.experiential.dao.CSGroupTypeInputMappingDao;
import com.google.sampling.experiential.dao.dataaccess.DataType;
import com.google.sampling.experiential.dao.dataaccess.ExternStringInput;
import com.google.sampling.experiential.dao.dataaccess.GroupTypeInputMapping;
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

public class CSGroupTypeInputMappingDaoImpl implements CSGroupTypeInputMappingDao {
  public static final Logger log = Logger.getLogger(CSGroupTypeInputMappingDaoImpl.class.getName());
  private static List<Column> predefinedInputColList = Lists.newArrayList();
  static Map<String, List<Input>> predefinedInputsMap = null;
  static {
    predefinedInputColList.add(new Column(GroupTypeInputMappingColumns.GROUP_TYPE_ID));
    predefinedInputColList.add(new Column(GroupTypeInputMappingColumns.INPUT_ID));
  }
  
  @Override
  public Map<String, List<Input>> getAllFeatureInputs() throws SQLException {
    if (predefinedInputsMap != null) {
      return predefinedInputsMap;
    }
    
    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement statementSelectAllFeatureInputs = null;
    List<Input> inputs = null;
    Input eachInput = null;
    DataType responseDataType = null;
    String currentFeature = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementSelectAllFeatureInputs = conn.prepareStatement(QueryConstants.GET_ALL_PRE_DEFINED_INPUTS.toString());
      rs = statementSelectAllFeatureInputs.executeQuery();
      predefinedInputsMap = Maps.newHashMap();
      while (rs.next()) {
        eachInput = new Input();
        responseDataType = new DataType();
        
        currentFeature = rs.getString(GroupTypeColumns.GROUP_TYPE_NAME);
        eachInput.setInputId(new PacoId(rs.getLong(GroupTypeInputMappingColumns.INPUT_ID), false));
        eachInput.setName(new ExternStringInput(rs.getString("esi1." +ExternStringInputColumns.LABEL), rs.getLong("esi1."+ ExternStringInputColumns.EXTERN_STRING_INPUT_ID)));
        eachInput.setText(new ExternStringInput(rs.getString("esi2." +ExternStringInputColumns.LABEL), rs.getLong("esi2."+ ExternStringInputColumns.EXTERN_STRING_INPUT_ID)));
        eachInput.setConditional(rs.getString(InputColumns.CONDITIONAL));
        eachInput.setLeftLabel(rs.getString(InputColumns.LEFT_LABEL));
        eachInput.setRightLabel(rs.getString(InputColumns.RIGHT_LABEL));
        eachInput.setRequired(rs.getBoolean(InputColumns.REQUIRED));
        eachInput.setParentId(new PacoId(rs.getLong(InputColumns.PARENT_ID),false));
        eachInput.setLikertSteps(rs.getInt(InputColumns.LIKERT_STEPS));
        
        responseDataType.setDataTypeId(new PacoId(rs.getInt(DataTypeColumns.DATA_TYPE_ID), false));
        responseDataType.setName(rs.getString(DataTypeColumns.NAME));
        responseDataType.setMultiSelect(rs.getBoolean(DataTypeColumns.MULTI_SELECT));
        responseDataType.setNumeric(rs.getBoolean(DataTypeColumns.IS_NUMERIC));
        
        eachInput.setResponseDataType(responseDataType);
        
        if (predefinedInputsMap.get(currentFeature) != null) {
          predefinedInputsMap.get(currentFeature).add(eachInput);
        } else {
          inputs = Lists.newArrayList();
          inputs.add(eachInput);
          predefinedInputsMap.put(currentFeature, inputs);
        }
      }
    } finally {
      try {
        if ( rs != null) {
          rs.close();
        }
        if (statementSelectAllFeatureInputs != null) {
          statementSelectAllFeatureInputs.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
    return predefinedInputsMap;
  }
  
  public void insertGroupTypeInputMapping(GroupTypeInputMapping predefinedInput) {
    Connection conn = null;
    PreparedStatement statementCreatePredefinedInput = null;
    ResultSet rs = null;
    ExpressionList insertPredefinedInputExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    Insert predefinedInputInsert = new Insert();
    if (predefinedInput != null) {
      try {
        log.info("Inserting predefined input into table" + predefinedInput);
        conn = CloudSQLConnectionManager.getInstance().getConnection();
        conn.setAutoCommit(false);
        predefinedInputInsert.setTable(new Table(GroupTypeInputMappingColumns.TABLE_NAME));
        predefinedInputInsert.setUseValues(true);
        insertPredefinedInputExprList.setExpressions(exp);
        predefinedInputInsert.setItemsList(insertPredefinedInputExprList);
        predefinedInputInsert.setColumns(predefinedInputColList);
        // Adding ? for prepared stmt
        for (Column c : predefinedInputColList) {
          ((ExpressionList) predefinedInputInsert.getItemsList()).getExpressions().add(new JdbcParameter());
        }
  
        statementCreatePredefinedInput = conn.prepareStatement(predefinedInputInsert.toString());
        statementCreatePredefinedInput.setInt(1, predefinedInput.getGroupTypeId());
        statementCreatePredefinedInput.setLong(2, predefinedInput.getInput().getInputId().getId());
        
        log.info(statementCreatePredefinedInput.toString());
        statementCreatePredefinedInput.execute();
        
        conn.commit();
      } catch(SQLException sqle) {
        log.warning("Exception while inserting to pre defined input table" + predefinedInput + ":" +  sqle);
      }
      finally {
        try {
          if( rs != null) { 
            rs.close();
          }
          if (statementCreatePredefinedInput != null) {
            statementCreatePredefinedInput.close();
          }
          if (conn != null) {
            conn.close();
          }
        } catch (SQLException ex1) {
          log.info(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
        }
      }
    } 
  }

  @Override
  public Map<String, List<String>> getAllPredefinedFeatureVariableNames() throws SQLException {
    Map<String, List<String>> predefinedFeatureInputVariableNamesMap = Maps.newHashMap();
    Map<String, List<Input>> predefinedFeatureInputMap = getAllFeatureInputs();
    Iterator<Entry<String, List<Input>>> predefinedItr = predefinedFeatureInputMap.entrySet().iterator();
    while (predefinedItr.hasNext()) {
      Entry<String,List<Input>> entry = predefinedItr.next();
      predefinedFeatureInputVariableNamesMap.put(entry.getKey(), convertInputToVariableName(entry.getValue()));
    }
    return predefinedFeatureInputVariableNamesMap;
  }
  
  private List<String> convertInputToVariableName (List<Input> inputLst) {
    List<String> inputVariableNames = Lists.newArrayList();
    for (Input i : inputLst) {
      inputVariableNames.add(i.getName().getLabel());
    }
    return inputVariableNames;
  }
 
 
}
