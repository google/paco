package com.google.sampling.experiential.server;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
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
    boolean onlyQueryingOwnData = true;
    boolean adminOnAllExperiments = true;
    PlainSelect plainSelect = null;

    Set<String> userSpecifiedWhoValues = SearchUtil.retrieveUserSpecifiedConditions(selectSql, EventBaseColumns.WHO);
    Set<String> userSpecifiedExpIds = SearchUtil.retrieveUserSpecifiedConditions(selectSql,
                                                                                  EventBaseColumns.EXPERIMENT_ID);
    Set<Long> userSpecifiedExpIdValues = convertToLong(userSpecifiedExpIds);
    plainSelect = (PlainSelect) selectSql.getSelectBody();
    // Level 1 filters
    // a->No exp id filter, no processing
    if (userSpecifiedExpIdValues.size() == 0) {
      throw new Exception("Unauthorized access: No experiment id filter");
    }

    // if user querying own data
    if(userSpecifiedWhoValues.size()==0 || userSpecifiedWhoValues.size()>1 || !userSpecifiedWhoValues.contains(loggedInUserWithQuotes)){
      onlyQueryingOwnData = false;
    }
    
    // if user is admin on all experiments
    if(adminExperimentsinDB!=null && !adminExperimentsinDB.containsAll(userSpecifiedExpIdValues)){
      adminOnAllExperiments = false;
    }
    
    if(!adminOnAllExperiments &&  !onlyQueryingOwnData){
      throw new Exception("Unauthorized access: Mixed ACL error");
    }
    
    if ((adminExperimentsinDB!=null && adminExperimentsinDB.size()==0) && !onlyQueryingOwnData){
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

  private static Set<Long> convertToLong(Set<String> inpList) {
    Set<Long> outSet = Sets.newHashSet();
    for (String s : inpList) {
      outSet.add(Long.parseLong(s.trim()));
    }
    return outSet;
  }
}
