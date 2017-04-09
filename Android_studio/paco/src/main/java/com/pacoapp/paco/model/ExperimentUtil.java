package com.pacoapp.paco.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ExperimentUtil {
  /**
   * 
   * @param eventsColumns
   *          This holds all the column names of the tables Events
   *          as keys and the associated table name as value
   * @param colNames
   *          List of names that the client has asked for in the query
   * @return Table name that should be used for the given columns. 
   *         This will have the following two scenarios 
   *         - If all input columns are from Events table, will return Events table 
   *         - If any one of input columns is not from Events table, will
   *         return Eventsoutputs table
   */
  public static String identifyTablesInvolved(Map<String, String> eventsColumns, List<String> colNames) {
    // This method is not to validate the column names. This just helps identifying if we need to do a join on outputs table.
    String tableIndicator = ExperimentProvider.EVENTS_TABLE_NAME;
    if (colNames != null && colNames.size() > 0) {
      for (String s : colNames) {
        String crTableName = eventsColumns.get(s.toUpperCase());
        // if we do not get a match in Event column names, then we need to do a join
        if (crTableName == null) {
          tableIndicator = ExperimentProvider.EVENTS_OUTPUTS_TABLE_NAME;
          return tableIndicator;
        }
      }
    }
   return tableIndicator;
  }

  /**
   * 
   * @param inputString
   *          contains all the columns names and with some sql key words
   *          depending upon the user query
   * @return the list of column names
   */
  public static List<String> aggregateExtractedColNames(String inputString) {
    // any non word character or the words 'and' 'or' 'is' 'not' get replaced
    // with blank
    inputString = inputString.replaceAll("\\W+", " ");
    // replace logical operators and other key words
    inputString = inputString.replaceAll(" and | or | is | not | asc | desc | asc| desc", " ");
    // multiple blank spaces get truncated to single blank space
    inputString = inputString.replaceAll("( )+", " ").trim();
    String[] out = inputString.split(" ");
    return Arrays.asList(out);
  }
}
