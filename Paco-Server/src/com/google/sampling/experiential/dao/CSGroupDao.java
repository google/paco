package com.google.sampling.experiential.dao;

import java.sql.SQLException;
import java.util.List;

import com.google.sampling.experiential.dao.dataaccess.Group;

public interface CSGroupDao {
  void insertGroup(List<Group> groups) throws SQLException;
}
