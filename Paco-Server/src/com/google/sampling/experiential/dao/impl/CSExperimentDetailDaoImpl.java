package com.google.sampling.experiential.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.logging.Logger;

import com.google.cloud.sql.jdbc.Statement;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.cloudsql.columns.ExperimentDetailColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentVersionGroupMappingColumns;
import com.google.sampling.experiential.dao.CSExperimentDetailDao;
import com.google.sampling.experiential.dao.dataaccess.ExperimentDetail;
import com.google.sampling.experiential.dao.dataaccess.InformedConsent;
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

public class CSExperimentDetailDaoImpl implements CSExperimentDetailDao {
  public static final Logger log = Logger.getLogger(CSExperimentDetailDaoImpl.class.getName());
  private static List<Column> experimentColList = Lists.newArrayList();
  
  static {
    experimentColList.add(new Column(ExperimentDetailColumns.EXPERIMENT_NAME));
    experimentColList.add(new Column(ExperimentDetailColumns.DESCRIPTION));
    experimentColList.add(new Column(ExperimentDetailColumns.CREATOR));
    experimentColList.add(new Column(ExperimentDetailColumns.ORGANIZATION));
    experimentColList.add(new Column(ExperimentDetailColumns.CONTACT_EMAIL));
    experimentColList.add(new Column(ExperimentDetailColumns.INFORMED_CONSENT_ID));
    experimentColList.add(new Column(ExperimentDetailColumns.DELETED));
    experimentColList.add(new Column(ExperimentDetailColumns.MODIFIED_DATE));
    experimentColList.add(new Column(ExperimentDetailColumns.PUBLISHED));
    experimentColList.add(new Column(ExperimentDetailColumns.RINGTONE_URI));
    experimentColList.add(new Column(ExperimentDetailColumns.POST_INSTALL_INSTRUCTIONS));
  }
  
  @Override
  public void insertExperimentDetail(ExperimentDetail experiment) throws SQLException {
    Connection conn = null;
    PreparedStatement statementCreateExperiment = null;
    ResultSet rs = null;
    Long experimentId = null;
    ExpressionList insertExperimentExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    Insert experimentInsert = new Insert();
    PacoId expId = new PacoId();
    if (experiment != null) {
      try {
        conn = CloudSQLConnectionManager.getInstance().getConnection();
        conn.setAutoCommit(false);

        experimentInsert.setTable(new Table(ExperimentDetailColumns.TABLE_NAME));
        experimentInsert.setUseValues(true);
        insertExperimentExprList.setExpressions(exp);
        experimentInsert.setItemsList(insertExperimentExprList);
        experimentInsert.setColumns(experimentColList);
        // Adding ? for prepared stmt
        for (Column c : experimentColList) {
          ((ExpressionList) experimentInsert.getItemsList()).getExpressions().add(new JdbcParameter());
        }
       
        statementCreateExperiment = conn.prepareStatement(experimentInsert.toString(), Statement.RETURN_GENERATED_KEYS);
        statementCreateExperiment.setString(1, experiment.getTitle());
        String expDesc = experiment.getDescription();
        if (expDesc != null && expDesc.length() >= 2500) {
          log.warning("exp desc is :" + expDesc.length());
          expDesc = experiment.getDescription().substring(0,2500);
        }
        
        statementCreateExperiment.setString(2, expDesc);
        statementCreateExperiment.setLong(3, experiment.getCreator().getUserId().getId());
        statementCreateExperiment.setString(4, experiment.getOrganization());
        statementCreateExperiment.setString(5, experiment.getContactEmail());
        InformedConsent ic = experiment.getInformedConsent();
        statementCreateExperiment.setObject(6, ic != null ? ic.getInformedConsentId().getId() : null, Types.BIGINT);
        statementCreateExperiment.setBoolean(7, experiment.isDeleted());
        statementCreateExperiment.setTimestamp(8, experiment.getModifiedDate() != null ? new Timestamp(experiment.getModifiedDate().getMillis()) : null);
        statementCreateExperiment.setBoolean(9, experiment.isPublished());
        statementCreateExperiment.setString(10, experiment.getRingtoneUri());
        statementCreateExperiment.setString(11, experiment.getPostInstallInstructions());
        statementCreateExperiment.execute();
        rs = statementCreateExperiment.getGeneratedKeys();
        if (rs.next()) {
          experimentId = rs.getLong(1);
        }
        expId.setId(experimentId);
        expId.setIsCreatedWithThisCall(true);
        experiment.setExperimentDetailId(expId);
        conn.commit();
      } catch(SQLException sqle) {
        log.warning("Exception while inserting to experiment table" + experiment.getTitle() + ":" +  sqle);
        throw sqle;
      }
      finally {
        try {
          if( rs != null) { 
            rs.close();
          }
          if (statementCreateExperiment != null) {
            statementCreateExperiment.close();
          }
          if (conn != null) {
            conn.close();
          }
        } catch (SQLException ex1) {
          log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
        }
      }
    } else {
      log.warning("insert experiment failed" + experiment);
    }
  }

  @Override
  public Long getExperimentDetailId(Long expId, Integer expVersion) throws SQLException {
    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement statementGetExperimentInfo = null;
    Long expFacetId = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementGetExperimentInfo = conn.prepareStatement(QueryConstants.GET_EXPERIMENT_DETAIL_ID.toString());
      statementGetExperimentInfo.setLong(1, expId);
      statementGetExperimentInfo.setInt(2, expVersion);
      rs = statementGetExperimentInfo.executeQuery();
      while (rs.next()) {
        expFacetId = rs.getLong(ExperimentVersionGroupMappingColumns.EXPERIMENT_DETAIL_ID);
      }
    } finally {
      try {
        if ( rs != null) {
          rs.close();
        }
        if (statementGetExperimentInfo != null) {
          statementGetExperimentInfo.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
        throw ex1;
      }
    }
    return expFacetId;
  }
}
