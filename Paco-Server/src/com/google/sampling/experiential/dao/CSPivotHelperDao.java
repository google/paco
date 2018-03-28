package com.google.sampling.experiential.dao;

import java.sql.SQLException;
import java.util.List;

import com.google.sampling.experiential.dao.dataaccess.ExperimentVersionMapping;
import com.google.sampling.experiential.dao.dataaccess.PivotHelper;
import com.google.sampling.experiential.server.PacoId;

public interface CSPivotHelperDao {
  void insertPivotHelper(List<PivotHelper> pvList) throws SQLException;
  boolean updatePivotHelperStatus(Long evmId, Integer anonWho, Long inputId, Long updateCt) throws SQLException;
  void incrementUpdateCtByOne(Long evmId, Integer anonWho, List<Long> inputIds) throws SQLException;
  boolean getPivotHelper(Long evmId, Integer anonWho, Long inputId) throws SQLException;
  List<PivotHelper> convertToPivotHelper(ExperimentVersionMapping matchingEVMRecord, PacoId anonWhoId);
}
