package com.google.sampling.experiential.dao;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import com.google.sampling.experiential.server.PacoId;

public interface CSUserDao {

  PacoId getUseridAndCreate(String email, boolean createOption) throws SQLException;

  Long insertUserAndRetrieveId(String email) throws SQLException;

  Map<String, Long> getUserIdsForEmails(Set<String> userEmailLst) throws SQLException;

}
