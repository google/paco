/*
* Copyright 2011 Google Inc. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance  with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package com.google.sampling.experiential.server.migration;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.dao.CSEventDao;
import com.google.sampling.experiential.dao.CSEventOutputDao;
import com.google.sampling.experiential.dao.CSExperimentVersionGroupMappingDao;
import com.google.sampling.experiential.dao.CSOutputDao;
import com.google.sampling.experiential.dao.dataaccess.ExperimentVersionGroupMapping;
import com.google.sampling.experiential.dao.impl.CSEventDaoImpl;
import com.google.sampling.experiential.dao.impl.CSEventOutputDaoImpl;
import com.google.sampling.experiential.dao.impl.CSExperimentVersionGroupMappingDaoImpl;
import com.google.sampling.experiential.dao.impl.CSOutputDaoImpl;
import com.google.sampling.experiential.datastore.EventEntityConverter;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.What;
import com.google.sampling.experiential.server.ExceptionUtil;
import com.google.sampling.experiential.server.migration.dao.impl.CloudSQLMigrationDaoImpl;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.WhatDAO;
import com.pacoapp.paco.shared.util.ErrorMessages;
import com.pacoapp.paco.shared.util.SearchUtil;

import net.sf.jsqlparser.JSQLParserException;

/**
 * Retrieve Event objects from the JDO store.
 *
 * @author Bob Evans
 *
 */
public class MigrationDataRetriever {

  private static MigrationDataRetriever instance;
  private static final Logger log = Logger.getLogger(MigrationDataRetriever.class.getName());
  private static int NULL_CTR = 0;

  @VisibleForTesting
  MigrationDataRetriever() {
  }

  public static synchronized MigrationDataRetriever getInstance() {
    if (instance == null) {
      instance = new MigrationDataRetriever();
    }
    return instance;
  }


  private DateTime getEarliestWhen() throws Exception{
    CloudSQLMigrationDaoImpl  daoImpl =  new CloudSQLMigrationDaoImpl();
    Long whenUtcInMillis = null;
    try {
      whenUtcInMillis = daoImpl.getEarliestWhen();
    } catch (SQLException | ParseException e) {
      log.warning("Not able to get earliest when" + e.getMessage());
    }
    if (whenUtcInMillis != null) {
      DateTime dt = new DateTime(whenUtcInMillis);
      log.info("Earliest date in cloud sql"+ dt);
      return dt;
      
    } else {
      throw new Exception("no date fetched from Cloud sql, Dont proceed");
    }
  }
  
  private DateTime getEarliestStreaming() throws Exception{
    CloudSQLMigrationDaoImpl  daoImpl =  new CloudSQLMigrationDaoImpl();
    Long whenUtcInMillis = null;
    try {
      whenUtcInMillis = daoImpl.getEarliestStreaming();
    } catch (SQLException | ParseException e) {
      log.warning("Not able to get earliest in streaming" + e.getMessage());
    }
    if (whenUtcInMillis != null) {
      DateTime dt = new DateTime(whenUtcInMillis);
      log.info("Earliest date in streamin"+ dt);
     
      return dt;
    } else {
      throw new Exception("no date fetched from streaming Cloud sql, Dont proceed");
    }
  }
  
  
  private boolean readEventDataStoreAndInsertToCloudSql(String oldCursor) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    CloudSQLMigrationDaoImpl  daoImpl =  new CloudSQLMigrationDaoImpl();
    Cursor cursor = null;
    int count = 0;
    int pageSize = 1000;
    DateTime earliestWhenInCloudSql = null;
    List<Event> eventsBatch = null;
    QueryResultList<Entity> results = null;
    boolean isFinished = false;
    if (oldCursor == null) {
      try {
        earliestWhenInCloudSql = getEarliestWhen();
        daoImpl.persistStreamingStart(earliestWhenInCloudSql);
      } catch (Exception e) { 
        log.warning("Do not proceed. CS does not have data yet or we cannot persist streaming table");
        return false;
      }
    } else {
      log.info("old cursor " + oldCursor);
      cursor = Cursor.fromWebSafeString(oldCursor);
      try {
        earliestWhenInCloudSql = getEarliestStreaming();
      } catch (Exception e) { 
        log.warning("Do not proceed. Streaming table does not have data yet.");
        return false;
      }
    }
    
    while (true) {
      eventsBatch = Lists.newArrayList();
      if (count % 10000 == 0) {
        log.info("Count = " + count);
      }
      boolean isContinueCSInsert = true;
      FetchOptions fetchOptions = FetchOptions.Builder.withLimit(pageSize);
      if (cursor != null) {
        fetchOptions.startCursor(cursor);
      }

      try { 
        com.google.appengine.api.datastore.Query q = new com.google.appengine.api.datastore.Query("Event");
        Date utilEarlyWhen = earliestWhenInCloudSql.toDate();
        q.setFilter(new com.google.appengine.api.datastore.Query.FilterPredicate("when", FilterOperator.LESS_THAN, utilEarlyWhen));
        PreparedQuery pq = datastore.prepare(q);
        results = pq.asQueryResultList(fetchOptions);
        if (results.isEmpty()) {
          log.info("empty results");
          break;
        } 
        
        for (int i = 0; i < results.size(); i++) {
          Entity entity = results.get(i);
          Event event = createEventFromEntity(entity);
          event.setId(entity.getKey().getId());
          eventsBatch.add(event);
        }
      } catch ( DatastoreTimeoutException dte) {
        isContinueCSInsert = false;
        log.severe("datastore timing out" + dte);
        try {
          log.warning("Data store timing out, so sleeping for 1 hour");
          Thread.sleep(3600000);
        } catch (InterruptedException e) {
          log.warning("Data store timeout sleep interrupted" + e);
        }
      }
      
      if (isContinueCSInsert) {
        
        boolean isSuccess = daoImpl.insertEventsInBatch(eventsBatch);
        //Only if the batch cs insert of events, should we move the cursor to the next batch in datastore
        if (isSuccess) {
          cursor = results.getCursor();
          log.info("Moving the cursor:" + cursor.toWebSafeString());
          daoImpl.persistCursor(cursor.toWebSafeString());
          count = count + results.size();
        }
        
        if (cursor == null || !isSuccess) {
          log.info("null cursor or insert to cs failed, so break");
          isFinished = true;
          break;
        }
      }
    }
    return isFinished;
  }
  
  public boolean catchUpEventsFromDSToCS(String oldCursor,DateTime startTime, DateTime endTime, boolean populateEventsTableOldMethod) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    CloudSQLMigrationDaoImpl  migDaoImpl =  new CloudSQLMigrationDaoImpl();
    
    int count = 0;
    int pageSize = 1000;
    Cursor cursor = null;
    Event evtObjDS = null;
    QueryResultList<Entity> results = null;
    boolean isFinished = false;
    if (oldCursor != null) {
      log.info("old cursor " + oldCursor);
      cursor = Cursor.fromWebSafeString(oldCursor);
    }
    
    do {
     
      FetchOptions fetchOptions = FetchOptions.Builder.withLimit(pageSize);
      if (cursor != null) {
        fetchOptions.startCursor(cursor);
      }

      com.google.appengine.api.datastore.Query q = new com.google.appengine.api.datastore.Query("Event");
      FilterPredicate lessThan = new FilterPredicate("when", FilterOperator.LESS_THAN, endTime.toDate());
      FilterPredicate greaterThan = new FilterPredicate("when", FilterOperator.GREATER_THAN, startTime.toDate());
      Filter andFilter = CompositeFilterOperator.and(lessThan, greaterThan);
      q.setFilter(andFilter);
      PreparedQuery pq = datastore.prepare(q);
      results = pq.asQueryResultList(fetchOptions);
      if (results.isEmpty()) {
        log.info("empty results");
        isFinished = true;
        break;
      } 
      
      for (int i = 0; i < results.size(); i++) {
        Entity entity = results.get(i);
        // get event id from DS
        evtObjDS = EventEntityConverter.convertEntityToEvent(entity);
        evtObjDS.setId(entity.getKey().getId());
        copySingleEventAndOutputsFromDSToCS(evtObjDS, populateEventsTableOldMethod);    
      }
      cursor = results.getCursor();
      log.info("Moving the cursor:" + cursor.toWebSafeString());
      log.info("Count = " + count + "Results = "+ results.size());
      migDaoImpl.persistCursor(cursor.toWebSafeString());
      count = count + results.size();

    } while (cursor != null);
    return isFinished;
  }
  
 


  private void copySingleEventAndOutputsFromDSToCS(Event evtObjDS, boolean populateEventsTableOldMethod) {
    CSEventOutputDao eventOutputDaoImpl = new CSEventOutputDaoImpl();
    CSExperimentVersionGroupMappingDao evmDaoImpl = new CSExperimentVersionGroupMappingDaoImpl();
    CSEventDao eventDaoImpl = new CSEventDaoImpl();
    CSOutputDao outputDaoImpl = new CSOutputDaoImpl();
    CloudSQLMigrationDaoImpl sqlMigDaoImpl = new CloudSQLMigrationDaoImpl(); 
    List<String> whatTexts = Lists.newArrayList();
    Boolean eventPresentInCS = false;
    List<WhatDAO> outputsList = null;
    int outputsInDS=0;
    int outputsInCS=0;
    List<Event> evtDSLst = Lists.newArrayList();
    Set<What> whatsDS = evtObjDS.getWhat();
    outputsInDS = whatsDS.size();
    boolean withOutputs = false;
    ExperimentVersionGroupMapping evmForThisGroup = null;
    // find if event present in events cloud sql
    try {
      evtDSLst.add(evtObjDS);
      if (!populateEventsTableOldMethod) {
        Map<String, ExperimentVersionGroupMapping> allEVMInVersion = evmDaoImpl.getAllGroupsInVersion(Long.parseLong(evtObjDS.getExperimentId()), evtObjDS.getExperimentVersion());
        if (allEVMInVersion == null) { 
            evmDaoImpl.createEVGMByCopyingFromLatestVersion(Long.parseLong(evtObjDS.getExperimentId()), evtObjDS.getExperimentVersion());
            allEVMInVersion = evmDaoImpl.getAllGroupsInVersion(Long.parseLong(evtObjDS.getExperimentId()), evtObjDS.getExperimentVersion());
        } 
        if (allEVMInVersion == null) { 
          return;
        } 
        // Rename event group Name from null to System, if its system predefined inputs
        evmForThisGroup = evmDaoImpl.findMatchingEVGMRecord(evtObjDS, allEVMInVersion, true);
      }
      String getQueryForEventIdSql = SearchUtil.getQueryForEventRetrieval(evtObjDS.getId().toString());
      List<EventDAO> eventInCS = eventOutputDaoImpl.getEvents(getQueryForEventIdSql, withOutputs, populateEventsTableOldMethod);
      if (eventInCS.size() == 0) {
        //copy event to cloud sql
        if (populateEventsTableOldMethod) {
          eventDaoImpl.insertSingleEventOnlyOldFormat(evtObjDS);
        } else {
          eventDaoImpl.insertSingleEventOnly(evtObjDS);
        }
        eventPresentInCS = true;
      } else {
        eventPresentInCS = true;
      }
    } catch (SQLException | ParseException | JSQLParserException e) {
      log.warning(ErrorMessages.SQL_EXCEPTION.getDescription() + "Event id "+ evtObjDS.getId() + "needs to be moved to CS. But failed:" + ExceptionUtil.getStackTraceAsString(e));
      sqlMigDaoImpl.insertCatchupFailure("EventsReadOrWrite", evtObjDS.getId(), null, e.getMessage());
    } catch (Exception e) {
      log.warning(ErrorMessages.GENERAL_EXCEPTION.getDescription() + "Event id "+ evtObjDS.getId() + "needs to be moved to CS. But failed:"+ ExceptionUtil.getStackTraceAsString(e));
      sqlMigDaoImpl.insertCatchupFailure("EventsReadOrWrite", evtObjDS.getId(), null, e.getMessage());    
    }       
   
    if (eventPresentInCS) {
      // find if cloud sql has the correct number of outputs
      try {
        outputsList = outputDaoImpl.getOutputs(evtObjDS.getId(), populateEventsTableOldMethod);
        for (int i=0; i< outputsList.size();i++) {
          whatTexts.add(outputsList.get(i).getName());
        }
        outputsInCS = outputsList.size();
      } catch (SQLException e) {
        log.warning(ErrorMessages.SQL_EXCEPTION + " retrieve outputs for eventid" + evtObjDS.getId() + e.getMessage());
        sqlMigDaoImpl.insertCatchupFailure("OutputRead", evtObjDS.getId(), null, e.getMessage());
      }
      // identify missing outputs and insert those alone
      if (outputsInDS > outputsInCS) {
        Iterator<What> whatItr = whatsDS.iterator();
        while (whatItr.hasNext()) {
          What temp = whatItr.next();
          String text = temp.getName();
          String answer = temp.getValue();
          Long inputId = null;
          try {
            if (!populateEventsTableOldMethod) {
              inputId = evmForThisGroup.getInputCollection().getInputOrderAndChoices().get(text).getInput().getInputId().getId();
            }
            if (!whatTexts.contains(text)) {
              log.info("Outputs missing in CS for event id " + evtObjDS.getId() + "--" + text);
              outputDaoImpl.insertSingleOutput(evtObjDS.getId(), inputId ,  text, answer, populateEventsTableOldMethod) ;
            }
          } catch (SQLException sqle) { 
            sqlMigDaoImpl.insertCatchupFailure("OutputWrite", evtObjDS.getId(), text, sqle.getMessage());
          }
        }
      }
    }
  }

  private Event createEventFromEntity(Entity entity) {
    return EventEntityConverter.convertEntityToEvent(entity);
  }
  
  public boolean readOutputsDataStoreAndInsertToCloudSql(String oldCursor) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Cursor cursor = null;
    long count = 0;
    int pageSize = 1000;
    List<MigrationOutput> outputsBatch = null;
    QueryResultList<Entity> results = null;
    boolean isFinished = false;
    if(oldCursor != null) {
      log.info("old cursor " + oldCursor);
      cursor = Cursor.fromWebSafeString(oldCursor);
    }
    
    while (true) {
      outputsBatch = Lists.newArrayList();
      if ( count % 10000 == 0 ) {
        log.info("Count = " + count);
      }
      boolean isContinueCSInsert = true;
      FetchOptions fetchOptions = FetchOptions.Builder.withLimit(pageSize);
      if (cursor != null) {
        fetchOptions.startCursor(cursor);
      }
     
      try { 
        com.google.appengine.api.datastore.Query q = new com.google.appengine.api.datastore.Query("What");
        PreparedQuery pq = datastore.prepare(q);
        results = pq.asQueryResultList(fetchOptions);
        if (results.isEmpty()) {
          log.info("empty results");
          break;
        } 
       
        MigrationOutput output = null;
        for (int i = 0; i < results.size(); i++) {
          Entity entity = results.get(i);
          output = createOutputFromEntity(entity);
          outputsBatch.add(output);
        }
      } catch ( DatastoreTimeoutException dte) {
        isContinueCSInsert = false;
        log.severe("datastore timing out" + dte);
        try {
          log.warning("Data store timing out, so sleeping for 1 hour");
          Thread.sleep(3600000);
        } catch (InterruptedException e) {
          log.warning("Data store timeout sleep interrupted" + e);
        }
      }
      
      if (isContinueCSInsert) {
        CloudSQLMigrationDaoImpl  daoImpl =  new CloudSQLMigrationDaoImpl();
        
        boolean isSuccess = daoImpl.insertOutputsInBatch(outputsBatch);
        //Only if the batch cs insert is successful, should we move the cursor to the next batch in datastore
        if (isSuccess) {
          cursor = results.getCursor();
          if (cursor!=null) {
            log.info("Moving the cursor: " + cursor.toWebSafeString());
            daoImpl.persistCursor(cursor.toWebSafeString());
          }
        } else {
          if (cursor != null) {
            log.warning("cs insert batch failed, so restart from the cursor " + cursor.toWebSafeString());
          }
        }
        count = count + results.size();
        
        if (cursor == null || !isSuccess) {
          log.warning("Last sql batch insert was "+ isSuccess + ". If false, fix error and restart from cursor ");
          isFinished = true;
          break;
        }
      }//if continue csinsert
    }//while
    return isFinished;
  }


  private MigrationOutput createOutputFromEntity(Entity entity) {
    MigrationOutput output = new MigrationOutput();
    String tempName = null;
    String tempValue = null;
    
    output.setEventId(entity.getParent().getId());
    if(entity.getProperty("name") != null) {
      tempName = entity.getProperty("name").toString();
      if(tempName.equalsIgnoreCase("null")) {
        tempName = tempName + "-" + NULL_CTR++;
      }
    } else {
      tempName ="null-"+ NULL_CTR++;
    }
    output.setText(tempName);
    if (entity.getProperty("value") != null) {
      tempValue = entity.getProperty("value").toString();
    }
    output.setAnswer(tempValue);
    
    return output;
  }
  
  public boolean copyAllEventsFromLowLevelDSToCloudSql(String cursor) {
    log.info("Getting events from low level datastore for migrating to cloud sql");
    long t11 = System.currentTimeMillis();
   
    boolean finished = readEventDataStoreAndInsertToCloudSql(cursor);
   
    long t12 = System.currentTimeMillis();
    log.info("get execute time in millis: " + (t12 - t11));
    return finished;
  }
  
  public boolean copyAllOutputsFromLowLevelDSToCloudSql(String cursor) {
    log.info("Getting outputs from low level datastore for migrating to cloud sql");
    long t11 = System.currentTimeMillis();
   
    boolean finished = readOutputsDataStoreAndInsertToCloudSql(cursor);
   
    long t12 = System.currentTimeMillis();
    log.info("get execute time in millis: " + (t12 - t11));
    return finished;
  }
  
}
