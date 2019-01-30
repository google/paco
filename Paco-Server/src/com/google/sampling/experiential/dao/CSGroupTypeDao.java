package com.google.sampling.experiential.dao;

import java.sql.SQLException;

public interface CSGroupTypeDao {
  Integer getGroupTypeId(String feature) throws SQLException;
  

}
