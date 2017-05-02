package com.google.sampling.experiential.server;

import java.util.List;
import java.util.Set;

import com.pacoapp.paco.shared.model2.EventBaseColumns;
import com.pacoapp.paco.shared.util.QueryPreprocessor;
import com.pacoapp.paco.shared.util.SearchUtil;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

public class ACLHelper {
  static final String EQUALS = " = ";

  // for testing only
  public static String getModifiedQueryBasedOnACL(String selectSql, String loggedInUser,
                                                  List<Long> adminExperimentsinDB,
                                                  QueryPreprocessor qPreprocessor) throws Exception {
    Select selStat = SearchUtil.getJsqlSelectStatement(selectSql);
    return getModifiedQueryBasedOnACL(selStat, loggedInUser, adminExperimentsinDB, qPreprocessor);
  }

  public static String getModifiedQueryBasedOnACL(Select selectSql, String loggedInUser,
                                                  List<Long> adminExperimentsinDB,
                                                  QueryPreprocessor qPreprocessor) throws Exception {
    String loggedInUserWithQuotes = "'" + loggedInUser + "'";
    boolean onlyQueryingOwnData = true;
    boolean adminOnAllExperiments = true;
    PlainSelect plainSelect = null;
    Set<String> userSpecifiedWhoValues = qPreprocessor.getWhoClause();
    Set<Long> userSpecifiedExpIdValues = qPreprocessor.getExpIdValues();
    plainSelect = (PlainSelect) selectSql.getSelectBody();
    // Level 1 filters
    // a->No exp id filter, no processing
    if (userSpecifiedExpIdValues.size() == 0) {
      throw new Exception("Unauthorized access: No experiment id filter");
    }

    // if user querying own data
    if (userSpecifiedWhoValues.size() == 0 || userSpecifiedWhoValues.size() > 1
        || !userSpecifiedWhoValues.contains(loggedInUserWithQuotes)) {
      onlyQueryingOwnData = false;
    }

    // if user is admin on all experiments
    if (adminExperimentsinDB != null && !adminExperimentsinDB.containsAll(userSpecifiedExpIdValues)) {
      adminOnAllExperiments = false;
    }

    if (!adminOnAllExperiments && !onlyQueryingOwnData) {
      throw new Exception("Unauthorized access: Mixed ACL error");
    }

    if ((adminExperimentsinDB != null && adminExperimentsinDB.size() == 0) && !onlyQueryingOwnData) {
      String whoClause = EventBaseColumns.WHO + EQUALS + loggedInUserWithQuotes;
      if (userSpecifiedWhoValues.size() == 0) {
        Expression oldWhereClause = plainSelect.getWhere();
        Expression newWhoClause = CCJSqlParserUtil.parseCondExpression(whoClause);
        Expression ex = new AndExpression(oldWhereClause, newWhoClause);
        plainSelect.setWhere(ex);
      }
    }

    return plainSelect.toString();
  }
}
