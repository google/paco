package com.google.sampling.experiential.dao;

import java.sql.SQLException;
import java.util.List;

import com.google.sampling.experiential.dao.dataaccess.Input;

public interface CSInputDao {
  void insertInput(Input input) throws SQLException;

  void insertInput(List<Input> inputs) throws SQLException;

  List<Input> insertVariableNames(List<String> variableNames) throws SQLException;
 
}
