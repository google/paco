package com.google.sampling.experiential.server;

import java.sql.SQLException;
import java.util.List;

import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.shared.EventDAO;

public interface CloudSQLDao {
  boolean insertEvent(Event e) throws SQLException;
  List<EventDAO> getEvents(String query) throws SQLException;
  String createTables() throws SQLException;
}
