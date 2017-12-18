package com.google.sampling.experiential.server;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.datastore.EventServerColumns;
import com.google.sampling.experiential.datastore.ExperimentLookupColumns;
import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.model2.EventBaseColumns;
import com.pacoapp.paco.shared.model2.OutputBaseColumns;
import com.pacoapp.paco.shared.model2.SQLQuery;
import com.pacoapp.paco.shared.util.Constants;
import com.pacoapp.paco.shared.util.ErrorMessages;
import com.pacoapp.paco.shared.util.SearchUtil;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.util.SelectUtils;

public class AllFieldsSearchQuery extends SearchQuery{
  private static String CLIENT_REQUEST = "ClientReq";
  private static final String AND = " and ";
  AllFieldsSearchQuery(SQLQuery sqlQueryObj, Float pacoProtocol) {
    this.pacoProtocol = pacoProtocol;
    this.sqlQueryObj = sqlQueryObj;
  }
 
  @Override
  public void addJoinClauses() throws JSQLParserException { 
    super.addJoinClauses();
    SearchUtil.addOutputJoinClause(jsqlStatement);
  }
  @Override
  public void addOptimizationToQuery() throws JSQLParserException {
    boolean outputColsInWhere = sqlQueryObj.getCriteriaQuery().contains(OutputBaseColumns.ANSWER) || sqlQueryObj.getCriteriaQuery().contains(OutputBaseColumns.NAME);
    jsqlStatement = modifyToOptimizePerformance(jsqlStatement, outputColsInWhere);
    
  }
  //instead of currentSelect which is --> select * from events inner join outputs on events._id=outputs.event_id where <conditions> <limit><group><order> 
  // do a late fetch as
  // optimizedSelect(firstJoinObj) which is -->               select * from events 
  //                                                                       inner join ouputs on events._id=outputs.event_id
  // (secondJoinObj with ExperimentLookup -->                              inner join experiment_lookup on events.experiment_lookup_id=experiment_lookup.lookup_id
  // (thirdJoinObj with subselect)                                         inner join (select _id, text from events inner join outputs on events._id=outputs.event_id where <conditions> <limit><group><order>) as clientReq 
  // (with thirdJoinCondition)                                                    on events._id=clientReq._id and outputs.event_id=clientReq._id and outputs.text=clientReq.text
  private static Select modifyToOptimizePerformance(Select currentSelect, boolean outputColsInWhere) throws JSQLParserException {
    PlainSelect optimizedPlainSelect = null;
    Select optimizedSelect = null;
    Expression firstJoinOnExp = null;
    Expression secondJoinOnExp = null;
    Expression thirdJoinOnExp = null;
    List<Join> jList = Lists.newArrayList();
    FromItem fromOutput = new Table(OutputBaseColumns.TABLE_NAME);
    FromItem fromExperimentLookup = new Table(ExperimentLookupColumns.TABLE_NAME); 
    Join firstJoinObj = new Join();
    Join secondJoinObj = new Join();
    Join thirdJoinObj = new Join();
    SubSelect clientReqQuery = new SubSelect();
    StringBuffer secondJoinCondition = new StringBuffer();
    StringBuffer thirdJoinCondition = new StringBuffer();
    
    // even though it is a select * , when we optimize we change it to get only _id and text
    modifyProjectionColumnsToClientQuery(currentSelect);
    clientReqQuery.setSelectBody(currentSelect.getSelectBody());
    
    clientReqQuery.setAlias(new Alias(CLIENT_REQUEST));
    // second join on condition
    secondJoinCondition.append(EventServerColumns.TABLE_NAME + "." + EventServerColumns.EXPERIMENT_LOOKUP_ID + " = " + ExperimentLookupColumns.TABLE_NAME + "." + ExperimentLookupColumns.EXPERIMENT_LOOKUP_ID);
    // third join on condition
    thirdJoinCondition.append(EventServerColumns.TABLE_NAME + "." + Constants.UNDERSCORE_ID + " = " +CLIENT_REQUEST + "." + Constants.UNDERSCORE_ID + AND);
    thirdJoinCondition.append(OutputBaseColumns.TABLE_NAME + "." + OutputBaseColumns.EVENT_ID + " = "  + CLIENT_REQUEST + "." + Constants.UNDERSCORE_ID);
    // if where clause contains text/answer then do not add this condition
    if (!outputColsInWhere) {
      thirdJoinCondition.append(AND + OutputBaseColumns.TABLE_NAME + "." + OutputBaseColumns.NAME+ " = "  + CLIENT_REQUEST + "." + OutputBaseColumns.NAME);
    }
         
    try {
      firstJoinOnExp = CCJSqlParserUtil.parseCondExpression(EventServerColumns.TABLE_NAME + "." + Constants.UNDERSCORE_ID+ " = " + OutputBaseColumns.TABLE_NAME + "."+ OutputBaseColumns.EVENT_ID);
      secondJoinOnExp = CCJSqlParserUtil.parseCondExpression(secondJoinCondition.toString());
      thirdJoinOnExp = CCJSqlParserUtil.parseCondExpression(thirdJoinCondition.toString());
    } catch (JSQLParserException e) {  
      log.warning(ErrorMessages.JSON_PARSER_EXCEPTION.getDescription()+ e.getMessage());
    }
    
    firstJoinObj.setOnExpression(firstJoinOnExp);
    firstJoinObj.setInner(true);
    firstJoinObj.setRightItem(fromOutput);
    
    secondJoinObj.setOnExpression(secondJoinOnExp);
    secondJoinObj.setInner(true);
    secondJoinObj.setRightItem(fromExperimentLookup);
    
    thirdJoinObj.setOnExpression(thirdJoinOnExp);
    thirdJoinObj.setInner(true);
    thirdJoinObj.setRightItem(clientReqQuery);
    
    jList.add(firstJoinObj);
    jList.add(secondJoinObj);
    jList.add(thirdJoinObj);
    optimizedSelect = SelectUtils.buildSelectFromTableAndSelectItems(new Table(EventBaseColumns.TABLE_NAME), new AllColumns());
    optimizedPlainSelect = ((PlainSelect) optimizedSelect.getSelectBody());
    // Since we are making an inner query and outer query to improve performance
    // when we have an order by, the outer qry does not order the records in the order we want, so adding the order clause to outer qry as well
    List<OrderByElement> orderByList = ((PlainSelect)currentSelect.getSelectBody()).getOrderByElements();
    if ( orderByList != null) {
      optimizedPlainSelect.setOrderByElements(orderByList);  
    }
    optimizedPlainSelect.setJoins(jList);
    return optimizedSelect;
  }
  private static void modifyProjectionColumnsToClientQuery(Select selStatement) {
    SelectBody sb = selStatement.getSelectBody();
    SelectItem si1 = new SelectExpressionItem();
    ((SelectExpressionItem)si1).setExpression(new Column(Constants.UNDERSCORE_ID));
    SelectItem si2 = new SelectExpressionItem();
    ((SelectExpressionItem)si2).setExpression(new Column(OutputBaseColumns.NAME));
    List<SelectItem> siList = Lists.newArrayList();
    siList.add(si1);
    siList.add(si2);
    ((PlainSelect)sb).setSelectItems(siList);  
  }

  @Override
  public PacoResponse executeAcledQuery(String aclQuery) throws SQLException, ParseException {
    log.info("all fields execute");
    boolean withOutputs = true;
    List<EventDAO> evtList = null;
    CloudSQLDaoImpl impl = new CloudSQLDaoImpl();
    EventQueryStatus pacoResponse = new EventQueryStatus(pacoProtocol);
    
    evtList = impl.getEvents(aclQuery, withOutputs);
    pacoResponse.setEvents(evtList);
    log.info("all fields execute - records size:" + evtList.size());
    pacoResponse.setStatus(Constants.SUCCESS);
    return pacoResponse;
  }
  
}
