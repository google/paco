package com.google.sampling.experiential.server;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryError;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.InsertAllResponse;
import com.google.cloud.bigquery.InsertAllRequest.RowToInsert;
import com.google.cloud.bigquery.QueryRequest;
import com.google.cloud.bigquery.QueryResponse;
import com.google.cloud.bigquery.QueryResult;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableInfo;
import com.google.cloud.bigquery.Field.Mode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.comm.SQLQuery1;

public class PacoBQProcessor {

  private static final Logger log = Logger.getLogger(PacoBQProcessor.class.getName());
  static Map<String, Integer> dbPOJOMap = new HashMap<String, Integer>();
  long WAIT_TIME = 60000;
  boolean USE_LEGACY_SQL = true;
  
  public PacoBQProcessor(){
    if(dbPOJOMap.size()==0){
      dbPOJOMap.put("id",1);
      dbPOJOMap.put("who",2);
      dbPOJOMap.put("lat",3);
      dbPOJOMap.put("lon",4);
      dbPOJOMap.put("when",5);
      dbPOJOMap.put("appId",6);
      dbPOJOMap.put("pacoVersion",7);
      dbPOJOMap.put("experimentName",8);
      dbPOJOMap.put("experimentId",9);
      dbPOJOMap.put("experimentVersion",10);
      dbPOJOMap.put("schduledTime",11);
      dbPOJOMap.put("responseTime",12);
      dbPOJOMap.put("shared",13);
      dbPOJOMap.put("timeZone",14);
      dbPOJOMap.put("experimentGroupName",15);
      dbPOJOMap.put("actionId",16);
      dbPOJOMap.put("actionTriggerId",17);
      dbPOJOMap.put("actionTriggerSpecId",18);
      dbPOJOMap.put("what.name",19);
      dbPOJOMap.put("what.value",20);
    }
    
  }
  public String convertJSONtoBQSQL(SQLQuery1 jsonRequest){
    //TODO add date filter -> Feature of BQ 
    // This is the query for hitting BQ
    StringBuilder sb = new StringBuilder();
    String[]  criValue =  jsonRequest.getCriteriaValue();
    String projection = StringUtils.join(jsonRequest.getProjection(),", ");
    String whereClause = jsonRequest.getCriteriaQuery();
    String limit = jsonRequest.getLimit();
    String groupBy = jsonRequest.getGroupBy();
    String having = jsonRequest.getHaving();
    String orderBy = jsonRequest.getSortOrder();
    String tableName = jsonRequest.getTableName();
    int i=0;
    
    if(StringUtils.countMatches("\\?", whereClause) >  criValue.length){
      throw new RuntimeException();
    }
    
    sb.append("SELECT ");
    sb.append(projection);
    sb.append(" FROM [quantifiedself-staging2:test_from_code."+tableName+"]  where ");
    
    while (whereClause.contains("?")){
      whereClause = whereClause.replaceFirst("\\?", criValue[i++]);
    }
    
    sb.append(whereClause);
    
    if(groupBy!=null){
      sb.append(" GROUP BY ");
      sb.append(groupBy);
      
      if(having!=null){
        sb.append(" HAVING ");
        sb.append(having);
      }
    }
     
    if(orderBy!=null){
      sb.append(" ORDER BY ");
      sb.append(orderBy);
    }
    

    if(limit!=null){
      sb.append(" LIMIT ");
      sb.append(limit);
    }
    
    
    return sb.toString();
  }
  
  private List<String> getProjectionListFromSQL(String plainSQL){
    List<String> projList;
    String selectClause = StringUtils.substringBetween(plainSQL,"SELECT", "FROM");
    String select[] = selectClause.split(",");
    projList = Arrays.asList(select);
    return projList;
  }
  
  public boolean createTable(String tableName){
    BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
    TableId tableId1 = TableId.of("test_from_code", tableName);

    Field f1 = Field.newBuilder("id", Field.Type.integer()).build();
    Field f2 = Field.newBuilder("who", Field.Type.string()).build();
    Field f3 = Field.newBuilder("lat", Field.Type.string()).build();
    Field f4 = Field.newBuilder("lon", Field.Type.string()).build();
    Field f5 = Field.newBuilder("when", Field.Type.timestamp()).build();
    Field f6 = Field.newBuilder("appId", Field.Type.string()).build();
    Field f7 = Field.newBuilder("pacoVersion", Field.Type.string()).build();
    Field f8 = Field.newBuilder("experimentName", Field.Type.string()).build();
    Field f9 = Field.newBuilder("experimentId", Field.Type.string()).build();
    Field f10 = Field.newBuilder("experimentVersion", Field.Type.integer()).build();
    Field f11 = Field.newBuilder("scheduledTime", Field.Type.timestamp()).build();
    Field f12 = Field.newBuilder("responseTime", Field.Type.timestamp()).build();
    Field f13 = Field.newBuilder("shared", Field.Type.bool()).build();
    Field f14 = Field.newBuilder("timezone", Field.Type.string()).build();
    Field f15 = Field.newBuilder("experimentGroupName", Field.Type.string()).build();
    Field f16 = Field.newBuilder("actionId", Field.Type.integer()).build();
    Field f17 = Field.newBuilder("actionTriggerSpecId", Field.Type.integer()).build();
    
    Field whatName = Field.newBuilder("name", Field.Type.string()).build();
    Field whatValue = Field.newBuilder("value", Field.Type.string()).build();
    List<Field> whatFields = new ArrayList<Field>();
    whatFields.add(whatName);
    whatFields.add(whatValue);
    Field f18 = Field.newBuilder("what", Field.Type.record(whatFields)).setMode(Mode.REPEATED).build();
    
//
//    // Table schema definition
    Schema schema = Schema.newBuilder().addField(f1).addField(f2).addField(f3).addField(f4).addField(f5).addField(f6)
            .addField(f7).addField(f8).addField(f9).addField(f10).addField(f11).addField(f12).addField(f13).addField(f14)
            .addField(f15).addField(f16).addField(f17).addField(f18).build();
    // Create a table
    StandardTableDefinition tableDefinition = StandardTableDefinition.of(schema);
    Table createdTable = bigquery.create(TableInfo.of(tableId1, tableDefinition));
    log.info("Table created");
    return true;
  }
  
  public boolean insertAllToBQ(String tableName, Iterable<RowToInsert> rowLst){
    BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
    TableId tableId = TableId.of("test_from_code", tableName);
    
    try{
      
    //insert BQ
    InsertAllResponse qryResponse = bigquery.insertAll(InsertAllRequest.newBuilder(tableId).setRows(rowLst).build());
    if (qryResponse.hasErrors()) {
      // If any of the insertions failed, this lets you inspect the errors
      for (Entry<Long, List<BigQueryError>> entry : qryResponse.getInsertErrors().entrySet()) {
        // inspect row error
        for(BigQueryError bqe : entry.getValue()){
          log.info(bqe.getMessage() + "-->" +bqe.getReason());
        }
      }
    }
    log.info("All records inserted into BQ");
    }catch(Exception e){
      System.out.println("#####################"+e);
    }
    return true;
  }
  

  public String getTableName(Date inputDate){
    String tableName = "Partit_Events1";
    Calendar c= Calendar.getInstance();
    c.setTime(inputDate);
    int dd = c.get(Calendar.DAY_OF_MONTH);
    String dt ="";
    if(dd<10){
      dt="0"+dd;
    }
    int mm = c.get(Calendar.MONTH);
    mm++;
    String mon=mm+"";
    if (mm<10)
      mon = "0"+mm;
    
    int yy = c.get(Calendar.YEAR);
//    tableName = tableName+"$"+yy+mon+dt;
    return tableName;
  }
  
 
  
  
  // run client request in BQ
  public List<EventDAO> processRequest(SQLQuery1 jsonRequest, String user, DateTimeZone tz) {
    List<EventDAO> events = new ArrayList<>();
    EventDAO eventObj;
    String plainSQL = convertJSONtoBQSQL(jsonRequest);
    log.info("Query on BQ "+plainSQL);
    List<String> projectionList = getProjectionListFromSQL(plainSQL);
    BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
    QueryRequest queryRequest = QueryRequest.newBuilder(plainSQL).setMaxWaitTime(WAIT_TIME)
                               .setUseLegacySql(USE_LEGACY_SQL)
                               .build();
    QueryResponse response = bigquery.query(queryRequest);
    if (response.hasErrors()) {
       for(int g=0;g<response.getExecutionErrors().size();g++){
         BigQueryError be = response.getExecutionErrors().get(g);
         log.info("reason is "+be.getReason());
         log.info("msg is "+be.getMessage());
       }
    }

     QueryResult result = response.getResult();

     if(result!=null){
       Iterator<List<FieldValue>> iter = result.iterateAll();
       while (iter.hasNext()) {
         List<FieldValue> row = iter.next();
         eventObj = new EventDAO();
         populateIndividualRow(row, projectionList, eventObj);
         events.add(eventObj);
       }
     }else{
       log.info("result is null, might be taking longer than timeout specified");
     }
     log.info("Number of records from BQ"+ events.size());
     return events;
  }
  
  private void populateIndividualRow(List<FieldValue> record, List<String> projList, EventDAO event){

    for (int x=0; x< projList.size();x++){
      String s  = projList.get(x).trim();
      if(record.get(x).getValue()!=null){
        
        switch (dbPOJOMap.get(s)){
          case 1:
            event.setId(record.get(x).getLongValue());
            break;
          case 2:
            event.setWho(record.get(x).getStringValue());
            break;
          case 3:
            event.setLat(record.get(x).getStringValue());
            break;
          case 4:
            event.setLon(record.get(x).getStringValue());
            break;
          case 5:
            //divided by 1000 is a hack which we can remove. we will not run into this error
            //if we insert the date in the same format as we plan to retrieve
            if(new DateTime(record.get(x).getTimestampValue()).getYear()>2500)
              event.setWhen(new DateTime(record.get(x).getTimestampValue()/1000).toDate());
            else
              event.setWhen(new DateTime(record.get(x).getTimestampValue()).toDate());
            break;
          case 6:
            event.setAppId(record.get(x).getStringValue());
            break;
          case 7:
            event.setPaco_version(record.get(x).getStringValue());
            break;
          case 8:
            event.setExperimentName(record.get(x).getStringValue());
            break;
          case 9:
            event.setExperimentId(record.get(x).getLongValue());
            break;
          case 10:
            event.setExperimentVersion(Integer.parseInt(record.get(x).getStringValue()));
            break;
          case 11:
            //TODO remove 1000
            if(new DateTime(record.get(x).getTimestampValue()).getYear()>2500)
              event.setScheduledTime(new DateTime(record.get(x).getTimestampValue()/1000).toDate());
            else
              event.setScheduledTime(new DateTime(record.get(x).getTimestampValue()).toDate());
            break;
          case 12:
            //TODO remove 1000
            if(new DateTime(record.get(x).getTimestampValue()).getYear()>2500)
              event.setResponseTime(new DateTime(record.get(x).getTimestampValue()/1000).toDate());
            else
              event.setResponseTime(new DateTime(record.get(x).getTimestampValue()).toDate());
            break;
          case 13:
            event.setShared(record.get(x).getBooleanValue());
            break;
          case 14:
            //TODO
            event.setTimezone("timezone");
            break;
          case 15:
            event.setExperimentGroupName(record.get(x).getStringValue());
            break;
  
        }
      }
    }
  }
}
