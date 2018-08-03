package com.google.sampling.experiential.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.google.sampling.experiential.dao.dataaccess.GroupTypeInputMapping;
import com.google.sampling.experiential.dao.dataaccess.Input;

public interface CSGroupTypeInputMappingDao {
  void insertGroupTypeInputMapping(GroupTypeInputMapping predefinedInput) throws SQLException;
  Map<String, List<Input>> getAllFeatureInputs()  throws SQLException;
  Map<String, List<String>> getAllPredefinedFeatureVariableNames()  throws SQLException;
}
