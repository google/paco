package com.pacoapp.paco.shared.util;


import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.pacoapp.paco.shared.model2.SQLQuery;

public class SearchUtil {
  
  private static final String BLANK = " ";
  private static final String DOT = ".";

  /**
   * 
   * @param eventsColumns
   *          This holds all the column names of the tables Events
   *          as keys and the associated table name as value
   * @param sqlQuery
   *          SQL query object that client intends to send to the back end
   * @return Table name that should be used for the given columns. 
   *         This will have the following two scenarios 
   *         - If all input columns are from Events table, will return Events table 
   *         - If any one of input columns is not from Events table, will
   *         return Eventsoutputs table
   */
//  public static String identifyTablesInvolved(Map<EventTableColumns, EventTableColumns> eventsColumns, SQLQuery sqlQuery) {
  public static String identifyTablesInvolved(Map<String, Integer> eventsColumns, SQLQuery sqlQuery) {
    
    // This method is not to validate the column names. This just helps identifying if we need to do a join on outputs table.
    //add all column names in the query->select columns, where clause, group by clause, having clause, sort order clause
    List<String> allColumns = Lists.newArrayList();
    allColumns.addAll(Arrays.asList(sqlQuery.getProjection()));
    String colNameConcat = (sqlQuery.getGroupBy() != null) ? sqlQuery.getCriteriaQuery().concat(BLANK).concat(sqlQuery.getGroupBy()) : sqlQuery.getCriteriaQuery();
    colNameConcat = (sqlQuery.getGroupBy() != null && sqlQuery.getHaving() != null) ? colNameConcat.concat(BLANK).concat(sqlQuery.getHaving()) : colNameConcat;
    colNameConcat = (sqlQuery.getSortOrder() != null)?colNameConcat.concat(BLANK).concat(sqlQuery.getSortOrder()) : colNameConcat;
    allColumns.addAll(aggregateExtractedColNames(colNameConcat));
    
    
    String tableIndicator = "events";
    if (allColumns != null && allColumns.size() > 0) {
      for (String s : allColumns) {
//        EventTableColumns evtTableCol = eventsColumns.get(s.toLowerCase());
        // if we do not get a match in Event column names, then we need to do a join
//        if (evtTableCol == null) {
        Integer colIndexInTable = eventsColumns.get(s.toUpperCase());
        // if we do not get a match in Event column names, then we need to do a join
        if (colIndexInTable == null) {
          tableIndicator = "eventsoutputs";
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
    System.out.println("after non word"+inputString);
    // replace logical operators and other key words
    inputString = inputString.replaceAll(" in |null|null | and | or | is | not | asc | desc | asc| desc", " ");
    System.out.println("after key word replace"+inputString);;
    
    inputString = inputString.replaceAll(" (?i)in |(?i)null|(?i)null | (?i)and | (?i)or | (?i)is | (?i)not | (?i)asc | (?i)desc | (?i)asc| (?i)desc", " ");
    System.out.println("after key word replace2"+inputString);;
    
    // multiple blank spaces get truncated to single blank space
    inputString = inputString.replaceAll("( )+", " ").trim();
    System.out.println("after multiple blank"+inputString);;
    
    String[] out = inputString.split(" ");
    return Arrays.asList(out);
  }
  
//  public static void addImplicitConditions(SQLQuery sqlQuery){
    
//    // provide default sort order which is Event._Id desc
//    if(sqlQuery.getSortOrder() == null){
//      sqlQuery.setSortOrder("event".concat(DOT).concat("idName").concat("DESC"));
//    }
//    
//    //add limit clause to sort order
//    if (sqlQuery.getLimit() != null) {
//      sqlQuery.setSortOrder(sqlQuery.getSortOrder().concat(" LIMIT ").concat(sqlQuery.getLimit()));
//    } 
    
//    //adding a default projection of event table primary key column
//    int crtLength = sqlQuery.getProjection().length ;
//    String[] modifiedProjection = new String[crtLength+1];
//    System.arraycopy(sqlQuery.getProjection(), 0, modifiedProjection, 0, crtLength);
//    //adding the following columns in the projection list to help in coalescing
//    modifiedProjection[crtLength]="event".concat(DOT).concat("idName");
//    sqlQuery.setProjection(modifiedProjection);
//  }
}

