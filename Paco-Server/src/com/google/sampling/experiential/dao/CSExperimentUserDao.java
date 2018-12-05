package com.google.sampling.experiential.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import com.google.sampling.experiential.server.PacoId;
import com.google.sampling.experiential.server.PacoUser;

public interface CSExperimentUserDao {

  PacoId getAnonymousIdAndCreate(Long experimentId, String email, boolean createOption) throws SQLException;

  List<PacoUser> getAllUsersForExperiment(Long experimentId) throws SQLException;

  Integer getMaxAnonId(Long expId) throws SQLException;

  void ensureUserId(Long expId, Set<String> adminEmailsInRequest, Set<String> participantEmailsInRequest);

  List<Integer> getAllAnonIdsForEVGMId(Long evgmId) throws SQLException;

}
