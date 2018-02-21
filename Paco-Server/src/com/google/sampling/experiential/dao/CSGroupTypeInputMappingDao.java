package com.google.sampling.experiential.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.google.sampling.experiential.dao.dataaccess.GroupTypeInputMapping;
import com.google.sampling.experiential.dao.dataaccess.Input;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.GroupTypeEnum;

public interface CSGroupTypeInputMappingDao {
  void insertGroupTypeInputMapping(GroupTypeInputMapping predefinedInput) throws SQLException;
  Map<String, List<Input>> getAllFeatureInputs()  throws SQLException;
  ExperimentGroup createSystemExperimentGroupForGroupType(GroupTypeEnum groupType,
                                                    Boolean recordPhoneDetails) throws SQLException;
}
