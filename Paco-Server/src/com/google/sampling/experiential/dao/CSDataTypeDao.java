package com.google.sampling.experiential.dao;

import java.sql.SQLException;
import java.util.List;

import com.google.sampling.experiential.dao.dataaccess.DataType;

public interface CSDataTypeDao {
  List<DataType> getAllDataTypes() throws SQLException;
  // probably only a utility method, but for now, placing it here 
  DataType getMatchingDataType(List<DataType> fullList, String name, Boolean isNumeric, Boolean isMultiSelect);
}
