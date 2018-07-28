package com.google.sampling.experiential.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.google.sampling.experiential.dao.dataaccess.Input;
import com.google.sampling.experiential.dao.dataaccess.InputCollection;
import com.pacoapp.paco.shared.model2.ExperimentDAO;

public interface CSInputCollectionDao {
  
  void createInputCollectionId(ExperimentDAO exptDao,
                                                  Map<String, InputCollection> newVersion, Map<String, InputCollection> oldVersion) throws SQLException, Exception;
  void addInputToInputCollection(Long experimentId, Long inputCollectionId, Input input) throws SQLException;
  Input addUndefinedInputToCollection(Long experimentId, Long inputCollectionId, String variableName) throws SQLException, Exception;
  void addInputsToInputCollection(Long experimentId, InputCollection inputCollection, List<Input> inputs) throws SQLException;
  Long getInputCollectionId(Long experimentId, Integer experimentVersion, Integer numberOfGroups,
                            Boolean uniqueFlag) throws SQLException;
  List<Long> getAllInputIds(Long experimentId, Long inputCollectionId) throws SQLException;
  boolean deleteDupInputsInInputCollection(Long experimentId, List<Long> inputIds) throws SQLException;
  List<Long> getAllDupInputsForExperiment(Long experimentId) throws SQLException;
  boolean deleteInputCollectionInputAndChoiceCollection(Long experimentId) throws SQLException;
}
