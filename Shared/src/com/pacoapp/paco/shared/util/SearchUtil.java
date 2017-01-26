package com.pacoapp.paco.shared.util;


import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;
import com.pacoapp.paco.shared.model2.SQLQuery;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

public class SearchUtil {
  
  /**
   * 
   * @param inputString
   *          contains all the columns names and with some sql key words
   *          depending upon the user query
   * @return the list of column names
   */
  public static void getColumnNames(String inputString, List<String> colNames) {
    if (inputString == null)
            inputString ="";
    inputString = inputString.replaceAll(" (?i)asc | (?i)desc | (?i)asc| (?i)desc|\\[|\\]", " ");
    // multiple blank spaces get truncated to single blank space
    inputString = inputString.replaceAll("( )+", " ").trim();
    String[] out = inputString.split(" ");
    colNames.addAll(Arrays.asList(out));
  }
  
  public static void getColumnNames(Expression node, List<String> colNames) {  
    
    if(node==null)  
     return;  
    if(node instanceof Parenthesis){
      node = ((Parenthesis) node).getExpression();
    } else if (node.getClass().getName().contains("Column")){
      colNames.add(node.toString());
    }
    if(node instanceof BinaryExpression){
      getColumnNames(((BinaryExpression)node).getLeftExpression(), colNames);
    }
   
    if(node instanceof BinaryExpression){
      getColumnNames(((BinaryExpression)node).getRightExpression(), colNames);
    }
    
    if(node instanceof InExpression){
      getColumnNames(((InExpression)node).getLeftExpression(), colNames);
    }
  }  
  
  public static void getColumnNamesInWhere(Expression node, List<String> colNames){
    getColumnNames(node, colNames);
  }
  
  public static void getColumnNamesInHaving(Expression node, List<String> colNames){
    getColumnNames(node, colNames);
  }
  
  public static void getColumnNamesInSortOrder(String clause,  List<String> colNames){
    getColumnNames(clause, colNames);
  }
  public static void getColumnNamesInGroupBy(String clause,  List<String> colNames){
    getColumnNames(clause, colNames);
  }
  
  public static List<String> getAllColNamesInQuery(String selectSql){
    net.sf.jsqlparser.statement.Statement statement = null;
    try {
      statement = CCJSqlParserUtil.parse(selectSql);
    } catch (JSQLParserException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    List<String> colNames = Lists.newArrayList(); 
    
    Select selectStatement = (Select) statement;
    PlainSelect pl = (PlainSelect)selectStatement.getSelectBody();
    BinaryExpression where = (BinaryExpression) pl.getWhere();
    getColumnNamesInWhere(where, colNames);
    BinaryExpression having = (BinaryExpression) pl.getHaving();
    getColumnNamesInHaving(having, colNames);
    if(pl.getOrderByElements()!=null)
      getColumnNamesInSortOrder(pl.getOrderByElements().toString(), colNames);
    if(pl.getGroupByColumnReferences()!=null)
      getColumnNamesInGroupBy(pl.getGroupByColumnReferences().toString(), colNames);
    System.out.println("all cols new approach"+colNames);
    return colNames;
    
  }
  
  public static  String identifyTablesInvolved(List<String> colNamesInQuery){
    if(colNamesInQuery.contains("text") || colNamesInQuery.contains("answer")){
      return "eventsoutputs";
    }else{
      return "events";
    }
      
  }
  
  public static  String getPlainSql(SQLQuery sqlQuery) {

  //where group having order, limit
  StringBuffer sqlString = new StringBuffer("");
  
  sqlString.append("Select ");

  if(sqlQuery.getProjection()!=null){
//    TODO : Should we add apache utils to shared
//    String proj = StringUtils.join(sqlQuery.getProjection(),",");
//    sqlString.append(proj);
    String[] proj = sqlQuery.getProjection();
    StringBuffer colNames = new StringBuffer("");
    for(int i =0; i<proj.length;i++){
      if(i==proj.length-1){
        colNames.append(proj[i]);
      }else{
        colNames.append(proj[i]).append(",");
      }
    }
    sqlString.append(colNames);
  }
 
  sqlString.append( " from events ");

  if(sqlQuery.getCriteriaQuery()!=null){
    String[] repl = sqlQuery.getCriteriaValue();
    String x= sqlQuery.getCriteriaQuery();
    int i=0;
    while (x.contains("?")){
      x = x.replaceFirst("\\?", repl[i++]);
    }
        
    sqlString.append( " where ");
    sqlString.append(x);
  }
  
  if(sqlQuery.getGroupBy()!=null){
    sqlString.append( " group by ");
    sqlString.append(sqlQuery.getGroupBy());

    if(sqlQuery.getHaving()!=null){
      sqlString.append( " having ");
      sqlString.append(sqlQuery.getHaving());
    }
    
  }
  
  if(sqlQuery.getSortOrder()!=null){
    sqlString.append( " order by ");
    sqlString.append(sqlQuery.getSortOrder());
  }
  
//TODO Limit is getting added to sort order because sql lite helper api's 
//  does not allow direct limit variables. Should we continue that for server 
//  even though we do not need it. Should we handle client limitations to the client side alone
//  
//  if(sqlQuery.getLimit()!=null){
//    sqlString.append( " limit ");
//    sqlString.append(sqlQuery.getLimit());
//  }
  
  System.out.println(sqlString);
  
  return sqlString.toString();
}

}

