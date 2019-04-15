package com.google.sampling.experiential.dao;

import java.sql.SQLException;
import java.util.List;

import com.google.sampling.experiential.dao.dataaccess.PivotHelper;

public interface CSPivotHelperDao {
  void incrementUpdateCtByOne(Long evmId, Integer anonWho, List<Long> inputIds) throws SQLException;
  void insertPivotHelper(List<PivotHelper> pvList) throws SQLException;
  void updatePivotHelper(List<PivotHelper> pvList) throws SQLException;
  void insertIgnorePivotHelper(List<PivotHelper> pvList) throws SQLException;
}
