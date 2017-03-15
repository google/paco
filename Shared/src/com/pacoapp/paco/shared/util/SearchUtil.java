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
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.SelectUtils;

public class SearchUtil {

  public static void getColumnNames(Expression node, List<String> colNames) {

    if (node == null)
      return;
    if (node instanceof Parenthesis) {
      node = ((Parenthesis) node).getExpression();
    } else if (node.getClass().getName().contains("Column")) {
      colNames.add(node.toString());
    }
    if (node instanceof BinaryExpression) {
      getColumnNames(((BinaryExpression) node).getLeftExpression(), colNames);
    }

    if (node instanceof BinaryExpression) {
      getColumnNames(((BinaryExpression) node).getRightExpression(), colNames);
    }

    if (node instanceof InExpression) {
      getColumnNames(((InExpression) node).getLeftExpression(), colNames);
    }
  }

  public static List<String> retrieveUserSpecifiedConditions(Statement selectStatement, String colName) {
    PlainSelect pl = (PlainSelect) ((Select) selectStatement).getSelectBody();
    List<String> qvList = Lists.newArrayList();
    getQueriedValue(pl.getWhere(), colName, qvList);
    return qvList;
  }

  public static void getQueriedValue(Expression node, String columnName, List<String> queriedValueList) {

    if (node == null)
      return;
    if (node instanceof Parenthesis) {
      node = ((Parenthesis) node).getExpression();
    }

    if (node instanceof BinaryExpression) {
      Expression le = ((BinaryExpression) node).getLeftExpression();
      Expression re = ((BinaryExpression) node).getRightExpression();

      if ((le.getClass().getName().contains("Column")) && le.toString().equalsIgnoreCase(columnName)) {
        queriedValueList.add(((BinaryExpression) node).getRightExpression().toString());
      } else {
        getQueriedValue(le, columnName, queriedValueList);
        getQueriedValue(re, columnName, queriedValueList);

      }
    }

    if (node instanceof InExpression) {
      if ((((InExpression) node).getLeftExpression().getClass().getName().contains("Column"))
          && node.toString().contains(columnName)) {
        String listWithParen = ((InExpression) node).getRightItemsList().toString().replace('(', ' ');
        String listWithoutParen = listWithParen.replace(')', ' ');
        String[] arr = listWithoutParen.split(", ");
        queriedValueList.addAll(Arrays.asList(arr));
      }
    }
    if (node instanceof IsNullExpression) {
      if ((((IsNullExpression) node).getLeftExpression().getClass().getName().contains("Column"))
          && node.toString().contains(columnName)) {
        queriedValueList.add("isnull");
      }
    }

  }

  public static Statement getJsqlStatement(String selectSql) {
    net.sf.jsqlparser.statement.Statement statement = null;
    try {
      statement = CCJSqlParserUtil.parse(selectSql);
    } catch (JSQLParserException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return statement;
  }

  public static List<String> getAllColNamesInQuery(String selectSql) {
    List<String> colList = Lists.newArrayList();
    Statement statement = getJsqlStatement(selectSql);
    if (statement instanceof Select) {
      Select selectStatement = (Select) statement;
      ColumnNamesFinder colNamesFinder = new ColumnNamesFinder();
      colList = colNamesFinder.getColumnList(selectStatement);
    }
    return colList;
  }

  public static String identifyTablesInvolved(List<String> colNamesInQuery) {
    if (colNamesInQuery.contains("text") || colNamesInQuery.contains("answer")) {
      return "eventsoutputs";
    } else {
      return "events";
    }
  }

  public static String getPlainSql(SQLQuery sqlQuery) {
    Select selQry = null;
    Expression whereExpr = null;
    List<OrderByElement> orderByList = null;
    List<Expression> groupBy = null;
    Limit limit = null;
 
    try{
      // projection
      if (sqlQuery.getProjection() != null) {
        selQry = SelectUtils.buildSelectFromTableAndExpressions(new Table("events"), sqlQuery.getProjection());
      }
      
      // where clause
      if(sqlQuery.getCriteriaQuery()!=null){
        //TODO replace with jsql utility
        String[] repl = sqlQuery.getCriteriaValue();
        String tempWhereClause = sqlQuery.getCriteriaQuery();
        int i = 0;
        while (tempWhereClause.contains("?")) {
          tempWhereClause = tempWhereClause.replaceFirst("\\?", repl[i++]);
        }
        whereExpr = CCJSqlParserUtil.parseCondExpression(tempWhereClause);
      }
     
      // groupBy
      if(sqlQuery.getGroupBy() != null){
        groupBy = convertToExpressionList(sqlQuery.getGroupBy());
      }
  
      // orderBy
      if(sqlQuery.getSortOrder() != null) {
        orderByList = convertToOrderByList(sqlQuery.getSortOrder());
      }
      
      //limit
      if(sqlQuery.getLimit() != null){
        limit = new Limit();
        String[] splitLimitOffset = sqlQuery.getLimit().split(",");
        
        limit.setRowCount(Long.parseLong(splitLimitOffset[0]));
        if(splitLimitOffset.length>1) {
          limit.setOffset(Long.parseLong(splitLimitOffset[1]));
        }
      }
    }catch(JSQLParserException js){
      
    }
     
    PlainSelect plainSel = (PlainSelect) selQry.getSelectBody();
    plainSel.setWhere(whereExpr);
    plainSel.setOrderByElements(orderByList);
    plainSel.setGroupByColumnReferences(groupBy);
    plainSel.setLimit(limit);
    
    System.out.println("jsql new qry "+plainSel.toString());
    return plainSel.toString();

  }
  
  public static String getTableIndicator(SQLQuery sqlQuery){
    String plainSql = getPlainSql(sqlQuery);
    return getTableIndicator(sqlQuery, plainSql);
  }
  
  public static String getTableIndicator(SQLQuery sqlQuery, String plainSql){
    List<String> colNamesInQuery = getAllColNamesInQuery(plainSql);
    String tablesInvolved = identifyTablesInvolved(colNamesInQuery);
    return tablesInvolved;
  }
  
  public static PlainSelect getJoinQry(SQLQuery sqlQuery){
    PlainSelect ps = null;
    Expression joinExp = null;
    Select selStatement = null;
    List<Join> jList = Lists.newArrayList();
    Join joinObj = new Join();
    FromItem ft = new Table("outputs");

    String plainSql = getPlainSql(sqlQuery);
    Statement jsqlStmt = getJsqlStatement(plainSql);
    if (jsqlStmt instanceof Select) {
      selStatement = (Select) jsqlStmt;
    }
    String tableIndicator = getTableIndicator(sqlQuery, plainSql);
    if (tableIndicator != null && tableIndicator.equals("eventsoutputs")) {
      try {
        joinExp = CCJSqlParserUtil.parseCondExpression("events._id = outputs.event_id");
      } catch (JSQLParserException e) {
        e.printStackTrace();
      }
      joinObj.setOnExpression(joinExp);
      joinObj.setInner(true);
      joinObj.setRightItem(ft);
      jList.add(joinObj);
      ps = ((PlainSelect)selStatement.getSelectBody());
      ps.setJoins(jList);
    } else {
      ps = ((PlainSelect)selStatement.getSelectBody());
    }
    
    return ps; 
  }

  public static List<OrderByElement> convertToOrderByList(String inp) throws JSQLParserException {
    List<OrderByElement> outList = Lists.newArrayList();
    if(inp!=null){
      String[] inpAry = inp.split(",");
      for (String s : inpAry) {
        OrderByElement ob = new OrderByElement();
        ob.setAscDescPresent(true);
        String[] nameOrder = s.trim().split(" ");
        if (nameOrder.length > 1) {
          if (nameOrder[1].equalsIgnoreCase("ASC")) {
            ob.setAsc(true);
          } else if (nameOrder[1].equalsIgnoreCase("DESC")) {
            ob.setAsc(false);
          }
        }
        Expression exp = CCJSqlParserUtil.parseExpression(s);
        ob.setExpression(exp);
        outList.add(ob);
      }
    }
    return outList;
  }

  public static List<Expression> convertToExpressionList(String s) throws JSQLParserException {
    List<Expression> expLst = Lists.newArrayList();
    String[] gCol = s.split(",");
    for (String str : gCol) {
      Expression expr = CCJSqlParserUtil.parseExpression(str.trim());
      expLst.add(expr);
    }
    return expLst;
  }

}
