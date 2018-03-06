package com.google.sampling.experiential.server;

import java.sql.SQLException;
import java.text.ParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pacoapp.paco.shared.model2.SQLQuery;
import com.pacoapp.paco.shared.util.Constants;
import com.pacoapp.paco.shared.util.SearchUtil;

import net.sf.jsqlparser.JSQLParserException;

public class CustomFieldsSearchQuery extends SearchQuery{
  CustomFieldsSearchQuery(SQLQuery sqlQueryObj, Float pacoProtocol) {
    this.pacoProtocol = pacoProtocol;
    this.sqlQueryObj = sqlQueryObj;
    this.isClientTzNeedsAdded = true;
  }
 
  @Override
  public void addJoinClauses() throws JSQLParserException { 
    super.addJoinClauses();
    if (qPreProcessor.isOutputColumnsPresent()) {
      SearchUtil.addOutputJoinClause(jsqlStatement);
    }
  }
  
  @Override
  public void addOptimizationToQuery() {
    // Nothing to do
  }

  @Override
  public PacoResponse executeAcledQuery(String aclQuery) throws JSONException, SQLException, ParseException {
    log.info("custom fields execute");
    CustomResponse pacoResponse = new CustomResponse();
    CloudSQLDaoImpl impl = new CloudSQLDaoImpl();
    JSONArray resultsArray = impl.getResultSetAsJson(aclQuery, localDateColumns);
    JSONObject resultset = new JSONObject();
    resultset.put("customResponse", resultsArray);
    resultset.put("status", Constants.SUCCESS);
    String results = resultset.toString();
    pacoResponse.setResponse(results);
    log.info("custom fields execute - records size:" + resultsArray.length());
    pacoResponse.setStatus(Constants.SUCCESS);
    return pacoResponse;
  }


}
