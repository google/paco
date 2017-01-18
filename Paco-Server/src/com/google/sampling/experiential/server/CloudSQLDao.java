package com.google.sampling.experiential.server;

import java.util.List;

import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.Output;

public interface CloudSQLDao {
  void insertEvent(EventDAO e);
  void insertOutputs(EventDAO e);
  List<Output> getOutputs(Long eventId);
  List<EventDAO> getEvents(String query);
  String createTables();
}
