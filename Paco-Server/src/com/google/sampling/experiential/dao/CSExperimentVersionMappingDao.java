package com.google.sampling.experiential.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.google.sampling.experiential.dao.dataaccess.ExperimentVersionMapping;
import com.google.sampling.experiential.dao.dataaccess.Input;
import com.google.sampling.experiential.server.PacoId;
import com.pacoapp.paco.shared.model2.ExperimentDAO;

public interface CSExperimentVersionMappingDao {
  void updateExperimentVersionMapping(ExperimentDAO experimentDao) throws SQLException;
  Map<String, ExperimentVersionMapping> getAllGroupsInVersion(Long experimentId, Integer version) throws SQLException;
  boolean createExperimentVersionMapping(List<ExperimentVersionMapping> experimentVersionMappingLst);
  PacoId getExperimentVersionMappingId(Long experimentId, Integer version, String groupName) throws SQLException;
  Integer getNumberOfGroups(Long experimentId, Integer version) throws SQLException;
  void copyClosestVersion(Long experimentId, Integer fromVersion) throws SQLException;
  ExperimentVersionMapping createGroupWithInputs(Long experimentId, String experimentName, Integer experimentVersion, String groupName,
                                                 String whoEmail, List<String> inputVariables) throws SQLException;
  
  ExperimentVersionMapping createGroupWithPredefinedInputs(Long experimentId, Integer experimentVersion,
                                                           String groupName, List<Input> inputs, String grouptypeName) throws SQLException;
  ExperimentVersionMapping createMappingForDeletedExperiment(Long experimentId,String experimentName, Integer experimentVersion,
                                                             String whoEmail, String groupName,
                                                             List<String> inputVariables) throws SQLException;
}
