package com.google.sampling.experiential.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.sampling.experiential.dao.dataaccess.ExperimentVersionGroupMapping;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.What;
import com.pacoapp.paco.shared.model2.ExperimentDAO;

public interface CSExperimentVersionGroupMappingDao {
  void ensureExperimentVersionGroupMapping(ExperimentDAO experimentDao) throws SQLException, Exception;
  Map<String, ExperimentVersionGroupMapping> getAllGroupsInVersion(Long experimentId, Integer version) throws SQLException;
  boolean createExperimentVersionGroupMapping(List<ExperimentVersionGroupMapping> experimentVersionMappingLst) throws SQLException;
  Integer getNumberOfGroups(Long experimentId, Integer version) throws SQLException;
  void createEVGMByCopyingFromLatestVersion(Long experimentId, Integer fromVersion) throws SQLException;
  boolean updateEventsPosted(Long egvId) throws SQLException;
  boolean updateInputCollectionId(ExperimentVersionGroupMapping evm, Long newInputCollectionId) throws SQLException;
  void addWhatsToInputCollection(ExperimentVersionGroupMapping evm, List<String> inputsToBeAdded,
                                        boolean includeOldOnes) throws Exception;
  Integer getInputCollectionIdCountForExperiment(Long expId, Long icId) throws SQLException;
  boolean updateEventsPosted(Set<Long> egvmIds) throws SQLException;
  void ensureEVMRecord(Long experimentId, Long eventId, String experimentName,
                                           Integer experimentVersion, String groupName, String whoEmail,
                                           Set<What> whatSet, boolean migrationFlag,
                                           Map<String, ExperimentVersionGroupMapping> allEVMsInDB) throws Exception;
  ExperimentVersionGroupMapping getEVGMId(Long experimentId, Integer experimentVersion,
                                       String groupName) throws SQLException;
  Long getNumberOfEvents(Long experimentgroupVersionMappingId, Integer anonWhoId, Long inputId) throws SQLException;
  ExperimentVersionGroupMapping findMatchingEVGMRecord(Event event, Map<String, ExperimentVersionGroupMapping> allEVMMap,
                                                      boolean migrationFlag) throws Exception;
  void ensureCorrectGroupName(Event eventDao, Map<String, ExperimentVersionGroupMapping> allEVMMap) throws Exception;
  void ensureSystemGroupName(Event eventDao, Map<String, ExperimentVersionGroupMapping> allEVMMap) throws Exception;
}
