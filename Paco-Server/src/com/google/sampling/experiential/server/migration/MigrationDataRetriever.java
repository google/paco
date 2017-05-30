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
import java.util.List;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.datastore.EventEntityConverter;
import com.google.sampling.experiential.model.Event;

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
