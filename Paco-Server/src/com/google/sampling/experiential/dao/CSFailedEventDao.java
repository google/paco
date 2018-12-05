package com.google.sampling.experiential.dao;

import java.sql.SQLException;
import java.util.Map;

public interface CSFailedEventDao {

  boolean insertFailedEvent(String failedJson, String reason, String comments);

  Map<Long, String> getFailedEvents() throws SQLException;

  boolean updateFailedEventsRetry(Long id, String reprocessed) throws SQLException;

}
