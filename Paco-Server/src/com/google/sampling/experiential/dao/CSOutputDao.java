package com.google.sampling.experiential.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.google.sampling.experiential.shared.WhatDAO;

public interface CSOutputDao {
  Long getDistinctOutputCount(Long experimentId) throws SQLException;
  List<WhatDAO> getOutputs(Long eventId, Boolean populateEventsTableOldMethod) throws SQLException;
  boolean insertSingleOutput(Long eventId, Long inputId, String text, String answer,
                             Boolean oldColumnNames) throws SQLException;
  List<WhatDAO> getOutputsWithoutInputId(Connection conn, Long eventId) throws SQLException;
}
