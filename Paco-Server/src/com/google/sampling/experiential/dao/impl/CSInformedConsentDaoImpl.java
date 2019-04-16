package com.google.sampling.experiential.dao.impl;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.cloudsql.columns.InformedConsentColumns;
import com.google.sampling.experiential.dao.CSInformedConsentDao;
import com.google.sampling.experiential.dao.dataaccess.InformedConsent;
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

public class CSInformedConsentDaoImpl implements CSInformedConsentDao {
  public static final Logger log = Logger.getLogger(CSExperimentDetailDaoImpl.class.getName());
  private static List<Column> informedConsentColList = Lists.newArrayList();
  static {
    informedConsentColList.add(new Column(InformedConsentColumns.EXPERIMENT_ID));
    informedConsentColList.add(new Column(InformedConsentColumns.INFORMED_CONSENT_ID));
    informedConsentColList.add(new Column(InformedConsentColumns.INFORMED_CONSENT));
  }
  @Override
  public void insertInformedConsent(InformedConsent informedConsent, Integer experimentVersion) throws SQLException {
    Connection conn = null;
    PreparedStatement statementCreateInformedConsent = null;
    ResultSet rs = null;
    BigInteger informedConsentId = null;
    
    ExpressionList insertInformedConsentExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    Insert informedConsentInsert = new Insert();
    PacoId pacoId = new PacoId();
    if (informedConsent != null) {
      try {
        log.info("Inserting informed consent into informed consent table" + informedConsent.getExperimentId());
        conn = CloudSQLConnectionManager.getInstance().getConnection();
        conn.setAutoCommit(false);
        informedConsentInsert.setTable(new Table(InformedConsentColumns.TABLE_NAME));
        informedConsentInsert.setUseValues(true);
        insertInformedConsentExprList.setExpressions(exp);
        informedConsentInsert.setItemsList(insertInformedConsentExprList);
        informedConsentInsert.setColumns(informedConsentColList);
        // Adding ? for prepared stmt
        for (Column c : informedConsentColList) {
          ((ExpressionList) informedConsentInsert.getItemsList()).getExpressions().add(new JdbcParameter());
        }
        informedConsentId = IdGenerator.generate(new BigInteger(experimentVersion.toString()), 1);
        statementCreateInformedConsent = conn.prepareStatement(informedConsentInsert.toString());
        statementCreateInformedConsent.setLong(1, informedConsent.getExperimentId());
        statementCreateInformedConsent.setObject(2, informedConsentId, Types.BIGINT);
        statementCreateInformedConsent.setString(3, informedConsent.getInformedConsent());
        statementCreateInformedConsent.execute();
        
        conn.commit();
        pacoId.setId(informedConsentId.longValue());
        pacoId.setIsCreatedWithThisCall(true);
        informedConsent.setInformedConsentId(pacoId);
      } catch(SQLException sqle) {
        log.warning("Exception while inserting to informed_consent_history table:" +  sqle);
        throw sqle;
      }
      finally {
        try {
          if( rs != null) { 
            rs.close();
          }
          if (statementCreateInformedConsent != null) {
            statementCreateInformedConsent.close();
          }
          if (conn != null) {
            conn.close();
          }
        } catch (SQLException ex1) {
          log.info(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
        }
      }
    }
    log.info("returning consent id" + informedConsent);
  }
}
