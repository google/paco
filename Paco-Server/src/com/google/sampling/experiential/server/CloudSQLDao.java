package com.google.sampling.experiential.server;

import java.util.List;

import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.Output;
import com.pacoapp.paco.shared.model2.SQLQuery;

public interface CloudSQLDao {
  void insertEvent(EventDAO e);
  void insertOutputs(EventDAO e);
  List<Output> getOutputs(Long eventId);
  String getPlainSql(SQLQuery sqlQuery);
  List<EventDAO> getEvents(String query, String whereClause, String[] criValues);
}
