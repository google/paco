package com.google.sampling.experiential.dao;

import java.sql.SQLException;
import java.util.List;

import com.google.sampling.experiential.dao.dataaccess.GroupDetail;

public interface CSGroupDao {
  void insertGroup(List<GroupDetail> groups) throws SQLException;
}
