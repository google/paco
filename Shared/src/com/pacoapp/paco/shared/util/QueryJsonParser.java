package com.pacoapp.paco.shared.util;

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
   * query->limit: String Number of records to limit the result set, with offset value separated by comma Eg for valid values :"100" or "100,1000" 
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
  public static SQLQuery parseSqlQueryFromJson(String queryJson) throws JSONException {
    final String SELECT = "select";
    final String QUERY = "query";
    final String CRITERIA = "criteria";
    final String VALUES = "values";
    final String ORDER = "order";
    final String LIMIT = "limit";
    final String GROUP = "group";
    final String HAVING = "having";
    
    SQLQuery sqlObj = null;
    SQLQuery.Builder sqlBldr = null;
    String[] projectionColumns = null;
    String[] criteriaValues = null;
    JSONObject queryObj = null;
    if (Strings.isNullOrEmpty(queryJson)) {
      return null;
    }
    queryObj = new JSONObject(queryJson);
    if (queryObj.has(SELECT)) {
      JSONArray selectAr = queryObj.getJSONArray(SELECT);
      if (selectAr != null) {
        projectionColumns = new String[selectAr.length()];
        for (int j = 0; j < selectAr.length(); j++) {
          projectionColumns[j] = selectAr.getString(j);
        }
      }
    }
    
    sqlBldr = new SQLQuery.Builder(projectionColumns);
            
    if (queryObj.has(QUERY)) {
      JSONObject queryCriteria = queryObj.getJSONObject(QUERY);
      if (queryCriteria != null) {
        if (queryCriteria.has(CRITERIA)) {
          sqlBldr.criteriaQuery(queryCriteria.getString(CRITERIA));
        }

        if (queryCriteria.has(VALUES)) {
          JSONArray cv = queryCriteria.getJSONArray(VALUES);
          criteriaValues = new String[cv.length()];
          for (int i = 0; i < cv.length(); i++) {
            criteriaValues[i] = cv.getString(i);
          }
          sqlBldr.criteriaValues(criteriaValues);
        }
      }
    }
    
    if (queryObj.has(ORDER)) {
      sqlBldr.sortBy(queryObj.getString(ORDER));
    }
    
    if (queryObj.has(LIMIT)) {
      sqlBldr.limit(queryObj.getString(LIMIT));
    }
    
    // only if we have group clause, should we have the having column
    if (queryObj.has(GROUP)) {
      sqlBldr.groupBy(queryObj.getString(GROUP));

      if (queryObj.has(HAVING)) {
        sqlBldr.having(queryObj.getString(HAVING));
      }
    }

    sqlObj = sqlBldr.buildWithDefaultValues();
    return sqlObj;
  }
}
