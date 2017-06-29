package com.google.sampling.experiential.server.migration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONException;

public interface StoredProc {

  public String callStoredProc(String jobId, Long experimentId, String who) throws SQLException, JSONException, FileNotFoundException, IOException;

}