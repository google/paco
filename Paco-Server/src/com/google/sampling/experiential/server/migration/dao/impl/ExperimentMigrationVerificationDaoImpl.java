package com.google.sampling.experiential.server.migration.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.cloudsql.columns.ExternStringInputColumns;
import com.google.sampling.experiential.dao.CSTempExperimentIdVersionGroupNameDao;
import com.google.sampling.experiential.dao.dataaccess.ExperimentLite;
import com.google.sampling.experiential.dao.impl.CSTempExperimentIdVersionGroupNameDaoImpl;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.ExceptionUtil;
import com.google.sampling.experiential.server.migration.dao.ExperimentMigrationVerificationDao;

public class ExperimentMigrationVerificationDaoImpl implements ExperimentMigrationVerificationDao {
  public static final Logger log = Logger.getLogger(ExperimentMigrationVerificationDaoImpl.class.getName());
  final String EXP_ID_VERIFICATION_QUERY = "select e.experiment_id_old, egvm.experiment_id, e.experiment_id_old - egvm.experiment_id  from events e"
          + " join experiment_version_group_mapping egvm  on e.experiment_version_group_mapping_id = egvm.experiment_version_group_mapping_id "
          + " where (e.experiment_id_old - egvm.experiment_id) != 0 and egvm.experiment_id=?";
  final String EXP_NAME_VERIFICATION_QUERY = "select e.experiment_id_old, e.experiment_name_old, e.experiment_id_old, ed.experiment_detail_id,ed.experiment_name  from events e "
          + " join experiment_version_group_mapping egvm  on e.experiment_version_group_mapping_id = egvm.experiment_version_group_mapping_id  "
          + " join experiment_detail ed on egvm.experiment_detail_id=ed.experiment_detail_id "
          + " where e.experiment_name_old != ed.experiment_name and egvm.experiment_id=? ";
  final String EXP_VERSION_VERIFICATION_QUERY = "select e.experiment_id_old, e.experiment_version_old, egvm.experiment_version, e.experiment_version_old - egvm.experiment_version  from events e "
          + " join experiment_version_group_mapping egvm  on e.experiment_version_group_mapping_id = egvm.experiment_version_group_mapping_id "
          + " where (e.experiment_version_old - egvm.experiment_version) != 0 and egvm.experiment_id=?";
  final String GROUP_NAME_VERIFICATION_QUERY = "select e.experiment_id_old, e.experiment_name_old, e.group_name_old, gd.group_name from events e "  
          + " join experiment_version_group_mapping egvm  on e.experiment_version_group_mapping_id = egvm.experiment_version_group_mapping_id "  
          + " join group_detail gd on egvm.group_detail_id=gd.group_detail_id  "
          + " where e.group_name_old != gd.group_name and egvm.experiment_id=?";
  final String ANON_WHO_VERIFICATION_QUERY = "select e._id, e.who_old eventwhoemail, e.who eventwhoanonid, eu.experiment_user_anon_id,u.who from events e " 
          + " join experiment_version_group_mapping egvm  on e.experiment_version_group_mapping_id = egvm.experiment_version_group_mapping_id "  
          + " join experiment_detail ed on egvm.experiment_detail_id=ed.experiment_detail_id "
          + " left join experiment_user eu on egvm.experiment_id=eu.experiment_id and e.who=eu.experiment_user_anon_id "
          + " left join user u on u.user_id=eu.user_id "
          + " where (u.who is null or eu.experiment_user_anon_id is null or e.who_old!=u.who)  and egvm.experiment_id=?";
  final String INPUT_NAME_VERIFICATION_QUERY = " select e._id, o.text, i.input_id,o.input_id,o.answer, esi."+ ExternStringInputColumns.LABEL +" from events e "  
          + " join outputs o on e._id=o.event_id join experiment_version_group_mapping egvm on e.experiment_version_group_mapping_id=egvm.experiment_version_group_mapping_id "  
          + " join input_collection ic on egvm.input_collection_id=ic.input_collection_id and egvm.experiment_id=ic.experiment_ds_id  "
          + " join input i on ic.input_id=i.input_id and o.input_id=i.input_id  " 
          + " join " + ExternStringInputColumns.TABLE_NAME + " esi on i.name_id=esi.extern_string_input_id "  
          + " where o.text != esi."+ ExternStringInputColumns.LABEL +" and egvm.experiment_id=?";
  
  private boolean genericVerification (String query) throws SQLException {
    Connection conn = null;
    PreparedStatement statementVerifyExperimentMigration = null;
    CSTempExperimentIdVersionGroupNameDao eivgDaoImpl = new CSTempExperimentIdVersionGroupNameDaoImpl();
    ResultSet rs = null;
    boolean successFlag = true;
    Integer status = 4;
    List<Long> verifiedExperimentList = Lists.newArrayList();
    try { 
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementVerifyExperimentMigration = conn.prepareStatement(query);
      List<ExperimentLite> expLiteLst = eivgDaoImpl.getDistinctExperimentIdAndVersion(status);
      
      for (ExperimentLite expLiteObj : expLiteLst) { 
        if (!verifiedExperimentList.contains(expLiteObj.getExperimentId())) {
          statementVerifyExperimentMigration.setLong(1, expLiteObj.getExperimentId());
          rs = statementVerifyExperimentMigration.executeQuery();
          while (rs.next()) { 
            log.warning("Id"  + rs.getLong(1)  + "--" + rs.getObject(2));
            successFlag = false;
          } 
          if (successFlag) {
            log.info("verified:" + expLiteObj.getExperimentId());
          }
          verifiedExperimentList.add(expLiteObj.getExperimentId());
        } 
      }
      
    } catch (SQLException sqle) {
      log.warning("Catch all - sqle"+ExceptionUtil.getStackTraceAsString(sqle));
      throw sqle;
      
    } catch (Exception e) { 
      log.warning("Catch all - ge"+ExceptionUtil.getStackTraceAsString(e));
      throw e;
    }
    return successFlag;
  }
    
  @Override
  public boolean verifyExperimentId() throws SQLException {
    return genericVerification(EXP_ID_VERIFICATION_QUERY);
  }
  
  @Override
  public boolean verifyExperimentName() throws SQLException {
//    return genericVerification(EXP_NAME_VERIFICATION_QUERY);
    return true;
  }
  
  @Override
  public boolean verifyExperimentVersion() throws SQLException {
    return genericVerification(EXP_VERSION_VERIFICATION_QUERY);
  }
  
  @Override
  public boolean verifyGroupName() throws SQLException, Exception {
    return genericVerification(GROUP_NAME_VERIFICATION_QUERY);
  }
  
  @Override
  public boolean verifyAnonWho() throws SQLException {
    return genericVerification(ANON_WHO_VERIFICATION_QUERY);
  }
  
  @Override
  public boolean verifyInputId() throws SQLException {
    return genericVerification(INPUT_NAME_VERIFICATION_QUERY);
  }  
}
