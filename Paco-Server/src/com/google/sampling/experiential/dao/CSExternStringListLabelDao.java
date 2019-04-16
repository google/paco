package com.google.sampling.experiential.dao;

import java.sql.SQLException;

import com.google.sampling.experiential.server.PacoId;

public interface CSExternStringListLabelDao {
  PacoId getListLabelAndCreate(String label, boolean createOption) throws SQLException;
}
