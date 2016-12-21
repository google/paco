package com.google.sampling.experiential.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.DateTimeZone;

import com.google.cloud.bigquery.InsertAllRequest.RowToInsert;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.server.crypto.AES;
import com.google.sampling.experiential.server.crypto.EncryptionContext;
import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.comm.SQLQuery1;

public abstract class StreamingStrategyImpl implements StreamingStrategy {
  
  EncryptionContext encContext = new EncryptionContext();
  SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");

  private static final Logger log = Logger.getLogger(IncrementalStreamingStrategy.class.getName());
  @Override
  //template method
  public List<EventDAO> processRequest(SQLQuery1 jsonRequest, String user, DateTimeZone tz) {
    PacoBQProcessor pbq = new PacoBQProcessor();
    String tableName = jsonRequest.getTableName()!=null?jsonRequest.getTableName():DataStrategy.TABLE_NAME;
    log.info("In template method step1");
    List<Event> evtList = selectRecordsFromDS(jsonRequest.getPushCritToBQ(), user, tz);
//    log.info("In template method step2"+ evtList.size());
    
    ListMultimap<String, Event> multiValueMap = convertToMultiMap(evtList);
    
    if(isCreateTableNeeded()){
      //bq create table
      log.info("In template method step3");
      pbq.createTable(tableName);
      
    }
    
    for(String eachDay : multiValueMap.keySet()){
      List<Event> forEachResponseDay = multiValueMap.get(eachDay);
      
      List<RowToInsert> rows = transformRecordsToBQFormat(forEachResponseDay);
   
      try{
        tableName = tableName+pbq.getTableNameDecorator(sdf.parse(eachDay));
      }catch(Exception e){
        
      }
      pbq.insertAllToBQ(tableName, rows);
      log.info("In template method step5");
    break;
    }
    
   
    List<EventDAO> eDao = pbq.runBQQuery(jsonRequest, user, tz);
//    List<EventDAO> eDao = new ArrayList<EventDAO>();
    log.info("In template method step6"+eDao.size());
    return eDao;
      
  }
  
  private ListMultimap<String, Event> convertToMultiMap(List<Event> evtList){
    ListMultimap<String, Event> multiValueMap = ArrayListMultimap.create();
   
    for(Event e: evtList){
      multiValueMap.put(sdf.format(e.getWhen()), e);
    }
    return multiValueMap;
  }
  
  abstract boolean  isCreateTableNeeded();
  
  private String stripQuotes(String parameter) {
    if (parameter == null) {
      return null;
    }
    if (parameter.startsWith("'") || parameter.startsWith("\"")) {
      parameter = parameter.substring(1);
    }
    if (parameter.endsWith("'") || parameter.endsWith("\"")) {
      parameter = parameter.substring(0, parameter.length() - 1);
    }
    return parameter;
  }
  
    
    
  public List<Event> selectRecordsFromDS(String criteria, String loggedInUser, DateTimeZone tz){
    //TODO adjust time zone
    EventRetriever evtRet = EventRetriever.getInstance();
    List<com.google.sampling.experiential.server.Query> query = new QueryParser().parse(stripQuotes(criteria));
    
    EventQueryResultPair eqrp = evtRet.getEventsInBatchesForBQ(query, loggedInUser, tz, 10000, null);
    return eqrp.getEvents();
  }
  
  public List<RowToInsert> transformRecordsToBQFormat(List<Event> eventList){
    Map<String, Object> row1Content = null;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    RowToInsert r1Obj =null;
    List<RowToInsert> insRowLst = Lists.newArrayList();
    
    for(Event e: eventList){
      // Values of the row to insert
      row1Content = new HashMap<>();
      
      row1Content.put("id", e.getId());
      row1Content.put("appId", e.getAppId()!=null?e.getAppId():""); // 0xA, 0xD, 0xD, 0xE, 0xD in base64
      row1Content.put("experimentId", e.getExperimentId()!=null?e.getExperimentId():"");
      row1Content.put("experimentName", e.getExperimentName()!=null?e.getExperimentName():""); // 0xA, 0xD, 0xD, 0xE, 0xD in base64
      row1Content.put("experimentGroupName", e.getExperimentGroupName()!=null?e.getExperimentGroupName():"");
      row1Content.put("experimentVersion", e.getExperimentVersion()!=null?e.getExperimentVersion():1); // 0xA, 0xD, 0xD, 0xE, 0xD in base64
      row1Content.put("who", e.getWho()!=null ? DigestUtils.md5Hex(e.getWho()):DigestUtils.md5Hex(""));
      row1Content.put("pacoVersion", e.getPacoVersion()!=null?e.getPacoVersion():"");
      row1Content.put("lat", e.getLat()!=null?e.getLat():"");
      row1Content.put("lon", e.getLon()!=null?e.getLon():"");
      row1Content.put("scheduledTime", e.getScheduledTime()!=null?sdf.format(e.getScheduledTime()):sdf.format(new Date()));
      row1Content.put("responseTime", e.getResponseTime()!=null?sdf.format(e.getResponseTime()):sdf.format(new Date()));
      row1Content.put("when", e.getWhen()!=null?sdf.format(e.getWhen()):sdf.format(new Date()));
      row1Content.put("timezone", e.getTimeZone()!=null? e.getTimeZone():"");
      List<String> res = e.getWhatKeys();
      List<Map> finalRespLst = new ArrayList<>();
      Map<String, Object> indResMap;
      if(res!=null){
        for (int l=0;l<res.size();l++){
          indResMap = Maps.newHashMap();
          indResMap.put("name", res.get(l));
          indResMap.put("value", e.getWhatByKey(res.get(l)));
          finalRespLst.add(indResMap);
        }
      }
      row1Content.put("what", finalRespLst);
      r1Obj = RowToInsert.of((e.getId()+""), row1Content);
      insRowLst.add(r1Obj);
      
    }
    return insRowLst;
  }
    

}
