package com.google.sampling.experiential.dao;

import java.sql.SQLException;
import java.util.List;

import com.google.sampling.experiential.dao.dataaccess.Input;

public interface CSInputDao {
  void insertInput(Input input) throws SQLException, Exception;

  void insertInput(List<Input> inputs) throws SQLException, Exception;

  List<Input> insertVariableNames(List<String> variableNames) throws SQLException, Exception;

  boolean deleteAllInputs(List<Long> inputIds) throws SQLException;

  String getLabelForInputId(Long inputId) throws SQLException;
 
}
