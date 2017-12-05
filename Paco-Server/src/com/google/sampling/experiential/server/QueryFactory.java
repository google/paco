package com.google.sampling.experiential.server;

import com.pacoapp.paco.shared.model2.SQLQuery;
import com.pacoapp.paco.shared.util.Constants;
import com.pacoapp.paco.shared.util.ErrorMessages;

public class QueryFactory {
  public static SearchQuery createSearchQuery(SQLQuery sqlQueryObj, Float pacoProtocol) throws Exception {
    basicValidation(sqlQueryObj);
    if (sqlQueryObj.isFullEventAndOutputs()) {
      return new AllFieldsSearchQuery(sqlQueryObj, pacoProtocol);
    } else {
      return new CustomFieldsSearchQuery(sqlQueryObj, pacoProtocol);
    }
  }
  
  private static void basicValidation(SQLQuery sqlQueryObj) throws Exception {
    EventQueryStatus evQueryStatus = new EventQueryStatus();
    evQueryStatus.setStatus(Constants.SUCCESS);
    if (sqlQueryObj == null) {
      throw new Exception(ErrorMessages.JSON_PARSER_EXCEPTION.getDescription());
    }
    
    if (sqlQueryObj.getCriteriaQuery() == null || sqlQueryObj.getCriteriaValue() == null || sqlQueryObj.getCriteriaValue().length == 0 ) {
      throw new Exception(ErrorMessages.QUERY_CRITERIA_EMPTY_EXCEPTION.getDescription());
    }
  }
  
}
