package com.google.sampling.experiential.dao;

import java.sql.SQLException;
import java.util.List;

public interface CSPivotHelperDao {
  void incrementUpdateCtByOne(Long evmId, Integer anonWho, List<Long> inputIds) throws SQLException;
}
