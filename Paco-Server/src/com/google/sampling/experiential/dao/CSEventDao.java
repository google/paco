package com.google.sampling.experiential.dao;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import com.google.sampling.experiential.model.Event;

public interface CSEventDao {

  boolean insertSingleEventOnly(Event event) throws SQLException, ParseException, NumberFormatException, Exception;

  boolean insertSingleEventOnlyWithExperimentInfo(Event event) throws SQLException, ParseException;

  void updateGroupName(List<Long> eventIdsToBeUpdatedWithNewGroupName, List<String> eventIdsOldGroupName,
                       String featureName) throws SQLException;
}
