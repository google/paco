package com.google.sampling.experiential.dao;

import java.sql.SQLException;

import com.google.sampling.experiential.dao.dataaccess.Input;

public interface CSInputDao {
  void insertInput(Input input) throws SQLException;
}
