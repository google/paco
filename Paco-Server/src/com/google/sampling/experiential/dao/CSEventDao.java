package com.google.sampling.experiential.dao;

import java.sql.SQLException;
import java.text.ParseException;

import com.google.sampling.experiential.model.Event;

public interface CSEventDao {

  boolean insertSingleEventOnly(Event event) throws SQLException, ParseException, NumberFormatException, Exception;

  boolean insertSingleEventOnlyWithExperimentInfo(Event event) throws SQLException, ParseException;

  boolean updateGroupName(Long eventId, String oldGrpName, String newGrpName) throws SQLException;

}
