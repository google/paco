package com.pacoapp.paco.shared.util;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pacoapp.paco.shared.model2.EventBaseColumns;
import com.pacoapp.paco.shared.model2.OutputBaseColumns;
import com.pacoapp.paco.shared.model2.SQLQuery;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.SelectUtils;

public class SearchUtil {
  private static final Logger log = Logger.getLogger(SearchUtil.class.getName());
  public static final String COLUMN = "Column";
  public static final String ASC = "ASC";
  public static final String DESC = "DESC";
  public static final String ID = "_Id";
  
  public static Set<String> retrieveUserSpecifiedConditions(Select selectStatement, String colName) {
    PlainSelect pl = (PlainSelect) selectStatement.getSelectBody();
    Set<String> qvSet = Sets.newHashSet();
    getQueriedValue(pl.getWhere(), colName, qvSet);
    return qvSet;
  }

  public static void getQueriedValue(Expression node, String columnName, Set<String> queriedValueSet) {
    if (node == null) {
      return;
    }
    if (node instanceof Parenthesis) {
      node = ((Parenthesis) node).getExpression();
    }

    if (node instanceof BinaryExpression) {
      Expression le = ((BinaryExpression) node).getLeftExpression();
      Expression re = ((BinaryExpression) node).getRightExpression();

      if ((le instanceof Column) && le.toString().equalsIgnoreCase(columnName)) {
        queriedValueSet.add(re.toString());
      } else {
        getQueriedValue(le, columnName, queriedValueSet);
        getQueriedValue(re, columnName, queriedValueSet);

      }
    }

    if (node instanceof InExpression) {
      Expression le = ((InExpression) node).getLeftExpression();
      if (( le instanceof Column)
          && le.toString().equalsIgnoreCase(columnName)) {
        String listWithParen = ((InExpression) node).getRightItemsList().toString().replace('(', ' ');
        String listWithoutParen = listWithParen.replace(')', ' ');
        String[] arr = listWithoutParen.split(", ");
        queriedValueSet.addAll(Arrays.asList(arr));
      }
    }
  }

  public static Select getJsqlSelectStatement(String selectSql) throws JSQLParserException {
    Select statement = (Select) CCJSqlParserUtil.parse(selectSql);
    return statement;
  }

  public static List<String> getAllColNamesInQuery(String selectSql) throws JSQLParserException {
    List<String> colList = Lists.newArrayList();
    Select selStatement = getJsqlSelectStatement(selectSql);
    ColumnNamesFinder colNamesFinder = new ColumnNamesFinder();
    colList = colNamesFinder.getColumnList(selStatement);
    return colList;
  }

  public static String identifyTablesInvolved(List<String> colNamesInQuery) {
    if (colNamesInQuery.contains(OutputBaseColumns.NAME) || colNamesInQuery.contains(OutputBaseColumns.ANSWER)) {
      return OutputBaseColumns.TABLE_NAME;
    } else {
      return EventBaseColumns.TABLE_NAME;
    }
  }

  public static String getPlainSql(SQLQuery sqlQuery) throws JSQLParserException {
    Select selQry = null;
    Expression whereExpr = null;
    List<OrderByElement> orderByList = null;
    List<Expression> groupBy = null;
    Limit limit = null;
    // projection
    if (sqlQuery.getProjection() != null) {
      selQry = SelectUtils.buildSelectFromTableAndExpressions(new Table(EventBaseColumns.TABLE_NAME), sqlQuery.getProjection());
    }

    // where clause
    if (sqlQuery.getCriteriaQuery() != null) {
      // TODO replace with jsql utility
      String[] repl = sqlQuery.getCriteriaValue();
      String tempWhereClause = sqlQuery.getCriteriaQuery();
      int i = 0;
      while (tempWhereClause.contains("?")) {
        tempWhereClause = tempWhereClause.replaceFirst("\\?", repl[i++]);
      }
      whereExpr = CCJSqlParserUtil.parseCondExpression(tempWhereClause);
    }

    // groupBy
    if (sqlQuery.getGroupBy() != null) {
      groupBy = convertToExpressionList(sqlQuery.getGroupBy());
    }

    // orderBy
    if (sqlQuery.getSortOrder() != null) {
      orderByList = convertToOrderByList(sqlQuery.getSortOrder());
    }

    // limit
    if (sqlQuery.getLimit() != null) {
      limit = new Limit();
      String[] splitLimitOffset = sqlQuery.getLimit().split(",");
      limit.setRowCount(Long.parseLong(splitLimitOffset[0]));
      if (splitLimitOffset.length > 1) {
        limit.setOffset(Long.parseLong(splitLimitOffset[1]));
      }
    }

    PlainSelect plainSel = (PlainSelect) selQry.getSelectBody();
    plainSel.setWhere(whereExpr);
    plainSel.setOrderByElements(orderByList);
    plainSel.setGroupByColumnReferences(groupBy);
    plainSel.setLimit(limit);

    return plainSel.toString();
  }

  public static String getTableIndicator(SQLQuery sqlQuery) throws JSQLParserException {
    String plainSql = getPlainSql(sqlQuery);
    return getTableIndicator(sqlQuery, plainSql);
  }

  public static String getTableIndicator(SQLQuery sqlQuery, String plainSql) throws JSQLParserException {
    List<String> colNamesInQuery = getAllColNamesInQuery(plainSql);
    String tablesInvolved = identifyTablesInvolved(colNamesInQuery);
    return tablesInvolved;
  }

  public static PlainSelect getJoinQry(SQLQuery sqlQuery) throws JSQLParserException {
    PlainSelect ps = null;
    Expression joinExp = null;
    Select selStatement = null;
    List<Join> jList = Lists.newArrayList();
    Join joinObj = new Join();
    FromItem ft = new Table(OutputBaseColumns.TABLE_NAME);

    String plainSql = getPlainSql(sqlQuery);
    selStatement = getJsqlSelectStatement(plainSql);
    String tableIndicator = getTableIndicator(sqlQuery, plainSql);
    if (tableIndicator != null && tableIndicator.equals(OutputBaseColumns.TABLE_NAME)) {
      try {
        joinExp = CCJSqlParserUtil.parseCondExpression(ID+ " = " +OutputBaseColumns.TABLE_NAME+ "."+OutputBaseColumns.EVENT_ID);
      } catch (JSQLParserException e) {
        e.printStackTrace();
      }
      joinObj.setOnExpression(joinExp);
      joinObj.setInner(true);
      joinObj.setRightItem(ft);
      jList.add(joinObj);
      ps = ((PlainSelect) selStatement.getSelectBody());
      ps.setJoins(jList);
    } else {
      ps = ((PlainSelect) selStatement.getSelectBody());
    }

    return ps;
  }
  

  public static List<OrderByElement> convertToOrderByList(String inp) throws JSQLParserException {
    List<OrderByElement> orderByList = Lists.newArrayList();
    if (inp != null) {
      String[] inpAry = inp.split(",");
      for (String s : inpAry) {
        OrderByElement ob = new OrderByElement();
        String[] nameOrder = s.trim().split(" ");
        if (nameOrder.length > 1) {
          ob.setAscDescPresent(true);
          ob.setAsc(nameOrder[1].equalsIgnoreCase(ASC));
        }
        Expression exp = CCJSqlParserUtil.parseExpression(s);
        ob.setExpression(exp);
        orderByList.add(ob);
      }
    }
    return orderByList;
  }

  public static List<Expression> convertToExpressionList(String s) throws JSQLParserException {
    List<Expression> expLst = Lists.newArrayList();
    Expression expr = null;
    String[] gCol = s.split(",");
    for (String str : gCol) {
      expr = CCJSqlParserUtil.parseExpression(str.trim());
      expLst.add(expr);
    }
    return expLst;
  }

}
