package com.google.sampling.experiential.server;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.shared.TimeUtil;

public class ReportJobStatusManager {

  private static final Logger log = Logger.getLogger(ReportJobStatusManager.class.getName());

  private static final String REPORT_JOB_KIND = "report_job";

  private static final String ID_PROPERTY = "reportId";
  private static final String REQUESTOR_PROPERTY = "requester";
  private static final String STATUS_PROPERTY = "status";
  private static final String ERROR_MESSAGE_PROPERTY = "error_message";
  private static final String LOCATION_PROPERTY = "location";
  private static final String START_TIME_PROPERTY = "start_time";
  private static final String END_TIME_PROPERTY = "end_time";

  public static final int PENDING = 1;
  public static final int COMPLETE = 2;
  public static final int FAILED = 3;

  public ReportJobStatus isItDone(String who, String id) {
    if (who == null || id == null) {
      log.info("Invalid isItDone request");
      throw new IllegalArgumentException("Invalid report parameters for startReport");
    }
    Entity report = getReportById(id);
    if (report == null) {
      return null;
    }
    if (!isOriginalRequestor(who, id, report)) {
      return null; // no need to throw an error, why let on that there is a
                   // report by this id that someone else owns?
    }
    return report == null ? null : new ReportJobStatus((String)report.getProperty(ID_PROPERTY),
                                                 (String)report.getProperty(REQUESTOR_PROPERTY),
                                                 new Integer(((Long)report.getProperty(STATUS_PROPERTY)).intValue()),
                                                 (String)report.getProperty(START_TIME_PROPERTY),
                                                 (String)report.getProperty(END_TIME_PROPERTY),
                                                 (String)report.getProperty(LOCATION_PROPERTY),
                                                 (String)report.getProperty(ERROR_MESSAGE_PROPERTY));
  }

  private Entity getReportById(String id) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(REPORT_JOB_KIND);
    query.addFilter(ID_PROPERTY, FilterOperator.EQUAL, id);
    PreparedQuery preparedQuery = ds.prepare(query);
    Iterator<Entity> iterator = preparedQuery.asIterator();
    Entity user = null;
    if (iterator.hasNext()) {
      user = iterator.next();
    }
    return user;
  }

  public void startReport(String requestorEmail, String id) {
    if (requestorEmail == null || id == null) {
      log.info("Invalid startReport request");
      throw new IllegalArgumentException("Invalid report parameters for startReport");
    }

    requestorEmail = requestorEmail.toLowerCase();
    Entity report = getReportById(id);
    if (report == null) {
      DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
      Entity entity = new Entity(REPORT_JOB_KIND);
      entity.setProperty(ID_PROPERTY, id);
      entity.setProperty(REQUESTOR_PROPERTY, requestorEmail);
      entity.setProperty(STATUS_PROPERTY, PENDING);
      entity.setProperty(START_TIME_PROPERTY, getCurrentTimeAsString());
      ds.put(entity);
    } else {
      log.info("Report with id " + id + " already exists");
    }
  }

  private String getCurrentTimeAsString() {
    return DateTime.now().toString(TimeUtil.DATETIME_FORMAT);
  }

  public void completeReport(String requestorEmail, String id, String location) {
    if (requestorEmail == null || id == null || location == null) {
      log.info("Invalid completeReport request");
      throw new IllegalArgumentException("Invalid report parameters for completeReport");
    }
    requestorEmail = requestorEmail.toLowerCase();
    Entity report = getReportById(id);
    if (report == null) {
      log.info("No report found for id: " + id);
      return;
    }
    if (!isOriginalRequestor(requestorEmail, id, report)) {
      return;
    }

    report.setProperty(STATUS_PROPERTY, COMPLETE);
    report.setProperty(END_TIME_PROPERTY, getCurrentTimeAsString());
    if (!Strings.isNullOrEmpty(location)) {
      report.setProperty(LOCATION_PROPERTY, location);
    }

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    ds.put(report);
  }

  public void failReport(String requestorEmail, String id, String errorMessage) {
    if (requestorEmail == null || id == null) {
      log.info("Invalid failReport request");
      throw new IllegalArgumentException("Invalid report parameters for failReport");
    }

    Entity report = getReportById(id);
    if (report == null) {
      log.info("No report found for id: " + id);
      return;
    }
    if (!isOriginalRequestor(requestorEmail, id, report)) {
      return;
    }

    report.setProperty(STATUS_PROPERTY, FAILED);
    report.setProperty(END_TIME_PROPERTY, getCurrentTimeAsString());
    if (!Strings.isNullOrEmpty(errorMessage)) {
      report.setProperty(ERROR_MESSAGE_PROPERTY, errorMessage);
    }

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    ds.put(report);
  }

  private boolean isOriginalRequestor(String requestor, String id, Entity reportById) {
    boolean authorized = false;
    String originalRequestor = (String) reportById.getProperty(REQUESTOR_PROPERTY);
    if (!originalRequestor.equals(requestor)) {
      log.info("Unauthorized report job request. " + requestor + " tried to access report " + id + " owned by "
               + originalRequestor);
      authorized = false;
    } else {
      authorized = true;
    }
    return authorized;
  }

  public List<String> getAllReportStatus() {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Query q = new Query(REPORT_JOB_KIND);
    PreparedQuery preparedQuery = ds.prepare(q);

    List<String> reportProperties = Lists.newArrayList();
    QueryResultIterator<Entity> iterator = preparedQuery.asQueryResultIterator();
    while (iterator.hasNext()) {
      String id = (String) iterator.next().getProperty(ID_PROPERTY);
      String requestor = (String) iterator.next().getProperty(REQUESTOR_PROPERTY);
      String status = (String) iterator.next().getProperty(STATUS_PROPERTY);
      String start = (String) iterator.next().getProperty(START_TIME_PROPERTY);
      String end = (String) iterator.next().getProperty(END_TIME_PROPERTY);
      String location = (String) iterator.next().getProperty(LOCATION_PROPERTY);
      String error = (String) iterator.next().getProperty(ERROR_MESSAGE_PROPERTY);
      
      StringBuilder buf = new StringBuilder();
      buf.append(id).append(",");
      buf.append(id).append(",");
      buf.append(requestor).append(",");
      buf.append(status).append(",");
      buf.append(start).append(",");
      buf.append(end).append(",");
      buf.append(location).append(",");
      buf.append(error);
      reportProperties.add(status);
    }
    return reportProperties;
  }

}
