package com.pacoapp.paco.shared.util;

import java.util.List;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.pacoapp.paco.shared.model2.EventBaseColumns;
import com.pacoapp.paco.shared.model2.OutputBaseColumns;
import com.pacoapp.paco.shared.model2.SQLQuery;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.Distinct;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.SelectUtils;

public class SearchUtil {
  private static final Logger log = Logger.getLogger(SearchUtil.class.getName());
  
  public static Select getJsqlSelectStatement(String selectSql) throws JSQLParserException {
    Select statement = (Select) CCJSqlParserUtil.parse(selectSql);
    return statement;
  }

  public static String getPlainSql(SQLQuery sqlQuery) throws JSQLParserException, Exception {
    Select selQry = null;
    Expression whereExpr = null;
    List<OrderByElement> orderByList = null;
    List<Expression> groupBy = null;
    Limit limit = null;
    boolean allCol = false;
    boolean isDistinct = false;
    SelectItem star = new AllColumns();
    // projection
    String[] projections = sqlQuery.getProjection();
    if (projections != null) {
      for(int s = 0; s < projections.length; s++) {
        if (Constants.STAR.equals(projections[s])) {
          allCol = true;
          break;
        } else if (projections[s].equalsIgnoreCase(Constants.WHEN)) {
          projections[s] = Constants.WHEN_WITH_BACKTICK;
        } else if(projections[s].toLowerCase().startsWith(Constants.DISTINCT)) {
          isDistinct = true;
          projections[s] = projections[s].replace(Constants.DISTINCT, "").trim();
        }
      }
      if (allCol) {
        selQry = SelectUtils.buildSelectFromTableAndSelectItems(new Table(EventBaseColumns.TABLE_NAME), star);
      } else {
        selQry = SelectUtils.buildSelectFromTableAndExpressions(new Table(EventBaseColumns.TABLE_NAME), projections);  
      }
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
      tempWhereClause = tempWhereClause.replaceAll(Constants.WHEN, Constants.WHEN_WITH_BACKTICK);
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
      try {
        String[] splitLimitOffset = sqlQuery.getLimit().split(",");
        limit.setRowCount(Long.parseLong(splitLimitOffset[0]));
        if (splitLimitOffset.length > 1) {
          limit.setOffset(Long.parseLong(splitLimitOffset[1]));
        }
      } catch (NumberFormatException nfe){
        throw new Exception(ErrorMessages.INVALID_LIMIT_OFFSET.getDescription(), nfe);
      }
    }

    PlainSelect plainSel = (PlainSelect) selQry.getSelectBody();
    if (isDistinct) { 
      Distinct dt = new Distinct();
      plainSel.setDistinct(dt);
    }
    plainSel.setWhere(whereExpr);
    plainSel.setOrderByElements(orderByList);
    plainSel.setGroupByColumnReferences(groupBy);
    plainSel.setLimit(limit);

    return plainSel.toString();
  }
  
  public static void addOutputJoinClause(Select selStatement) throws JSQLParserException {
    PlainSelect ps = null;
    Expression joinExp = null;
    List<Join> jList = null;
    Join joinObj = new Join();
    ps = ((PlainSelect) selStatement.getSelectBody());
    if (ps.getJoins() == null) { 
      jList = Lists.newArrayList();
    } else {
      jList = ps.getJoins();
    }
    FromItem ft = new Table(OutputBaseColumns.TABLE_NAME); 
    try {
      joinExp = CCJSqlParserUtil.parseCondExpression(Constants.UNDERSCORE_ID+ " = " +OutputBaseColumns.TABLE_NAME+ "."+OutputBaseColumns.EVENT_ID);
    } catch (JSQLParserException e) {
      e.printStackTrace();
    }
    joinObj.setOnExpression(joinExp);
    joinObj.setInner(true);
    joinObj.setRightItem(ft);
    jList.add(joinObj);
    ps.setJoins(jList);
  }

  public static List<OrderByElement> convertToOrderByList(String inp) throws JSQLParserException {
    List<OrderByElement> orderByList = Lists.newArrayList();
    OrderByElement  addOnWhenOrderBy = new OrderByElement();
    if (inp != null) {
      String[] inpAry = inp.split(",");
      for (String s : inpAry) {
        if (s.contains(Constants.WHEN)) {
          s = s.replace(Constants.WHEN, Constants.WHEN_WITH_BACKTICK);
        }
        OrderByElement ob = new OrderByElement();
        String[] nameOrder = s.trim().split(" ");
        if (nameOrder.length > 1) {
          ob.setAscDescPresent(true);
          ob.setAsc(nameOrder[1].equalsIgnoreCase(Constants.ASC));
        }
       
        Expression exp = CCJSqlParserUtil.parseExpression(s);
        ob.setExpression(exp);
        orderByList.add(ob);
        
        if (s.contains(Constants.WHEN)) {
          Expression addOnWhenexp = CCJSqlParserUtil.parseExpression(Constants.WHEN_FRAC_SEC);
          addOnWhenOrderBy.setAscDescPresent(true);
          addOnWhenOrderBy.setAsc(ob.isAsc());
          addOnWhenOrderBy.setExpression(addOnWhenexp);
          orderByList.add(addOnWhenOrderBy);
        }
      }
    }
    return orderByList;
  }

  public static List<Expression> convertToExpressionList(String s) throws JSQLParserException {
    List<Expression> expLst = Lists.newArrayList();
    Expression expr = null;
    String[] gCol = s.split(",");
    for (String str : gCol) {
      if (Constants.WHEN.equalsIgnoreCase(str.trim())) {
        str = Constants.WHEN_WITH_BACKTICK;
      }
      expr = CCJSqlParserUtil.parseExpression(str.trim());
      expLst.add(expr);
    }
    return expLst;
  }
  
  public static String getQueryForEventRetrieval(String eventId) throws JSQLParserException, Exception { 
    SQLQuery.Builder sqlBldr = new SQLQuery.Builder();
    sqlBldr.criteriaQuery(Constants.UNDERSCORE_ID + "=?").criteriaValues(new String[]{eventId});
    String plainSql = getPlainSql(sqlBldr.buildWithDefaultValues());
    Select clientJsqlStatement = getJsqlSelectStatement(plainSql);
    return clientJsqlStatement.toString();
  }

}
