package com.pacoapp.paco.shared.util;

import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Strings;
import com.pacoapp.paco.shared.model2.SQLQuery;

public class JsUtil {
  public static final Logger log = Logger.getLogger(JsUtil.class.getName());

  /**
   * The query JSON should have the following format Example 
   * {query:{criteria: " (group_name in(?,?) and (answer=?)) ",values:["New
   * Group","Exp Group", "ven"]},limit: 100,group: "group_name",order:
   * "response_time" ,select: ["group_name","response_time",
   * "experiment_name", "text", "answer"]} 
   * The above JSON represents the following
   * query->criteria: String with where clause conditions and the values replaced by '?' 
   * query->values: An array of String representing the values of the '?' expressed in query->criteria (in order). 
   * query->limit: Integer Number of records to limit the result set 
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
  public static SQLQuery convertJSONToPOJO(String queryJson) {
    SQLQuery sqlObj = null;
    SQLQuery.Builder sqlBldr = null;
    String[] projectionColumns = null;
    String[] criteriaValues = null;
    JSONObject criteriaQueryObj;
    if (Strings.isNullOrEmpty(queryJson)) {
      return null;
    }
    
    try {
      criteriaQueryObj = new JSONObject(queryJson);
      if (criteriaQueryObj.has("select")) {
        JSONArray selectAr = criteriaQueryObj.getJSONArray("select");
        if (selectAr != null) {
          projectionColumns = new String[selectAr.length()];
          for (int j = 0; j < selectAr.length(); j++) {
            projectionColumns[j] = selectAr.getString(j);
          }
        }
      }
      
      sqlBldr = new SQLQuery.Builder(projectionColumns);
              
      if (criteriaQueryObj.has("query")) {
        JSONObject queryCriteria = criteriaQueryObj.getJSONObject("query");
        if (queryCriteria != null) {
          if (queryCriteria.has("criteria")) {
            sqlBldr.criteriaQuery(queryCriteria.getString("criteria"));
          }

          if (queryCriteria.has("values")) {
            JSONArray cv = queryCriteria.getJSONArray("values");
            criteriaValues = new String[cv.length()];
            for (int i = 0; i < cv.length(); i++) {
              criteriaValues[i] = cv.getString(i);
            }
            sqlBldr.criteriaValues(criteriaValues);
          }
        }
      }
      
      if (criteriaQueryObj.has("order")) {
        sqlBldr.sortBy(criteriaQueryObj.getString("order"));
      }
      
      if (criteriaQueryObj.has("limit")) {
        sqlBldr.limit(criteriaQueryObj.getString("limit"));
      }
      
      // only if we have group clause, should we have the having column
      if (criteriaQueryObj.has("group")) {
        sqlBldr.groupBy(criteriaQueryObj.getString("group"));

        if (criteriaQueryObj.has("having")) {
          sqlBldr.having(criteriaQueryObj.getString("having"));
        }
      }

      sqlObj = sqlBldr.buildWithDefaultValues();

    } catch (JSONException e) {
      e.printStackTrace();
      log.info("json exception"+e);
    }

    return sqlObj;
  }
}
