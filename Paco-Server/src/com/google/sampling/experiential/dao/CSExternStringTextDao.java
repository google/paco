package com.google.sampling.experiential.dao;

import java.sql.SQLException;

import com.google.sampling.experiential.server.PacoId;

public interface CSExternStringTextDao {
  PacoId getTextAndCreate(String label, boolean createOption) throws SQLException;
}
