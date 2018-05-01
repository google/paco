package com.google.sampling.experiential.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.sampling.experiential.dao.dataaccess.ExperimentVersionMapping;
import com.google.sampling.experiential.model.What;
import com.pacoapp.paco.shared.model2.ExperimentDAO;

public interface CSExperimentVersionGroupMappingDao {
  void ensureExperimentVersionMapping(ExperimentDAO experimentDao) throws SQLException, Exception;
  Map<String, ExperimentVersionMapping> getAllGroupsInVersion(Long experimentId, Integer version) throws SQLException;
  boolean createExperimentVersionMapping(List<ExperimentVersionMapping> experimentVersionMappingLst) throws SQLException;
  Integer getNumberOfGroups(Long experimentId, Integer version) throws SQLException;
  void copyClosestVersion(Long experimentId, Integer fromVersion) throws SQLException;
  boolean updateEventsPosted(Long egvId) throws SQLException;
  boolean updateInputCollectionId(ExperimentVersionMapping evm, Long newInputCollectionId) throws SQLException;
  void addOnlyNewWhatsToInputCollection(ExperimentVersionMapping evm, List<String> inputsToBeAdded,
                                        boolean checkCollisionF) throws Exception;
  Integer getInputCollectionIdCountForExperiment(Long expId, Long icId) throws SQLException;
  boolean updateEventsPosted(Set<Long> egvmIds) throws SQLException;
  void ensureEVMRecord(Long experimentId, Long eventId, String experimentName,
                                           Integer experimentVersion, String groupName, String whoEmail,
                                           Set<What> whatSet, boolean migrationFlag,
                                           Map<String, ExperimentVersionMapping> allEVMsInDB) throws Exception;
}
