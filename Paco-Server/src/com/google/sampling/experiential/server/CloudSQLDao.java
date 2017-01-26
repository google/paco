package com.google.sampling.experiential.server;

import java.util.List;

import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.Output;

public interface CloudSQLDao {
  void insertEvent(Event e);
  void insertOutputs(Event e);
  List<Output> getOutputs(Long eventId);
  List<EventDAO> getEvents(String query);
  String createTables();
}
