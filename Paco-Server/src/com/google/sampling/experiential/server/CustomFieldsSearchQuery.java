package com.google.sampling.experiential.server;

import java.sql.SQLException;
import java.text.ParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.sampling.experiential.cloudsql.columns.ExternStringInputColumns;
import com.google.sampling.experiential.cloudsql.columns.OutputServerColumns;
import com.google.sampling.experiential.dao.impl.CSEventOutputDaoImpl;
import com.pacoapp.paco.shared.model2.SQLQuery;
import com.pacoapp.paco.shared.util.Constants;
import com.pacoapp.paco.shared.util.SearchUtil;

import net.sf.jsqlparser.JSQLParserException;

public class CustomFieldsSearchQuery extends SearchQuery {
  CustomFieldsSearchQuery(SQLQuery sqlQueryObj, Float pacoProtocol) {
    this.pacoProtocol = pacoProtocol;
    this.sqlQueryObj = sqlQueryObj;
    this.isClientTzNeedsAdded = true;
  }
 
  @Override
  public void addJoinClauses(Boolean oldMethodFlag) throws JSQLParserException {
    boolean isOutputTableAdded = false;
    
    if (qPreProcessor.isOutputColumnsPresent()) {
      if (jsqlStatement.toString().contains(OutputServerColumns.ANSWER) || (oldMethodFlag && jsqlStatement.toString().contains(OutputServerColumns.TEXT))) {
        SearchUtil.addOutputJoinClause(jsqlStatement);
        isOutputTableAdded = true;
      } 
    }
    super.addJoinClauses(oldMethodFlag);
    // add ic, i, cc, esi, esll joins
    if (!oldMethodFlag) {
      super.addInputCollectionBundleJoinClause(jsqlStatement, isOutputTableAdded);
    }
  }
  
  @Override
  public PacoResponse executeAcledQuery(String aclQuery, Boolean oldColumnName) throws JSONException, SQLException, ParseException {
    log.info("custom fields execute");
    CustomResponse pacoResponse = new CustomResponse();
    CSEventOutputDaoImpl impl = new CSEventOutputDaoImpl();
    log.info("cs-acled qry"+ aclQuery);
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

  @Override
  public String renameTextColumn(String acledQuery) {
    // during migration phase, when both events and experiment_version_group_mapping tables have the same column name, experiment_id will be ambiguous
    // TODO following replaces will only be needed during the big migration phase. This should be cleaned up.
    String tempAcledQuery = acledQuery.replace("experiment_id", "experiment_version_group_mapping.experiment_id");
    tempAcledQuery = tempAcledQuery.replace("experiment_version_group_mapping.experiment_version_group_mapping.experiment_id", "experiment_version_group_mapping.experiment_id");;
    tempAcledQuery = tempAcledQuery.replace("group_name", "group_detail.group_name");
    return tempAcledQuery.replace("text", "esi1." + ExternStringInputColumns.LABEL );
  }
}
