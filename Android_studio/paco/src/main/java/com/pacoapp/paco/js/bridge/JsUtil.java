package com.pacoapp.paco.js.bridge;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Strings;

public class JsUtil {
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
  public static SQLQuery convertJSONToPOJO(String queryJson) throws JSONException {
    SQLQuery sqlObj = null;
    String criteriaColumns = null;
    String[] projectionColumns = null;
    String groupBy = null;
    String[] criteriaValues = null;
    String sortOrder = null;
    String limitRecords = null;
    String having = null;
    JSONObject criteriaQueryObj;
    if (Strings.isNullOrEmpty(queryJson)) {
      return null;
    }
    try {
      criteriaQueryObj = new JSONObject(queryJson);
      sqlObj = new SQLQuery();
      if (criteriaQueryObj.has("select")) {
        JSONArray selectAr = criteriaQueryObj.getJSONArray("select");
        if (selectAr != null) {
          projectionColumns = new String[selectAr.length()];
          for (int j = 0; j < selectAr.length(); j++) {
            projectionColumns[j] = selectAr.getString(j);
          }
        }
      }
      
      if(projectionColumns == null){
        projectionColumns = new String[]{"*"};
      }

      if (criteriaQueryObj.has("query")) {
        JSONObject queryCriteria = criteriaQueryObj.getJSONObject("query");
        if (queryCriteria != null) {
          if (queryCriteria.has("criteria")) {
            criteriaColumns = queryCriteria.getString("criteria");
          }

          if (queryCriteria.has("values")) {
            JSONArray cv = queryCriteria.getJSONArray("values");
            criteriaValues = new String[cv.length()];
            for (int i = 0; i < cv.length(); i++) {
              criteriaValues[i] = cv.getString(i);
            }
          }
        }
      }
      
      if (criteriaQueryObj.has("order")) {
        sortOrder = criteriaQueryObj.getString("order");
      }
      
      if (criteriaQueryObj.has("limit")) {
        limitRecords = criteriaQueryObj.getString("limit");
      }
      
      // only if we have group clause, should we have the having column
      if (criteriaQueryObj.has("group")) {
        groupBy = criteriaQueryObj.getString("group");

        if (criteriaQueryObj.has("having")) {
          having = criteriaQueryObj.getString("having");
        }
      }

      sqlObj.setProjection(projectionColumns);
      sqlObj.setCriteriaQuery(criteriaColumns);
      sqlObj.setCriteriaValue(criteriaValues);
      sqlObj.setGroupBy(groupBy);
      sqlObj.setHaving(having);
      sqlObj.setLimit(limitRecords);
      sqlObj.setSortOrder(sortOrder);

    } catch (JSONException e) {
      e.printStackTrace();
      throw e;
    }

    return sqlObj;
  }
}
