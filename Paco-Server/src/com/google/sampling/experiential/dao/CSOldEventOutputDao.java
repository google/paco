package com.google.sampling.experiential.dao;

import java.sql.SQLException;
import java.text.ParseException;

import com.google.sampling.experiential.model.Event;

public interface CSOldEventOutputDao {

  boolean insertEventAndOutputsInOldWay(Event event) throws SQLException, ParseException, Exception;
}
