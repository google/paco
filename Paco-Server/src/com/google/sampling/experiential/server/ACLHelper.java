package com.google.sampling.experiential.server;

import java.util.List;
import java.util.Set;

import com.google.sampling.experiential.cloudsql.columns.EventServerColumns;
import com.pacoapp.paco.shared.util.Constants;
import com.pacoapp.paco.shared.util.ErrorMessages;
import com.pacoapp.paco.shared.util.QueryPreprocessor;
import com.pacoapp.paco.shared.util.SearchUtil;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

public class ACLHelper {
  

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
    String loggedInUserWithQuotes = Constants.SINGLE_QUOTE + loggedInUser + Constants.SINGLE_QUOTE;
    boolean onlyQueryingOwnData = true;
    boolean adminOnAllExperiments = true;
    boolean onlyAParticipant = false;
    PlainSelect plainSelect = null;
    Set<String> userSpecifiedWhoValues = qPreprocessor.getWhoClause();
    Set<Long> userSpecifiedExpIdValues = qPreprocessor.getExpIdValues();
    plainSelect = (PlainSelect) selectSql.getSelectBody();
    // Level 1 filters
    // a->No exp id filter, no processing
    if (userSpecifiedExpIdValues.size() == 0) {
      throw new Exception(ErrorMessages.EXPERIMENT_ID_CLAUSE_EXCEPTION.getDescription());
    }
    
    // is the user only a participant
    if (adminExperimentsinDB != null && adminExperimentsinDB.size() == 0) {
      onlyAParticipant = true;
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
    
    // if the user is an admin on atleast one experiment, then check if he has full access/ACL on all expts in query
    // and also if he is not querying just his data
    if (!onlyAParticipant && !adminOnAllExperiments && !onlyQueryingOwnData) {
      throw new Exception(ErrorMessages.UNAUTHORIZED_ACCESS_MIXED_ACL.getDescription());
    }
    
    // if logged in user is only a participant  
    // and he queries more than his data 
    if (onlyAParticipant && !onlyQueryingOwnData) {
      Expression ex = null;
      String whoClause = EventServerColumns.WHO + Constants.EQUALS + loggedInUserWithQuotes;
      if (userSpecifiedWhoValues.size() == 0) {
        Expression oldWhereClause = plainSelect.getWhere();
        Expression newWhoClause = CCJSqlParserUtil.parseCondExpression(whoClause);
        if (oldWhereClause != null) {
          ex = new AndExpression(oldWhereClause, newWhoClause);
        } else {
          ex = newWhoClause;
        }
        plainSelect.setWhere(ex);
      }
    }

    return plainSelect.toString();
  }
}
