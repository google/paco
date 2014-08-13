package com.google.sampling.experiential.datastore;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.server.TimeUtil;

public class PublicExperimentList {

  public static final Logger log = Logger.getLogger(PublicExperimentList.class.getName());

  private static final String END_DATE_PROPERTY = "end_date";

  // if we are still running in the year 5000, I will be happy for this to break.
  // Appengine datastore queries are a bummer.
  private static final Date INFINITY = new DateTime().withYear(5000).withMonthOfYear(1).withDayOfMonth(1).toDate();

  public static String PUBLIC_EXPERIMENT_KIND = "public_experiment";

  public static void updatePublicExperimentsList(Experiment experiment, DateTime dateTime) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    if (experiment.getId() == null) {
      log.severe("Experiment must have an id to be versioned in history table.");
      throw new IllegalArgumentException("Experiments must have an id to be in the public experiments list");
    }

    Key key = KeyFactory.createKey(PUBLIC_EXPERIMENT_KIND, experiment.getId());
    Entity entity = new Entity(key);
    entity.setProperty(END_DATE_PROPERTY, getEndDateColumn(experiment));

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

      Key key = KeyFactory.createKey(PUBLIC_EXPERIMENT_KIND, experiment.getId());
      Entity entity = new Entity(key);
      entity.setProperty(END_DATE_PROPERTY, getEndDateColumn(experiment));

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



  public static List<Long> getPublicExperiments(String timezone) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(PUBLIC_EXPERIMENT_KIND);

    DateTime nowInUserTimezone = TimeUtil.getNowInUserTimezone(DateTimeZone.forID(timezone));
    String dateString = toDateString(nowInUserTimezone);
    Filter endDateFilter = new Query.FilterPredicate(END_DATE_PROPERTY,
                                                     FilterOperator.LESS_THAN,
                                                     dateString);
    query.setFilter(endDateFilter);
    QueryResultIterable<Entity> result = ds.prepare(query).asQueryResultIterable();
    List<Long> experimentIds = Lists.newArrayList();
    for (Entity entity : result) {
      Date endDateProperty = (Date)entity.getProperty(END_DATE_PROPERTY);
      if (!expired(endDateProperty, nowInUserTimezone)) {
        experimentIds.add(entity.getKey().getId());
      }
    }
    return experimentIds;
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
