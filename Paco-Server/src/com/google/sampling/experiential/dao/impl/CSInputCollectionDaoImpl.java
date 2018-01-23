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
import java.util.logging.Logger;

import com.google.cloud.sql.jdbc.Statement;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.cloudsql.columns.InputCollectionColumns;
import com.google.sampling.experiential.dao.CSChoiceCollectionDao;
import com.google.sampling.experiential.dao.CSDataTypeDao;
import com.google.sampling.experiential.dao.CSInputCollectionDao;
import com.google.sampling.experiential.dao.CSInputDao;
import com.google.sampling.experiential.dao.dataaccess.ChoiceCollection;
import com.google.sampling.experiential.dao.dataaccess.DataType;
import com.google.sampling.experiential.dao.dataaccess.Input;
import com.google.sampling.experiential.dao.dataaccess.InputCollection;
import com.google.sampling.experiential.dao.dataaccess.InputOrderAndChoice;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.IdGenerator;
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
  public void createInputCollectionId(ExperimentDAO exptDao, Map<String, InputCollection> newVersionGroupInputCollections,  Map<String, InputCollection> oldVersionGroupInputCollections) throws SQLException {
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
        log.info("Inserting input collection into input_collection_history table" + exptDao.getId());
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
              log.info("input size is greater than 0");
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
                if (currentDataType != null && currentDataType.isResponseMappingRequired() && choiceCollection != null && choiceCollectionId == null) {
                  choiceDaoImpl.createChoiceCollectionId(exptDao.getId(), inputCollectionId, currentIOC.getInputOrder(), choiceCollection);
                  choiceCollectionId = BigInteger.valueOf(choiceCollection.getChoiceCollectionId());
                  statementCreateInputCollection.setObject(4, choiceCollectionId, Types.BIGINT);
                } else {
                  statementCreateInputCollection.setObject(4, choiceCollectionId, Types.BIGINT);
                }
                statementCreateInputCollection.setInt(5, currentIOC.getInputOrder());
                log.info(statementCreateInputCollection.toString());
                statementCreateInputCollection.addBatch();
              }
              
              statementCreateInputCollection.executeBatch();
              currentGroupInputCollection.setInputCollectionId(inputCollectionId.longValue());
            }
          }  else {
            log.info("input collection id already set");
          }
        }
        conn.commit();
      } catch(SQLException sqle) {
        log.warning("Exception while inserting to input collection table" + exptDao.getId() + ":" +  sqle);
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
}
