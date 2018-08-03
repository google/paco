package com.google.sampling.experiential.datastore;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Transaction;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.server.TimeUtil;
import com.google.sampling.experiential.server.stats.participation.ParticipationStatsService;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.scheduling.ActionScheduleGenerator;

public class PublicExperimentList {

  public static final Logger log = Logger.getLogger(PublicExperimentList.class.getName());

  private static final String END_DATE_PROPERTY = "end_date";
  private static final String STATS_PARTICIPANTS_PROPERTY ="stats_participants";
  private static final String MODIFY_DATE_PROPERTY = "modify_date";

  // if we are still running in the year 5000, I will be happy for this to break.
  // Appengine datastore queries are a bummer.
  private static final Date INFINITY = new DateTime().withYear(5000).withMonthOfYear(1).withDayOfMonth(1).toDate();

  public static String PUBLIC_EXPERIMENT_KIND = "public_experiment";

  public static void updatePublicExperimentsList(/*Transaction tx,*/ DatastoreService ds,
                                                 ExperimentDAO experiment, Key experimentKey, DateTime dateTime) {
    if (experiment.getId() == null) {
      log.severe("Experiment must have an id to be published publicly.");
      throw new IllegalArgumentException("Experiments must have an id to be in the public experiments list");
    }

    ParticipationStatsService ps = new ParticipationStatsService(); //Used to count number of participants (then updated through a cron job)

    Key existingKey = KeyFactory.createKey(PUBLIC_EXPERIMENT_KIND, experimentKey.getId());
    Entity existingPublicAcl = null;
    try {
      existingPublicAcl = ds.get(/*tx,*/ existingKey);
    } catch (EntityNotFoundException e) {
    } 
    Entity entity = new Entity(PUBLIC_EXPERIMENT_KIND, experimentKey.getId());
    entity.setProperty(END_DATE_PROPERTY, getEndDateColumn(experiment));
    entity.setProperty(STATS_PARTICIPANTS_PROPERTY, ps.getTotalByParticipant( experiment.getId() ).size());
    entity.setProperty(MODIFY_DATE_PROPERTY, com.pacoapp.paco.shared.util.TimeUtil.formatDate(new Date().getTime())); //Update the modify date - used for experiment hub - "new"
    if (!ActionScheduleGenerator.isOver(dateTime, experiment) && experiment.getPublished() && experiment.getPublishedUsers().isEmpty()) {
      ds.put(/*tx, */entity);
    } else if (existingPublicAcl != null) {
      ds.delete(/*tx,*/ existingKey);
    }
  }

  public static void deletePublicExperiment(Transaction tx, DatastoreService ds, Key experimentKey) {
    Key key = KeyFactory.createKey(PUBLIC_EXPERIMENT_KIND, experimentKey.getId());
    ds.delete(tx, key);
  }

  public static void deletePublicExperiments(Transaction tx, DatastoreService ds, List<Long> experimentIds) {
    List<Key> keys = Lists.newArrayList();
    for (Long experimentId : experimentIds) {
      Key key = KeyFactory.createKey(PUBLIC_EXPERIMENT_KIND, experimentId);
      keys.add(key);
    }

    ds.delete(tx, keys);
  }

  @Deprecated
  public static void updatePublicExperimentsList(Experiment experiment, DateTime dateTime) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    if (experiment.getId() == null) {
      log.severe("Experiment must have an id to be published publicly.");
      throw new IllegalArgumentException("Experiments must have an id to be in the public experiments list");
    }

    ParticipationStatsService ps = new ParticipationStatsService(); //Used to count number of participants (then updated through a cron job)

    Key key = KeyFactory.createKey(PUBLIC_EXPERIMENT_KIND, experiment.getId());
    Entity entity = new Entity(key);
    entity.setProperty(END_DATE_PROPERTY, getEndDateColumn(experiment));
    entity.setProperty(STATS_PARTICIPANTS_PROPERTY, ps.getTotalByParticipant( experiment.getId() ).size());
    entity.setProperty(MODIFY_DATE_PROPERTY, com.pacoapp.paco.shared.util.TimeUtil.formatDate(new Date().getTime())); //Update the modify date - used for experiment hub - "new"

    if (!experiment.isOver(dateTime) && experiment.isPublic()) {
      ds.put(entity);
    } else {
      ds.delete(key);
    }
  }

  public static void updatePublicExperimentsList(List<Experiment> experiments, DateTime dateTime) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    for (Experiment experiment : experiments) {
      if (experiment.getId() == null) {
        log.severe("Experiment must have an id to be versioned in history table.");
        throw new IllegalArgumentException("Experiments must have an id to be in the public experiments list");
      }

      ParticipationStatsService ps = new ParticipationStatsService(); //Used to count number of participants (then updated through a cron job)

      Key key = KeyFactory.createKey(PUBLIC_EXPERIMENT_KIND, experiment.getId());
      Entity entity = new Entity(key);
      entity.setProperty(END_DATE_PROPERTY, getEndDateColumn(experiment));
      entity.setProperty(STATS_PARTICIPANTS_PROPERTY, ps.getTotalByParticipant( experiment.getId() ).size());
      entity.setProperty(MODIFY_DATE_PROPERTY, com.pacoapp.paco.shared.util.TimeUtil.formatDate(new Date().getTime())); //Update the modify date - used for experiment hub - "new"

      if (!experiment.isOver(dateTime) && experiment.isPublic()) {
        ds.put(entity);
      } else {
        ds.delete(key);
      }
    }
  }



  private static Date getEndDateColumn(Experiment experiment) {
    return experiment.getEndDateAsDate() != null ? experiment.getEndDateAsDate() : INFINITY;
  }

  private static Date getEndDateColumn(ExperimentDAO experiment) {
    final DateTime lastEndTime = ActionScheduleGenerator.getLastEndTime(experiment);
    return lastEndTime != null ? lastEndTime.toDate() : INFINITY;
  }

  public static class CursorExerimentIdListPair {
    public String cursor;
    public List<Long> ids;
    public CursorExerimentIdListPair(String cursor, List<Long> ids) {
      super();
      this.cursor = cursor;
      this.ids = ids;
    }

  }

  public static CursorExerimentIdListPair getPublicExperiments(String timezone, Integer limit, String cursor) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(PUBLIC_EXPERIMENT_KIND);

    DateTime nowInUserTimezone = TimeUtil.getNowInUserTimezone(DateTimeZone.forID(timezone));
    String dateString = toDateString(nowInUserTimezone);
    Filter endDateFilter = new Query.FilterPredicate(END_DATE_PROPERTY,
                                                     FilterOperator.GREATER_THAN,
                                                     nowInUserTimezone.toDate());
    query.setFilter(endDateFilter);
    FetchOptions options = FetchOptions.Builder.withDefaults();
    if (limit != null) {
      options.limit(limit);
    }
    if (!Strings.isNullOrEmpty(cursor) && !"null".equals(cursor)) {
      options.startCursor(Cursor.fromWebSafeString(cursor));
    }
    QueryResultList<Entity> result = ds.prepare(query).asQueryResultList(options);
    List<Long> experimentIds = Lists.newArrayList();
    for (Entity entity : result) {
      Date endDateProperty = (Date)entity.getProperty(END_DATE_PROPERTY);
      if (!expired(endDateProperty, nowInUserTimezone)) {
        experimentIds.add(entity.getKey().getId());
      }
    }
    return new CursorExerimentIdListPair(result.getCursor().toWebSafeString(), experimentIds);
  }

  public static CursorExerimentIdListPair getPublicExperimentsNew(String timezone, Integer limit, String cursor) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(PUBLIC_EXPERIMENT_KIND);

    DateTime nowInUserTimezone = TimeUtil.getNowInUserTimezone(DateTimeZone.forID(timezone));
    String dateString = toDateString(nowInUserTimezone);

    //Sort by modifyDate DESC (specifies the "newness")
    query.addSort(MODIFY_DATE_PROPERTY, Query.SortDirection.DESCENDING);

    FetchOptions options = FetchOptions.Builder.withDefaults();
    if (limit != null) {
      options.limit(limit);
    }
    if (!Strings.isNullOrEmpty(cursor) && !"null".equals(cursor)) {
      options.startCursor(Cursor.fromWebSafeString(cursor));
    }
    QueryResultList<Entity> result = ds.prepare(query).asQueryResultList(options);
    List<Long> experimentIds = Lists.newArrayList();
    for (Entity entity : result) {
      Date endDateProperty = (Date)entity.getProperty(END_DATE_PROPERTY);
      if (!expired(endDateProperty, nowInUserTimezone)) {
        experimentIds.add(entity.getKey().getId());
      }
    }
    return new CursorExerimentIdListPair(result.getCursor().toWebSafeString(), experimentIds);
  }

  public static CursorExerimentIdListPair getPublicExperimentsPopular(String timezone, Integer limit, String cursor) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(PUBLIC_EXPERIMENT_KIND);

    DateTime nowInUserTimezone = TimeUtil.getNowInUserTimezone(DateTimeZone.forID(timezone));

    Filter popularityFilter = new Query.FilterPredicate(STATS_PARTICIPANTS_PROPERTY,
            FilterOperator.GREATER_THAN,
            0L); //At least 1 participant!
    query.setFilter(popularityFilter);
    query.addSort(STATS_PARTICIPANTS_PROPERTY, Query.SortDirection.DESCENDING); //Sort DESC by participants

    FetchOptions options = FetchOptions.Builder.withDefaults();
    if (limit != null) {
      options.limit(limit);
    }
    if (!Strings.isNullOrEmpty(cursor) && !"null".equals(cursor)) {
      options.startCursor(Cursor.fromWebSafeString(cursor));
    }
    QueryResultList<Entity> result = ds.prepare(query).asQueryResultList(options);
    List<Long> experimentIds = Lists.newArrayList();

    for (Entity entity : result) {
      Date endDateProperty = (Date) entity.getProperty(END_DATE_PROPERTY);
      if (!expired(endDateProperty, nowInUserTimezone)) {
        experimentIds.add(entity.getKey().getId());
      }
    }

    return new CursorExerimentIdListPair(result.getCursor().toWebSafeString(), experimentIds);
  }

  public static boolean isPublicExperiment(Long experimentId) {
    Key key = KeyFactory.createKey(PUBLIC_EXPERIMENT_KIND, experimentId);
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(PUBLIC_EXPERIMENT_KIND);
    Entity result;
    try {
      result = ds.get(key);
      return result != null;
    } catch (EntityNotFoundException e) {
    }
    return false;
  }


  private static boolean expired(Date endDateProperty, DateTime nowInUserTimezone) {
    try {
      DateTime endDateTime = new DateTime(endDateProperty.getTime());
      return endDateTime.isBefore(nowInUserTimezone);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false; // false because it was not a date
  }

  private static String toDateString(DateTime userTime) {
    return userTime.getYear() + "," + userTime.getMonthOfYear() + ", " + userTime.getDayOfMonth();
  }

  public static void deletePublicExperiment(Experiment experiment) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Key key = KeyFactory.createKey(PUBLIC_EXPERIMENT_KIND, experiment.getId());
    ds.delete(key);
  }



}
