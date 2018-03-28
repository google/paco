package com.google.sampling.experiential.dao;

import java.sql.SQLException;
import java.util.List;

import com.google.sampling.experiential.shared.WhatDAO;

public interface CSOutputDao {
  boolean insertSingleOutput(Long eventId, String text, String answer) throws SQLException;
  List<WhatDAO> getOutputs(Long eventId) throws SQLException;
}
