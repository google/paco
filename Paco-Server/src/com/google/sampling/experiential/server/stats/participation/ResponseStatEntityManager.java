package com.google.sampling.experiential.server.stats.participation;

import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Transaction;
import com.google.common.collect.Lists;

/**
 * This class persists the response statistics for the participants in an experiment by date.
 * 
 * It allows updating a particular stat for a particular participant in an experiment for a particular date.
 * 
 * It allows retrieval of the stats for a given participant in an experiment or for 
 * all participants in an experiment on a given date.
 * 
 * Dates are stored in local time (but encoded as UTC given the appengine constraint of UTC only timezones).
 * We want to query on the local logical day for a participant, e.g.
 * 9am local(participant) time across participants who may be in multiple timezones
 * without having to do a bunch of timezone wrangling.
 *
 */
public class ResponseStatEntityManager {
  private static final Logger LOG = Logger.getLogger(ResponseStatEntityManager.class.getName());

  public static final String KIND = "response_stats";
  
  public static final String EXPERIMENT_ID_PROPERTY = "experimentId";
  public static final String EXPERIMENT_GROUP_NAME_PROPERTY = "experimentGroupName";
  public static final String WHO_PROPERTY = "who";
  public static final String SCHED_R_PROPERTY = "schedR";
  public static final String MISSED_R_PROPERTY = "missedR";
  public static final String SELF_R_PROPERTY = "selfR";
  public static final String DATE_PROPERTY = "date";
  public static final String LAST_CONTACT_DATE_TIME_PROPERTY = "lastContact";
  
  /**
   * Increment the scheduled responses count for an individual in an experiment on a particular date.
   * @param experimentId
   * @param experimentGroupName TODO
   * @param who
   */
  public void updateScheduledResponseCountForWho(long experimentId, String experimentGroupName, String who, DateTime date) {
    updateResponseCountForWho(experimentId, experimentGroupName, who, date, SCHED_R_PROPERTY);
  }

  /**
   * Increment the missed responses count for an individual in an experiment on a particular date.
   * @param experimentId
   * @param experimentGroupName TODO
   * @param who
   */
  public void updateMissedResponseCountForWho(long experimentId, String experimentGroupName, String who, DateTime date) {
    updateResponseCountForWho(experimentId, experimentGroupName, who, date, MISSED_R_PROPERTY);
  }
  
  /**
   * Increment the self responses count for an individual in an experiment on a particular date.
   * @param experimentId
   * @param experimentGroupName TODO
   * @param who
   */
  public void updateSelfResponseCountForWho(long experimentId, String experimentGroupName, String who, DateTime date) {
    updateResponseCountForWho(experimentId, experimentGroupName, who, date, SELF_R_PROPERTY);
  }

//  
//  /**
//   * Retrieves the response stats for a particular row (person, as who) for a particular experiment
//   * on a particular date. 
//   * 
//   * This will return a separate row for each group.
//   * 
//   * The date is converted to UTC, preserving local date but set to midnight. This allows us to query 
//   * on the concept of a "local" date even though appengine stores in utc.
//   * 
//   * @param experimentId
//   * @param who
//   * @return
//   */
//  public ResponseStat getResponseStatForWhoOnDate(long experimentId, String who, DateTime date) {
//    Query query = new Query(KIND);
//    FilterPredicate whoFilter = createWhoFilter(who);
//    FilterPredicate experimentFilter = createExperimentFilter(experimentId);
//
//    DateTime utcLocalDate = LocalToUTCTimeZoneConverter.changeZoneToUTC(date);
//    long dateMidnightUtcMillis = createDateQueryPredicateValue(utcLocalDate);    
//    FilterPredicate dateMidnightUTCMillisFilter = createDateFilter(dateMidnightUtcMillis);
//    
//    query.setFilter(CompositeFilterOperator.and(experimentFilter, whoFilter, dateMidnightUTCMillisFilter));
// 
//    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
//    Entity whoResult = ds.prepare(query).asSingleEntity();
//    return createResponseStatFromQueryResult(whoResult);
//  }
  
  /**
   * Retrieves the response stats for each participant for a particular experiment
   * on a particular date (in participant local timezone). 
   * 
   * This will have multiple rows for each participant - one row for each group that they have responses in.
   * 
   * The date is converted to UTC, preserving local date but set to midnight. This allows us to query 
   * on the concept of a "local" date even though appengine stores in utc.
   * 
   * @param experimentId
   * @param who
   * @return
   */
  public List<ResponseStat> getResponseStatsForExperimentOnDate(long experimentId, DateTime date) {
    Query query = new Query(KIND);
    FilterPredicate experimentFilter = createExperimentFilter(experimentId);

    DateTime utcLocalDate = LocalToUTCTimeZoneConverter.changeZoneToUTC(date);
    long dateMidnightUtcMillis = createDateQueryPredicateValue(utcLocalDate);    
    FilterPredicate dateMidnightUTCMillisFilter = createDateFilter(dateMidnightUtcMillis);
    
    query.setFilter(CompositeFilterOperator.and(experimentFilter, dateMidnightUTCMillisFilter));
 
    return executeResponseStatsQuery(query);       
  }

  /**
   * Retrieves the response stats for each participant for a particular group
   * on a particular date. 
   * 
   * This will have one row per participant.
   * 
   * @param experimentId
   * @param experimentGroupName
   * @param date
   * @return
   */
  public List<ResponseStat> getResponseStatsForExperimentGroupOnDate(Long experimentId, String experimentGroupName, DateTime date) {
    Query query = new Query(KIND);
    FilterPredicate experimentFilter = createExperimentFilter(experimentId);

    DateTime utcLocalDate = LocalToUTCTimeZoneConverter.changeZoneToUTC(date);
    long dateMidnightUtcMillis = createDateQueryPredicateValue(utcLocalDate);    
    FilterPredicate dateMidnightUTCMillisFilter = createDateFilter(dateMidnightUtcMillis);

    FilterPredicate groupFilter = createExperimentGroupFilter(experimentGroupName);
    query.setFilter(CompositeFilterOperator.and(experimentFilter, groupFilter, dateMidnightUTCMillisFilter));
 
    return executeResponseStatsQuery(query);       

  }

  /**
   * Retrieves the response stats for a particular row (person, as who) for a particular experiment
   * This retrieves all dates for that person.
   * 
   * There will be multiple rows for each date - one per group and multiple date rows
   * 
   * @param experimentId
   * @param who
   * @return
   */
  public List<ResponseStat> getResponseStatsForParticipant(long experimentId, String who) {
    Query query = new Query(KIND);
    FilterPredicate whoFilter = createWhoFilter(who);
    FilterPredicate experimentFilter = createExperimentFilter(experimentId);
    query.setFilter(CompositeFilterOperator.and(experimentFilter, whoFilter));
 
    return executeResponseStatsQuery(query);    
  }
  
  /**
   * This will return one row for each date.
   * 
   * @param experimentId
   * @param experimentGroupName
   * @param participant
   * @return
   */
  public List<ResponseStat> getResponseStatsForParticipantForGroup(long experimentId, String experimentGroupName, String participant) {
    Query query = new Query(KIND);
    FilterPredicate whoFilter = createWhoFilter(participant);
    FilterPredicate experimentFilter = createExperimentFilter(experimentId);
    FilterPredicate experimentGroupFilter = createExperimentGroupFilter(experimentGroupName);
    query.setFilter(CompositeFilterOperator.and(experimentFilter, experimentGroupFilter, whoFilter));

    return executeResponseStatsQuery(query);    
  }

  


  /**
   * Retrieves the response stats for every row(person, as who) for an experiment
   * ordered by person by date.
   * 
   * This will return multiple rows for each date with multiple rows for each group for each person.
   * For example,
   * 
   * experimentId, who, date
   * 1, bob, experimentGroup1, 2016/1/2
   * 1, bob, experimentGroup2, 2016/1/2
   * 
   * 1, bob, experimentGroup1, 2016/1/3
   * 
   * 1, steve, experimentGroup1, 2016/1/2
   * 1, steve, experimentGroup2, 2016/1/2
   * 
   * 1, steve, experimentGroup1, 2016/1/3
   * ...
   * 
   * @return
   */
  public final List<ResponseStat> getResponseStatsForExperiment(long experimentId) {    
    Query query = new Query(KIND);
    query.setFilter(createExperimentFilter(experimentId));
    
    return executeResponseStatsQuery(query);
  }
  
  /**
   * Retrieves the response stats for every row(person, as who) for an experimentGroup within Experiment
   * ordered by person by date.
   * 
   * This will return multiple rows - one for each date- per person
   * For example,
   * 
   * experimentId, who, date
   * 1, bob, experimentGroup, 2016/1/2
   * 1, bob, experimentGroup, 2016/1/3
   * 1, bob, experimentGroup, 2016/1/4
   * 
   * 1, steve, experimentGroup, 2016/1/2
   * 1, steve, experimentGroup, 2016/1/3
   * 1, steve, experimentGroup, 2016/1/4
   * ...
   * 
   * @return
   */
  public final List<ResponseStat> getResponseStatsForExperimentGroup(long experimentId, String experimentGroup) {    
    Query query = new Query(KIND);
    FilterPredicate experimentFilter = createExperimentFilter(experimentId);
    FilterPredicate experimentGroupFilter = createExperimentGroupFilter(experimentGroup);
    Filter andFilter = CompositeFilterOperator.and(experimentFilter, experimentGroupFilter);
    query.setFilter(andFilter);
    return executeResponseStatsQuery(query);
  }


  private List<ResponseStat> executeResponseStatsQuery(Query query) {
    List<ResponseStat> responseStats = Lists.newArrayList();
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    for (Entity entity : ds.prepare(query).asIterable()) {
      responseStats.add(createResponseStatFromQueryResult(entity));
    }
    
    return responseStats;
  }

  /**
   * central method to update row in datastore. Creates the row if it does not already exist.
   * 
   *  Sets one of the SCHED_R, MISSED_R, SELF_R properties for participant of an experiment on a particular date.
   *
   * 
   * @param experimentId
   * @param experimentGroupName TODO
   * @param who
   * @param date TODO
   * @param prop
   */
  private void updateResponseCountForWho(long experimentId, String experimentGroupName, String who, DateTime date, String prop) {
    Query query = new Query(KIND);
    FilterPredicate experimentFilter = createExperimentFilter(experimentId);
    FilterPredicate experimentGroupFilter = createExperimentGroupFilter(experimentGroupName);
    FilterPredicate whoFilter = createWhoFilter(who);
    
    DateTime utcLocalDate = LocalToUTCTimeZoneConverter.changeZoneToUTC(date);
    
    long dateMidnightUtcMillis = createDateQueryPredicateValue(utcLocalDate);    
    FilterPredicate dateMidnightUTCMillisFilter = createDateFilter(dateMidnightUtcMillis);
    // bounding strategy on a date, but, since it is just a "date" that we are looking for, canonicalized in utc, 
    // why not just compare millis? One filter and numerical.
//    List<FilterPredicate> dateBoundFilters = createFiltersBoundingDate(utcLocalDate);
//    Filter andFilter = CompositeFilterOperator.and(experimentFilter, whoFilter, dateBoundFilters.get(0), dateBoundFilters.get(1));
    Filter andFilter = CompositeFilterOperator.and(experimentFilter, experimentGroupFilter, whoFilter, dateMidnightUTCMillisFilter);
    query.setFilter(andFilter);

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Transaction tx = ds.beginTransaction();
    Entity whoResult;
    int value;
    try {
      whoResult = ds.prepare(query).asSingleEntity();
      if (whoResult == null) {
        whoResult = new Entity(KIND);
        whoResult.setProperty(EXPERIMENT_ID_PROPERTY, experimentId);
        whoResult.setProperty(EXPERIMENT_GROUP_NAME_PROPERTY, experimentGroupName);
        whoResult.setProperty(WHO_PROPERTY, who);
        whoResult.setProperty(DATE_PROPERTY, dateMidnightUtcMillis);
        whoResult.setUnindexedProperty(LAST_CONTACT_DATE_TIME_PROPERTY, date.withZone(DateTimeZone.UTC).toDate());
        value = 1;
      } else {
        Long property = (Long) whoResult.getProperty(prop);
        if (property != null) {
          value = convertToInt(property) + 1;
        } else {
          value = 1;
        }
        
        // update last contact time
        Date lastTime = (Date)whoResult.getProperty(LAST_CONTACT_DATE_TIME_PROPERTY);
        if (lastTime == null) {
          whoResult.setProperty(LAST_CONTACT_DATE_TIME_PROPERTY, date.toDate());
        } else {
          DateTime lastDateTime = new DateTime(lastTime);
          if (lastDateTime.isBefore(date)) {
            whoResult.setProperty(LAST_CONTACT_DATE_TIME_PROPERTY, date.toDate());
          }
        }
      }
      whoResult.setUnindexedProperty(prop, value);
      ds.put(tx, whoResult);
      tx.commit();
    } catch (ConcurrentModificationException e) {
      LOG.log(Level.WARNING, "You may need more shards. Consider adding more shards.");
      LOG.log(Level.WARNING, e.toString(), e);
    } catch (Exception e) {
      LOG.log(Level.WARNING, e.toString(), e);
    } finally {
      if (tx.isActive()) {
        LOG.severe("Rolling Back transaction updating ResponseStatEntity for event: " + experimentId + ", " + experimentGroupName + ", " + who + ", " + date.toString() + ", " + prop);
        tx.rollback();
      }
    }
  }

  private long createDateQueryPredicateValue(DateTime utcLocalDate) {
    return convertDateTimeToMidnightMillis(utcLocalDate);
  }

  private long convertDateTimeToMidnightMillis(DateTime utcLocalDate) {
    return utcLocalDate.toDateMidnight().getMillis();
  }

    
  private FilterPredicate createExperimentFilter(long experimentId) {
    return new FilterPredicate(EXPERIMENT_ID_PROPERTY, FilterOperator.EQUAL, experimentId);
  }
  
  private FilterPredicate createExperimentGroupFilter(String experimentGroupName) {
    return new FilterPredicate(EXPERIMENT_GROUP_NAME_PROPERTY, FilterOperator.EQUAL, experimentGroupName);
  }


  private FilterPredicate createWhoFilter(String who) {
    return new FilterPredicate(WHO_PROPERTY, FilterOperator.EQUAL, who);
  }

  private FilterPredicate createDateFilter(long dateMidnightUtcMillis) {
    FilterPredicate dateMidnightUTCMillisFilter = new FilterPredicate(DATE_PROPERTY, FilterOperator.EQUAL, dateMidnightUtcMillis);
    return dateMidnightUTCMillisFilter;
  }

  private ResponseStat createResponseStatFromQueryResult(Entity grpDateWhoStat) {
    if (grpDateWhoStat == null) {
      return null;
    }
    return new ResponseStat(
        (Long)grpDateWhoStat.getProperty(EXPERIMENT_ID_PROPERTY),
        (String)grpDateWhoStat.getProperty(EXPERIMENT_GROUP_NAME_PROPERTY),
        (String)grpDateWhoStat.getProperty(WHO_PROPERTY), 
        getDateProperty(grpDateWhoStat),
        convertToInt((Long)grpDateWhoStat.getProperty(SCHED_R_PROPERTY)),
        convertToInt((Long) grpDateWhoStat.getProperty(MISSED_R_PROPERTY)),
        convertToInt((Long) grpDateWhoStat.getProperty(SELF_R_PROPERTY)),
        getLastContactDateTime(grpDateWhoStat));
  }

  private int convertToInt(Long long1) {
    if (long1 != null) {
      return long1.intValue();
    }
    return 0;
  }

  private DateTime getLastContactDateTime(Entity whoResult) {
    Date property = (Date)whoResult.getProperty(LAST_CONTACT_DATE_TIME_PROPERTY);
    if (property != null) {
      return new DateTime(property, DateTimeZone.UTC);
    }
    return null;
  }

  


  private DateTime getDateProperty(Entity whoResult) {
    Long property = (Long)whoResult.getProperty(DATE_PROPERTY);
    if (property != null) {
      return new DateMidnight(property, DateTimeZone.UTC).toDateTime();
    }
    return null;
  }
  
//private List<FilterPredicate> createFiltersBoundingDate(DateTime utcLocalDate) {
//DateMidnight dateMidnight = utcLocalDate.toDateMidnight();
//Date beginningOfDay = dateMidnight.toDate();
//Date nextDay = dateMidnight.plusDays(1).toDate();
//FilterPredicate lowerDateBoundFilter = new FilterPredicate(DATE_PROPERTY, FilterOperator.GREATER_THAN_OR_EQUAL, beginningOfDay);
//FilterPredicate upperDateBoundFilter = new FilterPredicate(DATE_PROPERTY, FilterOperator.LESS_THAN, nextDay);
//List<FilterPredicate> dateBoundFilters = Lists.newArrayList(lowerDateBoundFilter, upperDateBoundFilter);
//return dateBoundFilters;
//}
//
//
//private Filter createDateFilter(DateTime date) {    
//DateTime utcLocalDate = LocalToUTCTimeZoneConverter.toTimeWithUTCZone(date);
//FilterPredicate lowerDateBoundFilter = new FilterPredicate(DATE_PROPERTY, FilterOperator.GREATER_THAN, utcLocalDate.minusDays(1).toDate());
//FilterPredicate upperDateBoundFilter = new FilterPredicate(DATE_PROPERTY, FilterOperator.GREATER_THAN, utcLocalDate.plusDays(1).toDate());
//return CompositeFilterOperator.and(lowerDateBoundFilter, upperDateBoundFilter);
//}

  
}
