package com.pacoapp.paco.shared.util;

import java.util.Iterator;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Strings;
import com.pacoapp.paco.shared.model2.SQLQuery;

public class QueryJsonParser {
  public static final Logger log = Logger.getLogger(QueryJsonParser.class.getName());

  /**
   * The query JSON should have the following format Example 
   * {query:{criteria: " (group_name in(?,?) and (answer=?)) ",values:["New
   * Group","Exp Group", "ven"]},limit: "100,10",group: "group_name",order:
   * "response_time" ,select: ["group_name","response_time",
   * "experiment_name", "text", "answer"]} 
   * The above JSON represents the following
   * query->criteria: String with where clause conditions and the values replaced by '?' 
   * query->values: An array of String representing the values of the '?' expressed in query->criteria (in order). 
   * query->limit: String Number of records to limit the result set, followed by "," followed by the offset integer(startPosition). Eg: 10,100 
   * query->group: String which holds the group by column 
   * query->order: String which holds the order by columns separated by commas 
   * query->select: An array of String which holds the column names and executes the following query 
   * Since the query requires columns from both Events and Outputs table, we do the
   * inner join. If the query requires columns from just Events table, it will
   * be a plain select ......from Events 
   * SELECT group_name, response_time,
   * experiment_name, text, answer FROM events INNER JOIN outputs ON
   * events._id = event_id WHERE ( (group_name in(?,?) and (answer=?)) ) GROUP
   * BY group_name ORDER BY response_time limit 100
   * @throws JSONException
   * 
   */
  public static SQLQuery parseSqlQueryFromJson(String queryJson, boolean enableGrpByAndProjection) throws JSONException {
    
    SQLQuery sqlObj = null;
    SQLQuery.Builder sqlBldr = null;
    String[] projectionColumns = null;
    String[] criteriaValues = null;
    JSONObject queryObj = null;
    if (Strings.isNullOrEmpty(queryJson)) {
      return null;
    }
    queryObj = new JSONObject(queryJson);
    sqlBldr = new SQLQuery.Builder();
    Iterator<String> itr = queryObj.keys();
    String crKey = null;
    
    while (itr.hasNext()) {
      
      crKey = itr.next();
      // Only when we enable group by feature, can we allow user specified projection columns
      if (enableGrpByAndProjection && crKey.equalsIgnoreCase(Constants.SELECT)) {
        JSONArray selectAr = queryObj.getJSONArray(crKey);
        if (selectAr != null) {
          projectionColumns = new String[selectAr.length()];
          sqlBldr.fullEventAndOutputs(false);
          for (int j = 0; j < selectAr.length(); j++) {
            projectionColumns[j] = selectAr.getString(j);
            if (Constants.STAR.equals(projectionColumns[j])) {
              sqlBldr.fullEventAndOutputs(true);
            }
          }
          sqlBldr.projection(projectionColumns);
        }
      } 
              
      if (crKey.equalsIgnoreCase(Constants.QUERY)) {
        JSONObject queryCriteria = queryObj.getJSONObject(crKey);
        if (queryCriteria != null) {
          Iterator<String> qcItr = queryCriteria.keys();
          while (qcItr.hasNext()) {
            String crQcKey = qcItr.next();
            if (crQcKey.equalsIgnoreCase(Constants.CRITERIA)) {
              sqlBldr.criteriaQuery(queryCriteria.getString(crQcKey).trim());
            }
    
            if (crQcKey.equalsIgnoreCase(Constants.VALUES)) {
              JSONArray cv = queryCriteria.getJSONArray(crQcKey);
              criteriaValues = new String[cv.length()];
              for (int i = 0; i < cv.length(); i++) {
                // identify Json string which could be marked with single or double quotes
                // to ones with single quotes, because jsql parser considers only values within single quotes
                // as string value
                if(cv.get(i).getClass().getName().equals("java.lang.String")) {
                  criteriaValues[i] = Constants.SINGLE_QUOTE+cv.getString(i)+Constants.SINGLE_QUOTE;
                } else {
                  criteriaValues[i] = cv.getString(i);
                }
              }
              sqlBldr.criteriaValues(criteriaValues);
            }
          }
        }
      }
      
      if (crKey.equalsIgnoreCase(Constants.ORDER)) {
        sqlBldr.sortBy(queryObj.getString(crKey).trim());
      }
      
      if (crKey.equalsIgnoreCase(Constants.LIMIT)) {
        sqlBldr.limit(queryObj.getString(crKey).trim());
      }
      
      // groupBy feature should be enabled and only if we have group clause, should we have the having column
      if (enableGrpByAndProjection && crKey.equalsIgnoreCase(Constants.GROUP)) {
        sqlBldr.groupBy(queryObj.getString(crKey).trim());
        // This gets set twice. Once when there is a *, and next when there is a group by.
        // Group by takes higher precedence.
        sqlBldr.fullEventAndOutputs(false);
  
        if (crKey.equalsIgnoreCase(Constants.HAVING)) {
          sqlBldr.having(queryObj.getString(crKey).trim());
        }
      }
    }
    sqlObj = sqlBldr.buildWithDefaultValues();
    return sqlObj;
  }
}
