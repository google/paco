package com.google.sampling.experiential.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.sampling.experiential.dao.dataaccess.ExperimentVersionMapping;
import com.google.sampling.experiential.model.What;
import com.google.sampling.experiential.server.PacoId;
import com.pacoapp.paco.shared.model2.ExperimentDAO;

public interface CSExperimentVersionMappingDao {
  void ensureExperimentVersionMapping(ExperimentDAO experimentDao) throws SQLException;
  Map<String, ExperimentVersionMapping> getAllGroupsInVersion(Long experimentId, Integer version) throws SQLException;
  boolean createExperimentVersionMapping(List<ExperimentVersionMapping> experimentVersionMappingLst);
  PacoId getExperimentVersionMappingId(Long experimentId, Integer version, String groupName) throws SQLException;
  Integer getNumberOfGroups(Long experimentId, Integer version) throws SQLException;
  void copyClosestVersion(Long experimentId, Integer fromVersion) throws SQLException;
  ExperimentVersionMapping prepareEVMForGroupWithInputs(Long experimentId, String experimentName,
                                                                    Integer experimentVersion, String groupName,
                                                                    String whoEmail,
                                                                    Set<What> whatSet, boolean migrationFlag) throws SQLException;
  boolean updateEventsPosted(Long egvId) throws SQLException;
}
