package com.google.sampling.experiential.dao.impl;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.google.cloud.sql.jdbc.Statement;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.cloudsql.columns.ChoiceCollectionColumns;
import com.google.sampling.experiential.dao.CSChoiceCollectionDao;
import com.google.sampling.experiential.dao.CSExternStringListLabelDao;
import com.google.sampling.experiential.dao.dataaccess.Choice;
import com.google.sampling.experiential.dao.dataaccess.ChoiceCollection;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.IdGenerator;
import com.google.sampling.experiential.server.PacoId;
import com.pacoapp.paco.shared.util.ErrorMessages;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;

public class CSChoiceCollectionDaoImpl implements CSChoiceCollectionDao {
  public static final Logger log = Logger.getLogger(CSChoiceCollectionDaoImpl.class.getName());
  private static List<Column> choiceCollectionColList = Lists.newArrayList();
  private CSExternStringListLabelDao labelDaoImpl = new CSExternStringListLabelDaoImpl();
  static {
    choiceCollectionColList.add(new Column(ChoiceCollectionColumns.EXPERIMENT_ID));
    choiceCollectionColList.add(new Column(ChoiceCollectionColumns.CHOICE_COLLECTION_ID));
    choiceCollectionColList.add(new Column(ChoiceCollectionColumns.CHOICE_ID));
    choiceCollectionColList.add(new Column(ChoiceCollectionColumns.CHOICE_ORDER));
  }
  
  @Override
  public void createChoiceCollectionId(Long experimentId, BigInteger inputCollectionId, Integer inputOrder, ChoiceCollection choiceCollection) throws SQLException {
   Connection conn = null;
   PreparedStatement statementCreateChoiceCollection = null;
   ResultSet rs = null;
   BigInteger choiceCollectionId = null;
   Iterator<String> choiceItr = null;
   ExpressionList insertChoicecollectionExprList = new ExpressionList();
   List<Expression> exp = Lists.newArrayList();
   Insert choiceCollectionInsert = new Insert();
   if (choiceCollection != null && choiceCollection.getChoiceCollectionId() == null) {
     try {
       conn = CloudSQLConnectionManager.getInstance().getConnection();
       conn.setAutoCommit(false);
       choiceCollectionInsert.setTable(new Table(ChoiceCollectionColumns.TABLE_NAME));
       choiceCollectionInsert.setUseValues(true);
       insertChoicecollectionExprList.setExpressions(exp);
       choiceCollectionInsert.setItemsList(insertChoicecollectionExprList);
       choiceCollectionInsert.setColumns(choiceCollectionColList);
       // Adding ? for prepared stmt
       for (Column c : choiceCollectionColList) {
         ((ExpressionList) choiceCollectionInsert.getItemsList()).getExpressions().add(new JdbcParameter());
       }
 
       statementCreateChoiceCollection = conn.prepareStatement(choiceCollectionInsert.toString(), Statement.RETURN_GENERATED_KEYS);
       
       choiceCollectionId = IdGenerator.generate(inputCollectionId.divide(BigInteger.valueOf(1000)), inputOrder);
       Choice currentChoice = null;
       PacoId currentChoiceId = null;
       choiceItr = choiceCollection.getChoices().keySet().iterator();
       while (choiceItr.hasNext()) {
         currentChoice = choiceCollection.getChoices().get(choiceItr.next());
         currentChoiceId = labelDaoImpl.getListLabelAndCreate(currentChoice.getChoiceLabel().getLabel(), true);
         statementCreateChoiceCollection.setLong(1, experimentId);
         statementCreateChoiceCollection.setObject(2, choiceCollectionId, Types.BIGINT);
         statementCreateChoiceCollection.setLong(3, currentChoiceId.getId());
         currentChoice.getChoiceLabel().setExternStringListLabelId(currentChoiceId);
         statementCreateChoiceCollection.setLong(4, currentChoice.getChoiceOrder());
         statementCreateChoiceCollection.addBatch();
       }
       choiceCollection.setChoiceCollectionId(choiceCollectionId.longValue());
       statementCreateChoiceCollection.executeBatch();
       conn.commit();
     } catch(SQLException sqle) {
       log.warning("Exception while inserting to choice collection table" + experimentId + ":" +  sqle);
       throw sqle;
     }
     finally {
       try {
         if( rs != null) { 
           rs.close();
         }
         if (statementCreateChoiceCollection != null) {
           statementCreateChoiceCollection.close();
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
}
