package com.google.sampling.experiential.dao;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import com.google.sampling.experiential.dao.dataaccess.User;

public interface CSUserDao {

  User getUserAndCreate(String email, boolean createOption) throws SQLException;

  Map<String, Long> getUserIdsForEmails(Set<String> userEmailLst) throws SQLException;

}
