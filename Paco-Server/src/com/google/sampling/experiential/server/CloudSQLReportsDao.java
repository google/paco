package com.google.sampling.experiential.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.json.JSONException;

public interface CloudSQLReportsDao {
  List<String> getParticipants(Long experimentId) throws SQLException;
  String getCompleteStatus(String jobId, Long experimentId, String who) throws SQLException, JSONException, FileNotFoundException, IOException;
  String getQuickStatus(String jobId, Long experimentId, String who) throws SQLException, JSONException, FileNotFoundException, IOException;
}
