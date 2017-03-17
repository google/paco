package com.google.sampling.experiential.server;

import java.util.List;

import com.google.common.collect.Lists;
import com.pacoapp.paco.shared.model2.EventBaseColumns;
import com.pacoapp.paco.shared.util.SearchUtil;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

public class ACLHelper {
  static final String EQUALS = " = ";
  public static String getModifiedQueryBasedOnACL(String selectSql, String loggedInUser,
                                                  List<Long> adminExperimentsinDB) throws Exception {
    Select selStat = SearchUtil.getJsqlSelectStatement(selectSql);
    return getModifiedQueryBasedOnACL(selStat, loggedInUser, adminExperimentsinDB);
  }

  public static String getModifiedQueryBasedOnACL(Select selectSql, String loggedInUser,
                                                  List<Long> adminExperimentsinDB) throws Exception {
    String loggedInUserWithQuotes = "'" + loggedInUser + "'";
    boolean onlyQueryingOwnData = false;
    boolean adminOnAllExperiments = false;
    PlainSelect plainSelect = null;

    List<String> userSpecifiedWhoValues = SearchUtil.retrieveUserSpecifiedConditions(selectSql, EventBaseColumns.WHO);
    List<String> userSpecifiedExpIds = SearchUtil.retrieveUserSpecifiedConditions(selectSql,
                                                                                  EventBaseColumns.EXPERIMENT_ID);
    List<Long> userSpecifiedExpIdValues = convertToLong(userSpecifiedExpIds);

    // Level 1 filters
    // a->No exp id filter, no processing
    if ((userSpecifiedExpIdValues.size() == 0)) {
      throw new Exception("Unauthorized access: No experiment id filter");
    }

    // if user querying own data
    for (String s : userSpecifiedWhoValues) {
      if (s.equalsIgnoreCase(loggedInUserWithQuotes)) {
        onlyQueryingOwnData = true;
      } else {
        onlyQueryingOwnData = false;
        break;
      }
    }

    // if user is admin on all experiments
    for (Long s : userSpecifiedExpIdValues) {
      if (adminExperimentsinDB.contains(s)) {
        adminOnAllExperiments = true;
      } else {
        adminOnAllExperiments = false;
        break;
      }
    }

    // b->Mixed ACL in experiment id filter/ not an administrator of any
    // experiment,
    // and who clause contains value other than logged in admin user
    if ((!adminOnAllExperiments || adminExperimentsinDB.size() == 0) && !onlyQueryingOwnData) {
      throw new Exception("Unauthorized access: Mixed/No Access on all/some experiments");
    }

    // c->Mixed ACL in experiment id filter, and who clause is not of a
    // participant or the logged in user
    // TODO participant check

    String whoClause = EventBaseColumns.WHO + EQUALS + loggedInUserWithQuotes;
    plainSelect = (PlainSelect) selectSql.getSelectBody();
    if (adminExperimentsinDB.size() == 0 && userSpecifiedWhoValues.size() == 0) {
      Expression oldWhereClause = plainSelect.getWhere();
      Expression newWhoClause = CCJSqlParserUtil.parseCondExpression(whoClause);
      Expression ex = new AndExpression(oldWhereClause, newWhoClause);
      plainSelect.setWhere(ex);
    }

    return plainSelect.toString();
  }

  private static List<Long> convertToLong(List<String> inpList) {
    List<Long> outList = Lists.newArrayList();
    for (String s : inpList) {
      outList.add(Long.parseLong(s.trim()));
    }
    return outList;
  }
}
