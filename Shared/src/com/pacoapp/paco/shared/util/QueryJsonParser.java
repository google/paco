package com.pacoapp.paco.shared.util;

import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Strings;
import com.pacoapp.paco.shared.model2.SPRequest;
import com.pacoapp.paco.shared.model2.SQLQuery;
import com.pacoapp.paco.shared.model2.StoredProcEnum;

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
    // Only when we enable group by feature, can we allow user specified projection columns
    if (enableGrpByAndProjection && queryObj.has(Constants.SELECT)) {
      JSONArray selectAr = queryObj.getJSONArray(Constants.SELECT);
      if (selectAr != null) {
        projectionColumns = new String[selectAr.length()];
        for (int j = 0; j < selectAr.length(); j++) {
          projectionColumns[j] = selectAr.getString(j);
        }
      }
    } 
    
    sqlBldr = new SQLQuery.Builder(projectionColumns);
            
    if (queryObj.has(Constants.QUERY)) {
      JSONObject queryCriteria = queryObj.getJSONObject(Constants.QUERY);
      if (queryCriteria != null) {
        if (queryCriteria.has(Constants.CRITERIA)) {
          sqlBldr.criteriaQuery(queryCriteria.getString(Constants.CRITERIA).trim());
        }

        if (queryCriteria.has(Constants.VALUES)) {
          JSONArray cv = queryCriteria.getJSONArray(Constants.VALUES);
          criteriaValues = new String[cv.length()];
          for (int i = 0; i < cv.length(); i++) {
            criteriaValues[i] = cv.getString(i);
          }
          sqlBldr.criteriaValues(criteriaValues);
        }
      }
    }
    
    if (queryObj.has(Constants.ORDER)) {
      sqlBldr.sortBy(queryObj.getString(Constants.ORDER).trim());
    }
    
    if (queryObj.has(Constants.LIMIT)) {
      sqlBldr.limit(queryObj.getString(Constants.LIMIT).trim());
    }
    
    // groupBy feature should be enabled and only if we have group clause, should we have the having column
    if (enableGrpByAndProjection && queryObj.has(Constants.GROUP)) {
      sqlBldr.groupBy(queryObj.getString(Constants.GROUP).trim());

      if (queryObj.has(Constants.HAVING)) {
        sqlBldr.having(queryObj.getString(Constants.HAVING).trim());
      }
    }

    sqlObj = sqlBldr.buildWithDefaultValues();
    return sqlObj;
  }
  
  public static SPRequest parseStoredProcRequestFromJson(String queryJson) throws JSONException {
    final String EXPERIMENT_ID = "expId";
    final String FROM_DATE = "startDate";
    final String END_DATE = "endDate";
    final String WHO = "who";
    final String ORDER = "order";
    final String LIMIT = "limit";
    final String SP_NAME = "spName";
    
    SPRequest sqlObj = null;
    SPRequest.Builder sqlBldr = null;
    String expId = null;
    JSONObject queryObj = null;
    if (Strings.isNullOrEmpty(queryJson)) {
      return null;
    }
    queryObj = new JSONObject(queryJson);
    if (queryObj.has(EXPERIMENT_ID)) {
      expId = queryObj.getString(EXPERIMENT_ID);
    } 
    
    sqlBldr = new SPRequest.Builder(expId);
            
    if (queryObj.has(FROM_DATE)) {
      sqlBldr.fromDate(queryObj.getString(FROM_DATE));
    }
    
    if (queryObj.has(END_DATE)) {
      sqlBldr.toDate(queryObj.getString(END_DATE));
    }

    
    if (queryObj.has(ORDER)) {
      sqlBldr.sortBy(queryObj.getString(ORDER).trim());
    }
    
    if (queryObj.has(LIMIT)) {
      sqlBldr.limit(queryObj.getString(LIMIT).trim());
    }
    
    if (queryObj.has(WHO)) {
      sqlBldr.who(queryObj.getString(WHO).trim());
    }
    
    if (queryObj.has(SP_NAME)) {
      sqlBldr.spName(StoredProcEnum.getEnum(queryObj.getString(SP_NAME).trim()));
    }

    sqlObj = sqlBldr.buildWithDefaultValues();
    return sqlObj;
  }
}
