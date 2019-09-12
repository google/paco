package com.google.sampling.experiential.server;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.DateTimeZone;

import com.google.appengine.api.ThreadManager;
import com.google.common.base.Strings;
import com.google.sampling.experiential.gcs.GCSFetcher;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.server.stats.usage.UsageStatsBlobWriter;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.WhatDAO;
import com.pacoapp.paco.shared.model2.ExperimentDAO;

/**
 * Setup a job as a background thread to run a report.
 * This runs in a backend instance.
 * Also, update the ReportJobStatus.
 *
 * The reportservlet will ask this to kick off a job.
 * It will also ask it for the status, which will include pending, completed, failed.
 * If completed, the client can then access the report that was generated at a location.
 *
 * @author bobevans
 *
 */
public class ReportJobExecutor {

  private static final Logger log = Logger.getLogger(ReportJobExecutor.class.getName());

  private static ReportJobExecutor instance;

  public static ReportJobExecutor getInstance() {
    if (instance == null) {
      instance = new ReportJobExecutor();
    }
    return instance;
  }

  private ReportJobStatusManager statusMgr;

  public ReportJobExecutor() {
    super();
    statusMgr = new ReportJobStatusManager();
  }
  
  public String runReportJob(final String requestorEmail, final DateTimeZone timeZoneForClient,
                             final List<Query> query, final boolean anon, final String reportFormat,
                             final String originalQuery, final int limit, final String cursor, final boolean includePhotos,final Float pacoProtocol, final boolean fullBlobAddress) {
    // TODO get a real id function for jobs

    final String jobId = DigestUtils.md5Hex(requestorEmail + Long.toString(System.currentTimeMillis()));
    log.info("In runReportJob for job: " + jobId);
    statusMgr.startReport(requestorEmail, jobId);

    final ClassLoader cl = getClass().getClassLoader();
    final Thread thread2 = ThreadManager.createBackgroundThread(new Runnable() {
      @Override
      public void run() {
        log.info("ReportJobExecutor running");
        Thread.currentThread().setContextClassLoader(cl);
        try {
          String location = doJob(requestorEmail, timeZoneForClient, query, anon, jobId, reportFormat, originalQuery, limit, cursor, includePhotos, pacoProtocol, fullBlobAddress);
          statusMgr.completeReport(requestorEmail, jobId, location);
        } catch (Throwable e) {
          statusMgr.failReport(requestorEmail, jobId, e.getClass() + "." + e.getMessage());
          log.severe("Could not run job: " + e.getMessage());
          e.printStackTrace();
        }
      }
    });
    thread2.start();
    log.info("Leaving runReportJob");
    return jobId;
  }

  protected String doJob(String requestorEmail, DateTimeZone timeZoneForClient, List<Query> query, boolean anon, String jobId,
                         String reportFormat, String originalQuery, int limit, String cursor, boolean includePhotos, Float pacoProtocol, boolean fullBlobAddress) throws IOException {
    log.info("starting doJob");
    if (!Strings.isNullOrEmpty(reportFormat) && reportFormat.equals("stats")) {
      log.info("Running stats report for job: " + jobId);
      return runStatsReport(jobId, timeZoneForClient, requestorEmail);
    }

    String experimentId = null;
    for (Query query2 : query) {
      if (query2.getKey().equals("experimentId")) {
        experimentId = query2.getValue();
      }
    }


    if (!Strings.isNullOrEmpty(reportFormat) && reportFormat.equals("csv")) {
      // TODO - get rid of the offset and limit params and rewrite the eventretriever call to loop until all results are retrieved.
      log.info("Getting events for job: " + jobId);
      EventQueryResultPair eventQueryResultPair = EventRetriever.getInstance().getEventsInBatchesOneBatch(query, requestorEmail, timeZoneForClient, limit, cursor);
      //EventRetriever.sortEvents(events);
      log.info("Got events for job: " + jobId);

      return generateCSVReport(anon, jobId, experimentId, eventQueryResultPair, pacoProtocol, includePhotos);
    } else if (!Strings.isNullOrEmpty(reportFormat) && reportFormat.equals("json")) {
      // TODO - get rid of the offset and limit params and rewrite the eventretriever call to loop until all results are retrieved.
      log.info("Getting events for job: " + jobId);
      EventQueryResultPair eventQueryResultPair = EventRetriever.getInstance().getEventsInBatchesOneBatch(query, requestorEmail, timeZoneForClient, limit, cursor);
      //EventRetriever.sortEvents(events);
      log.info("Got events for job: " + jobId);

      return generateJsonReport(anon, jobId, experimentId, eventQueryResultPair, timeZoneForClient, includePhotos, pacoProtocol, fullBlobAddress);
    } else if (!Strings.isNullOrEmpty(reportFormat) && reportFormat.equals("photozip")) {
      // TODO - get rid of the offset and limit params and rewrite the eventretriever call to loop until all results are retrieved.
      log.info("Getting events for job: " + jobId);
      EventQueryResultPair eventQueryResultPair = EventRetriever.getInstance().getEventsInBatches(query, requestorEmail, timeZoneForClient, limit, cursor);
      //EventRetriever.sortEvents(events);
      log.info("Got events for job: " + jobId);

      return generatePhotoZip(jobId, experimentId, eventQueryResultPair, anon, timeZoneForClient);
    } else {
      // TODO - get rid of the offset and limit params and rewrite the eventretriever call to loop until all results are retrieved.
      log.info("Getting events for job: " + jobId);
      EventQueryResultPair eventQueryResultPair = EventRetriever.getInstance().getEventsInBatches(query, requestorEmail, timeZoneForClient, limit, cursor);
      //EventRetriever.sortEvents(events);
      log.info("Got events for job: " + jobId);

      return generateHtmlReport(timeZoneForClient, anon, jobId, experimentId, eventQueryResultPair, originalQuery, 
                                requestorEmail, pacoProtocol, includePhotos, fullBlobAddress);
    }
  }

  public String runReportJobExperimental(final String requestorEmail, final DateTimeZone timeZoneForClient,
                             final List<Query> query, final boolean anon, final String reportFormat,
                             final String originalQuery, final boolean includePhotos, final Float pacoProtocol, final boolean fullBlobAddress) {
    // TODO get a real id function for jobs

    final String jobId = DigestUtils.md5Hex(requestorEmail + Long.toString(System.currentTimeMillis()));
    log.info("In runReportJobExperimental for job: " + jobId);
    statusMgr.startReport(requestorEmail, jobId);

    final ClassLoader cl = getClass().getClassLoader();
    final Thread thread2 = ThreadManager.createBackgroundThread(new Runnable() {
      @Override
      public void run() {
        log.info("ReportJobExecutor Experimental running");
        Thread.currentThread().setContextClassLoader(cl);
        try {
          String location = doJobExperimental(requestorEmail, timeZoneForClient, query, anon, jobId, reportFormat, originalQuery, includePhotos, pacoProtocol, fullBlobAddress);
          statusMgr.completeReport(requestorEmail, jobId, location);
        } catch (Throwable e) {
          statusMgr.failReport(requestorEmail, jobId, e.getClass() + "." + e.getMessage());
          log.severe("Could not run job: " + e.getMessage());
          log.log(Level.SEVERE, "Could not run job", e);
          e.printStackTrace();
        }
      }
    });
    thread2.start();
    log.info("Leaving runReportJob");
    return jobId;
  }

  protected String doJobExperimental(String requestorEmail, DateTimeZone timeZoneForClient, List<Query> query, boolean anon, String jobId,
                         String reportFormat, String originalQuery, boolean includePhotos, Float pacoProtocol, boolean fullBlobAddress) throws IOException {
    log.info("starting doJob experimental");
    String experimentId = null;
    for (Query query2 : query) {
      if (query2.getKey().equals("experimentId")) {
        experimentId = query2.getValue();
      }
    }
    log.info("Getting events for job: " + jobId);
    EventQueryResultPair eventQueryResultPair = EventRetriever.getInstance().getEventsFromLowLevelDS(query,
                                                                                                     requestorEmail,
                                                                                                     timeZoneForClient);
    // EventRetriever.sortEvents(events);
    log.info("Got events for job: " + jobId);

    if (!Strings.isNullOrEmpty(reportFormat) && reportFormat.equals("csv2")) {
      return generateCSVReport(anon, jobId, experimentId, eventQueryResultPair, pacoProtocol, includePhotos);
    } else if (!Strings.isNullOrEmpty(reportFormat) && reportFormat.equals("json2")) {
      return generateJsonReport(anon, jobId, experimentId, eventQueryResultPair, timeZoneForClient, includePhotos, pacoProtocol, fullBlobAddress);
    } else if (!Strings.isNullOrEmpty(reportFormat) && reportFormat.equals("html2")) {
      return generateHtmlReport(timeZoneForClient, anon, jobId, experimentId, eventQueryResultPair, originalQuery,
                                requestorEmail, pacoProtocol, includePhotos, fullBlobAddress);
    }
    return null;

  }

  protected String doJobExperimentalSplitLargeFilesAndCompose(String requestorEmail, DateTimeZone timeZoneForClient,
                                                              List<Query> query, boolean anon, String jobId,
                                                              String reportFormat, String originalQuery,
                                                              boolean includePhotos, Float pacoProtocol, boolean fullBlobAddress) throws IOException {
    log.info("starting doJob split large files");
    String experimentId = null;
    for (Query query2 : query) {
      if (query2.getKey().equals("experimentId")) {
        experimentId = query2.getValue();
      }
    }

    if (!Strings.isNullOrEmpty(reportFormat) && reportFormat.equals("csv2")) {
      log.info("Getting events for csv job: " + jobId);
      EventQueryResultPair eventQueryResultPair = EventRetriever.getInstance().getEventsFromLowLevelDS(query,
                                                                                                       requestorEmail,
                                                                                                       timeZoneForClient);
      log.info("Got events for job: " + jobId);
      return generateCSVReport(anon, jobId, experimentId, eventQueryResultPair, pacoProtocol, includePhotos);
    } else if (!Strings.isNullOrEmpty(reportFormat) && reportFormat.equals("json2")) {
      log.info("Getting events for json job: " + jobId);
      EventQueryResultPair eventQueryResultPair = EventRetriever.getInstance().getEventsFromLowLevelDS(query,
                                                                                                       requestorEmail,
                                                                                                       timeZoneForClient);
      log.info("Got events for job: " + jobId);
      return generateJsonReport(anon, jobId, experimentId, eventQueryResultPair, timeZoneForClient, includePhotos, pacoProtocol, fullBlobAddress);
    } else if (!Strings.isNullOrEmpty(reportFormat) && reportFormat.equals("html2")) {
      return generateHtmlReportSplitLargeFiles(requestorEmail, timeZoneForClient, query, anon, jobId, originalQuery,
                                             experimentId, pacoProtocol, includePhotos, fullBlobAddress);
    }
    return null;

  }

  private String generateHtmlReportSplitLargeFiles(String requestorEmail, DateTimeZone timeZoneForClient,
                                                 List<Query> query, boolean anon, String jobId, String originalQuery,
                                                 String experimentId, Float pacoProtocol, boolean inlineBlobs, boolean fullBlobAddress) throws IOException {
    log.info("Getting events for html job: " + jobId);
    EventQueryResultPair eventQueryResultPair = EventRetriever.getInstance().getEventsFromLowLevelDS(query,
                                                                                                     requestorEmail,
                                                                                                     timeZoneForClient);
    log.info("Got events for job: " + jobId);
//    if (!Strings.isNullOrEmpty(experimentId)) {
//      String eodFile = generateEODHtml(anon, jobId, experimentId, eventQueryResultPair, timeZoneForClient.getID());
//      if (eodFile != null) {
//        return eodFile;
//      }
//    }
    return new HtmlBlobWriter().writeNormalExperimentEventsAsHtml(anon, eventQueryResultPair, jobId, experimentId,
                                                                  timeZoneForClient.getID(), originalQuery, requestorEmail, pacoProtocol, inlineBlobs, fullBlobAddress);
  }

  private String generateJsonReport(boolean anon, String jobId, String experimentId,
                                    EventQueryResultPair eventQueryResultPair, DateTimeZone timeZoneForClient,
                                    boolean includePhotos, Float pacoProtocol, boolean fullBlobAddress) throws IOException {
    return new JSONBlobWriter().writeEventsAsJSON(anon, eventQueryResultPair, jobId, timeZoneForClient, includePhotos, pacoProtocol, fullBlobAddress);

  }

  // for json query - dup of frontend version
//  private EventQueryResultPair getEventsWithQuery(HttpServletRequest req,
//                                                  List<com.google.sampling.experiential.server.Query> queries,
//                                                  int limit, String cursor) {
//    User whoFromLogin = AuthUtil.getWhoFromLogin();
//    return EventRetriever.getInstance().getEventsInBatches(queries, whoFromLogin.getEmail().toLowerCase(),
//                                                           TimeUtil.getTimeZoneForClient(req), limit, cursor);
//  }

  private String runStatsReport(String jobId, DateTimeZone timeZoneForClient, String requestorEmail) throws IOException {
    String tz = timeZoneForClient != null ? timeZoneForClient.getID() : null;
    return new UsageStatsBlobWriter().writeStatsAsJson(jobId, tz, requestorEmail);
  }

  private String generatePhotoZip(String jobId, String experimentId, EventQueryResultPair eventQueryResultPair, boolean anon, DateTimeZone timeZoneForClient) {
      return new PhotoZipBlobWriter().writePhotoZipFile(anon, experimentId, eventQueryResultPair, jobId, timeZoneForClient.getID());
  }

  private String generateHtmlReport(DateTimeZone timeZoneForClient, boolean anon, String jobId, String experimentId,
                                    EventQueryResultPair eventQueryResultPair, String originalQuery, String requestorEmail, Float pacoProtocol, 
                                    boolean inlineBlobs, boolean fullBlobAddress) throws IOException {
    if (!Strings.isNullOrEmpty(experimentId)) {
      String eodFile = generateEODHtml(anon, jobId, experimentId, eventQueryResultPair, timeZoneForClient.getID(), pacoProtocol);
      if (eodFile != null) {
        return eodFile;
      }
    }
    return new HtmlBlobWriter().writeNormalExperimentEventsAsHtml(anon, eventQueryResultPair, jobId, experimentId, 
                                                                  timeZoneForClient.getID(), originalQuery, requestorEmail, 
                                                                  pacoProtocol, inlineBlobs, fullBlobAddress);
  }

  private String generateEODHtml(boolean anon, String jobId, String experimentId, EventQueryResultPair eventQueryResultPair, String timeZoneForClient, Float pacoProtocol) throws IOException {
    log.info("Checking referred experiment for job: " + jobId);
    ExperimentDAO referredExperiment = getReferredExperiment(experimentId);
    if (referredExperiment != null) {
      List<EventDAO> eodEventDAOs = EventRetriever.convertEventsToDAOs(eventQueryResultPair.getEvents());
      List<EventDAO> dailyPingEodEventDAOs = new EndOfDayEventProcessor().breakEodResponsesIntoIndividualDailyEventResponses(eodEventDAOs);
      return new HtmlBlobWriter().writeEndOfDayExperimentEventsAsHtml(anon, jobId, experimentId, dailyPingEodEventDAOs, timeZoneForClient, pacoProtocol);
    }
    return null;
  }

  private ExperimentDAO getReferredExperiment(String experimentId) {
    return ExperimentServiceFactory.getExperimentService().getReferredExperiment(Long.parseLong(experimentId));
  }

  private String generateCSVReport(boolean anon, String jobId, String experimentId, EventQueryResultPair eventQueryResultPair, Float pacoProtocol, boolean inlineBlobs)
                                                                                                       throws IOException {
    List<Event> events = eventQueryResultPair.getEvents();
    for (Iterator iterator = events.iterator(); iterator.hasNext();) {
      Event event2 = (Event) iterator.next();
      if (inlineBlobs) {
        // legacy GAE DS blob storage
        List<WhatDAO> whatMap = EventRetriever.convertToWhatDAOs(event2.getWhat());
        EventJsonDownloader.fillInResponsesWithEncodedBlobData(event2, whatMap);
        // new GCS blob storage
        GCSFetcher.fillInResponsesWithEncodedBlobDataFromGCS(whatMap);
      }  
    }
    
    if (!Strings.isNullOrEmpty(experimentId)) {
      String eodFile = generateEODCSV(anon, jobId, experimentId, events, pacoProtocol);
      if (eodFile != null) {
        return eodFile;
      }
    }
    List<EventDAO> eodEventDAOs = EventRetriever.convertEventsToDAOs(events);
    try {
      Long experimentIdLong = Long.parseLong(experimentId);
      ExperimentService es = ExperimentServiceFactory.getExperimentService();
      ExperimentDAO experiment = es.getExperiment(experimentIdLong);

      return new CSVBlobWriter().writeNormalExperimentEventsAsCSV(experiment, eodEventDAOs, jobId, anon, pacoProtocol);
    } catch (NumberFormatException e) {
      log.warning("ExperimentId is not a long: " + experimentId);
      throw e;
    }
  }

  private String generateEODCSV(boolean anon, String jobId, String experimentId, List<Event> events, Float pacoProtocol) throws IOException {
    ExperimentDAO referredExperiment = getReferredExperiment(experimentId);
    if (referredExperiment != null) {
      List<EventDAO> eodEventDAOs = EventRetriever.convertEventsToDAOs(events);
      List<EventDAO> dailyPingEodEventDAOs = new EndOfDayEventProcessor().breakEodResponsesIntoIndividualDailyEventResponses(eodEventDAOs);
      return new CSVBlobWriter().writeEndOfDayExperimentEventsAsCSV(anon, dailyPingEodEventDAOs, jobId, pacoProtocol);
    }
    return null;
  }
}
