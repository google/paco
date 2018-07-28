package com.google.sampling.experiential.dao.impl;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.google.cloud.sql.jdbc.Statement;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.sampling.experiential.cloudsql.columns.InputCollectionColumns;
import com.google.sampling.experiential.dao.CSChoiceCollectionDao;
import com.google.sampling.experiential.dao.CSExperimentVersionGroupMappingDao;
import com.google.sampling.experiential.dao.CSInputCollectionDao;
import com.google.sampling.experiential.dao.CSInputDao;
import com.google.sampling.experiential.dao.dataaccess.ChoiceCollection;
import com.google.sampling.experiential.dao.dataaccess.DataType;
import com.google.sampling.experiential.dao.dataaccess.Input;
import com.google.sampling.experiential.dao.dataaccess.InputCollection;
import com.google.sampling.experiential.dao.dataaccess.InputOrderAndChoice;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.IdGenerator;
import com.google.sampling.experiential.server.QueryConstants;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.util.ErrorMessages;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;

public class CSInputCollectionDaoImpl implements CSInputCollectionDao {
  public static final Logger log = Logger.getLogger(CSInputCollectionDaoImpl.class.getName());
  private static List<Column> inputCollectionColList = Lists.newArrayList();
  private CSChoiceCollectionDao choiceDaoImpl = new CSChoiceCollectionDaoImpl();
  private CSInputDao inputDaoImpl = new CSInputDaoImpl();
  
  static {
    inputCollectionColList.add(new Column(InputCollectionColumns.EXPERIMENT_ID));
    inputCollectionColList.add(new Column(InputCollectionColumns.INPUT_COLLECTION_ID));
    inputCollectionColList.add(new Column(InputCollectionColumns.INPUT_ID));
    inputCollectionColList.add(new Column(InputCollectionColumns.CHOICE_COLLECTION_ID));
    inputCollectionColList.add(new Column(InputCollectionColumns.INPUT_ORDER));
  }
  
  @Override
  public void createInputCollectionId(ExperimentDAO exptDao, Map<String, InputCollection> newVersionGroupInputCollections,  Map<String, InputCollection> oldVersionGroupInputCollections) throws Exception {
    Connection conn = null;
    PreparedStatement statementCreateInputCollection = null;
    ResultSet rs = null;
    Iterator<String> currentInputVarNameIOCItr = null;
    ChoiceCollection choiceCollection = null;
    String currentInputVarName = null;
    String currentGroupName = null;
    InputOrderAndChoice currentIOC = null;
    InputCollection currentGroupInputCollection = null;
    Map<String, InputOrderAndChoice> currentInputVarNameIOC = null;
    DataType currentDataType = null;
    BigInteger choiceCollectionId = null;
  
    ExpressionList insertInputcollectionExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    Insert inputCollectionInsert = new Insert();
    Iterator<String> newVersionGroupItr = newVersionGroupInputCollections.keySet().iterator(); 
    if (exptDao != null) {
      try {
        conn = CloudSQLConnectionManager.getInstance().getConnection();
        conn.setAutoCommit(false);
        inputCollectionInsert.setTable(new Table(InputCollectionColumns.TABLE_NAME));
        inputCollectionInsert.setUseValues(true);
        insertInputcollectionExprList.setExpressions(exp);
        inputCollectionInsert.setItemsList(insertInputcollectionExprList);
        inputCollectionInsert.setColumns(inputCollectionColList);
        // Adding ? for prepared stmt
        for (Column c : inputCollectionColList) {
           ((ExpressionList) inputCollectionInsert.getItemsList()).getExpressions().add(new JdbcParameter());
        }
   
        statementCreateInputCollection = conn.prepareStatement(inputCollectionInsert.toString(), Statement.RETURN_GENERATED_KEYS);
        int groupCount = 0;
        while (newVersionGroupItr.hasNext()) {
          groupCount++;
          currentGroupName = newVersionGroupItr.next();
          BigInteger inputCollectionId = IdGenerator.generate(new BigInteger(exptDao.getVersion().toString()), groupCount);
          currentGroupInputCollection = newVersionGroupInputCollections.get(currentGroupName);
          if (currentGroupInputCollection != null && currentGroupInputCollection.getInputCollectionId() == null) {
            currentInputVarNameIOC = currentGroupInputCollection.getInputOrderAndChoices();
            currentInputVarNameIOCItr = currentInputVarNameIOC.keySet().iterator();
            if ( currentInputVarNameIOC.size() >0) {
              while (currentInputVarNameIOCItr.hasNext()) {
                currentInputVarName = currentInputVarNameIOCItr.next();
                currentIOC = currentInputVarNameIOC.get(currentInputVarName);
                choiceCollection   = currentIOC.getChoiceCollection();
                if (choiceCollection != null && choiceCollection.getChoiceCollectionId() != null) {
                  choiceCollectionId = BigInteger.valueOf(choiceCollection.getChoiceCollectionId());
                } else {
                  choiceCollectionId = null;
                }
                
                statementCreateInputCollection.setLong(1, exptDao.getId());
                statementCreateInputCollection.setObject(2, inputCollectionId, Types.BIGINT);
                inputDaoImpl.insertInput(currentIOC.getInput());
                statementCreateInputCollection.setLong(3, currentIOC.getInput().getInputId().getId());
                currentDataType =  currentIOC.getInput().getResponseDataType();
                
                if (currentDataType != null && choiceCollection != null && choiceCollectionId == null) {
                  choiceDaoImpl.createChoiceCollectionId(exptDao.getId(), inputCollectionId, currentIOC.getInputOrder(), choiceCollection);
                  choiceCollectionId = BigInteger.valueOf(choiceCollection.getChoiceCollectionId());
                  statementCreateInputCollection.setObject(4, choiceCollectionId, Types.BIGINT);
                } else {
                  statementCreateInputCollection.setObject(4, choiceCollectionId, Types.BIGINT);
                }
                statementCreateInputCollection.setInt(5, currentIOC.getInputOrder());
                statementCreateInputCollection.addBatch();
              }
              
              statementCreateInputCollection.executeBatch();
              currentGroupInputCollection.setInputCollectionId(inputCollectionId.longValue());
            }
          }
        }
        conn.commit();
      } catch(SQLException sqle) {
        log.warning("Exception while inserting to input collection table" + exptDao.getId() + ":" +  sqle);
        throw sqle;
      } finally {
        try {
          if( rs != null) { 
            rs.close();
          }
          if (statementCreateInputCollection != null) {
            statementCreateInputCollection.close();
          }
          if (conn != null) {
            conn.close();
          }
        } catch (SQLException ex1) {
         log.info(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
       }
     }
   } else {
     log.warning("insert input collection failed" + exptDao);
   }
  }

  @Override
  public Input addUndefinedInputToCollection(Long experimentId, Long inputCollectionId, String variableName) throws Exception {
    CSInputDao inputDao = new CSInputDaoImpl();
    List<Input> inputLst = inputDao.insertVariableNames(Lists.newArrayList(variableName));
    addInputToInputCollection(experimentId, inputCollectionId, inputLst.get(0));
    return inputLst.get(0);
  }
  
  @Override
  public void addInputToInputCollection(Long experimentId, Long inputCollectionId, Input input) throws SQLException {
   
    Connection conn = null;
    PreparedStatement statementCreateInput = null;
    ResultSet rs = null;
    ExpressionList insertInputExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    Insert inputInsert = new Insert();
    try {
//      log.info("Inserting undefined input into input collection table");
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      conn.setAutoCommit(false);
      inputInsert.setTable(new Table(InputCollectionColumns.TABLE_NAME));
      inputInsert.setUseValues(true);
      insertInputExprList.setExpressions(exp);
      inputInsert.setItemsList(insertInputExprList);
      inputInsert.setColumns(inputCollectionColList);
      // Adding ? for prepared stmt
      for (Column c : inputCollectionColList) {
        ((ExpressionList) inputInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }

      statementCreateInput = conn.prepareStatement(inputInsert.toString());
      Long choiceCollectionId = null;
      statementCreateInput.setLong(1, experimentId);
      statementCreateInput.setLong(2, inputCollectionId);
      statementCreateInput.setLong(3, input.getInputId().getId());
      statementCreateInput.setObject(4, choiceCollectionId);
      statementCreateInput.setLong(5, -99);
      log.info(statementCreateInput.toString());
      statementCreateInput.execute();
      conn.commit();
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
  public void addInputsToInputCollection(Long experimentId, InputCollection inputCollection, List<Input> inputs) throws SQLException {
   
    Connection conn = null;
    PreparedStatement statementCreateInput = null;
    ResultSet rs = null;
    Long inputCollectionId = inputCollection.getInputCollectionId();
    ExpressionList insertInputExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    Insert inputInsert = new Insert();
    StringBuffer inputIdsAdded = new StringBuffer();
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      conn.setAutoCommit(false);
      inputInsert.setTable(new Table(InputCollectionColumns.TABLE_NAME));
      inputInsert.setUseValues(true);
      insertInputExprList.setExpressions(exp);
      inputInsert.setItemsList(insertInputExprList);
      inputInsert.setColumns(inputCollectionColList);
      // Adding ? for prepared stmt
      for (Column c : inputCollectionColList) {
        ((ExpressionList) inputInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }

      statementCreateInput = conn.prepareStatement(inputInsert.toString());
      Long choiceCollectionId = null;
      Integer inputOrder = null;
      Set<Long> inputIdsSet = Sets.newHashSet();  
      Map<String, InputOrderAndChoice> iocMap = inputCollection.getInputOrderAndChoices();
      for (Input input : inputs) {
        if (!inputIdsSet.contains(input.getInputId().getId())) {
          inputIdsSet.add( input.getInputId().getId());
          statementCreateInput.setLong(1, experimentId);
          statementCreateInput.setLong(2, inputCollectionId);
          statementCreateInput.setLong(3, input.getInputId().getId());
          
          if (iocMap != null) {
            InputOrderAndChoice oldIoc = iocMap.get(input.getName().getLabel());
            if (oldIoc != null) {
              choiceCollectionId = oldIoc.getChoiceCollection() != null ? oldIoc.getChoiceCollection().getChoiceCollectionId() : null;
              inputOrder = oldIoc.getInputOrder();
            }
          }
          statementCreateInput.setObject(4, choiceCollectionId);
          statementCreateInput.setLong(5, inputOrder != null ? inputOrder : -99);
          inputIdsAdded.append(input.getInputId().getId()+",");
          statementCreateInput.addBatch();
        }
      }
      statementCreateInput.executeBatch();
      conn.commit();
      log.info("added to collection id" + inputCollectionId + inputIdsAdded);
    } catch(SQLException sqle) {
      log.warning("Exception while inserting batch to input collection table:" +  sqle);
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
  public Long getInputCollectionId(Long experimentId, Integer experimentVersion, Integer numberOfGroups, Boolean uniqueFlag) throws SQLException {
    Integer numberOfInputCollectionIdsAlreadyInDB = 0;
    CSExperimentVersionGroupMappingDao daoImpl = new CSExperimentVersionGroupMappingDaoImpl();
    Integer noOfGroups = daoImpl.getNumberOfGroups(experimentId, experimentVersion);
    Long newInputCollectionId = IdGenerator.generate(BigInteger.valueOf(experimentVersion), noOfGroups+1).longValue();
    if (uniqueFlag) {
      while ( true)  {
        numberOfInputCollectionIdsAlreadyInDB = daoImpl.getInputCollectionIdCountForExperiment(experimentId, newInputCollectionId);
        if (numberOfInputCollectionIdsAlreadyInDB == 0) {
          return newInputCollectionId;
        } else {
          newInputCollectionId++;
        }
      }
    }
    return newInputCollectionId;
  }
 
  @Override
  public List<Long> getAllInputIds(Long experimentId, Long inputCollectionId) throws SQLException { 
    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement statementGetAllInputIds = null;
    Long id = null;
    List<Long> inputIds = Lists.newArrayList();
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementGetAllInputIds = conn.prepareStatement(QueryConstants.GET_ALL_INPUT_IDS.toString());
      
      statementGetAllInputIds.setLong(1, experimentId);
      statementGetAllInputIds.setLong(2, inputCollectionId);
      rs = statementGetAllInputIds.executeQuery();
      while (rs.next()) {
        inputIds.add(rs.getLong(InputCollectionColumns.INPUT_ID));
      }
    } finally {
      try {
        if ( rs != null) {
          rs.close();
        }
        if (statementGetAllInputIds != null) {
          statementGetAllInputIds.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
    return inputIds;
    
  }
  
  
  
  @Override
  public boolean deleteDupInputsInInputCollection(Long experimentId, List<Long> inputIds) throws SQLException { 
    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement statementDeleteAllInputIdsWithDupCtr = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementDeleteAllInputIdsWithDupCtr = conn.prepareStatement(QueryConstants.DELELTE_INPUTS_IN_INPUT_COLLECTION_FOR_EXPERIMENT.toString());
      for (Long eachInputId :  inputIds) { 
        statementDeleteAllInputIdsWithDupCtr.setLong(1, experimentId);
        statementDeleteAllInputIdsWithDupCtr.setLong(2, eachInputId);  
        statementDeleteAllInputIdsWithDupCtr.addBatch();
      }
      statementDeleteAllInputIdsWithDupCtr.executeBatch();
    } finally {
      try {
        if ( rs != null) {
          rs.close();
        }
        if (statementDeleteAllInputIdsWithDupCtr != null) {
          statementDeleteAllInputIdsWithDupCtr.close();
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
  
  @Override
  public boolean deleteInputCollectionInputAndChoiceCollection(Long experimentId) throws SQLException { 
    Connection conn = null;
    PreparedStatement statementDeleteAllOutputsAndInputCollection = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementDeleteAllOutputsAndInputCollection = conn.prepareStatement(QueryConstants.DELETE_INPUT_AND_CHOICE_COLLECTION_FOR_EXPT.toString());
      statementDeleteAllOutputsAndInputCollection.setLong(1, experimentId);
      statementDeleteAllOutputsAndInputCollection.execute();
    } finally {
      try {
        if (statementDeleteAllOutputsAndInputCollection != null) {
          statementDeleteAllOutputsAndInputCollection.close();
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
  
  @Override
  public List<Long> getAllDupInputsForExperiment(Long experimentId) throws SQLException { 
    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement statementGetAllInputIdsWithDupCtr = null;
    List<Long> inputIds =  Lists.newArrayList();
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementGetAllInputIdsWithDupCtr = conn.prepareStatement(QueryConstants.GET_INPUT_IDS_WITH_DUP_INPUTS_FOR_EXPERIMENT.toString());
      
      statementGetAllInputIdsWithDupCtr.setLong(1, experimentId);
      log.info(statementGetAllInputIdsWithDupCtr.toString());
      rs = statementGetAllInputIdsWithDupCtr.executeQuery();
      while (rs.next()) {
        inputIds.add(rs.getLong(InputCollectionColumns.INPUT_ID));
      }
    } finally {
      try {
        if ( rs != null) {
          rs.close();
        }
        if (statementGetAllInputIdsWithDupCtr != null) {
          statementGetAllInputIdsWithDupCtr.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
    return inputIds;
    
  }

}

 

