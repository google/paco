package com.pacoapp.paco.shared.util;

public enum ErrorMessages {
  METHOD_NOT_SUPPORTED(10,"Method not supported"),
  JSON_PARSER_EXCEPTION(20, "Json parsing error"),
  JSON_GENERATION_EXCEPTION(21, "Json Generation exception"),
  JSON_MAPPING_EXCEPTION(22, "Json Mapping exception"),
  JSQL_PARSER_EXCEPTION(25, "Jsql parsing error"),
  PROBABLE_SQL_INJECTION(30, "Invalid query, probable sql injection : "),
  INVALID_DATA_TYPE(40, "Invalid datatype :"),
  INVALID_COLUMN_NAME(50,"Invalid Column name : "),
  INVALID_GROUPBY(51,"Invalid GroupBy : "),
  JSON_EXCEPTION(60, "Invalid json"),
  SQL_EXCEPTION(70,"SQL Exception : "),
  SQL_INSERT_EXCEPTION(71,"SQL Exception : "),
  TEXT_PARSE_EXCEPTION(80,"Text Parse exception : "),
  ADD_DEFAULT_COLUMN_EXCEPTION(90, "Unable to add default column "),
  EXPERIMENT_ID_CLAUSE_EXCEPTION(91, "Unauthorized access: Error in Experiment id clause"),
  QUERY_CRITERIA_EMPTY_EXCEPTION(92, "Query Criteria empty: Experiment id clause required"),
  UNAUTHORIZED_ACCESS(100,"Unauthorized access"),
  UNAUTHORIZED_ACCESS_MIXED_ACL(101,"Unauthorized access : Mixed ACL error"),
  CONVERT_TO_UTC(110, "Converting to UTC"),
  CLOSING_RESOURCE_EXCEPTION(120, "Exception while trying to close resources"),
  NOT_VALID_DATA(130," Not valid data"),
  UNKNOWN_TABLE_INDICATOR(135, "Unknown table Indicator : "),
  MISSING_APP_HEADER(140, "Attempt to access task handler directly - missing custom App Engine header"),
  DATASOURCE_SETUP_EXCEPTION(147, "DataSource setup"),
  INVALID_LIMIT_OFFSET(148, "Invalid Number format :"),
  EVENT_MIGRATION_EXCEPTION(149, "Event Migration Failed"),
  GENERAL_EXCEPTION(150, "General Exception"),
  IO_EXCEPTION(151, "IO Exception"),
  INVALID_ACCESS_OR_ARGUMENT_EXCEPTION(152, "Invalid access or Illegal arument");
  
  private final int code;
  private final String description;
  
  private ErrorMessages(int code, String description){
   this.code = code;
   this.description = description;
  }

  public int getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }
  
  public String toString(){
    return  code + " : " +description;
  }
}
