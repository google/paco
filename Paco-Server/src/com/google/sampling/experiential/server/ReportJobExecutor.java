package com.google.sampling.experiential.server;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.DateTimeZone;

import com.google.appengine.api.ThreadManager;
import com.google.common.base.Strings;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.shared.EventDAO;

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
                             final List<Query> query, final boolean anon, final String reportFormat) {
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
          String location = doJob(requestorEmail, timeZoneForClient, query, anon, jobId, reportFormat);
          statusMgr.completeReport(requestorEmail, jobId, location);
        } catch (Throwable e) {
          statusMgr.failReport(requestorEmail, jobId, e.getClass() + "." + e.getMessage());
          log.severe("Could not run job: " + e.getMessage());
        }
      }
    });
    thread2.start();
    log.info("Leaving runReportJob");
    return jobId;
  }

  protected String doJob(String requestorEmail, DateTimeZone timeZoneForClient, List<Query> query, boolean anon, String jobId,
                         String reportFormat) throws IOException {
    log.info("starting doJob");
    String experimentId = null;
    for (Query query2 : query) {
      if (query2.getKey().equals("experimentId")) {
        experimentId = query2.getValue();
      }
    }

    // TODO - get rid of the offset and limit params and rewrite the eventretriever call to loop until all results are retrieved.
    log.info("Getting events for job: " + jobId);
    List<Event> events = EventRetriever.getInstance().getEvents(query, requestorEmail, timeZoneForClient, 0, 20000);
    EventRetriever.sortEvents(events);
    log.info("Got events for job: " + jobId);

    if (!Strings.isNullOrEmpty(reportFormat) && reportFormat.equals("csv")) {
      return generateCSVReport(anon, jobId, experimentId, events, timeZoneForClient);
    } else if (!Strings.isNullOrEmpty(reportFormat) && reportFormat.equals("photozip")) {
      return generatePhotoZip(jobId, experimentId, events, anon, timeZoneForClient);
    } else {
      return generateHtmlReport(timeZoneForClient, anon, jobId, experimentId, events);
    }
  }

  private String generatePhotoZip(String jobId, String experimentId, List<Event> events, boolean anon, DateTimeZone timeZoneForClient) {
      return new PhotoZipBlobWriter().writePhotoZipFile(anon, experimentId, events, jobId, timeZoneForClient.getID());
  }

  private String generateHtmlReport(DateTimeZone timeZoneForClient, boolean anon, String jobId, String experimentId,
                                    List<Event> events) throws IOException {
    if (!Strings.isNullOrEmpty(experimentId)) {
      String eodFile = generateEODHtml(anon, jobId, experimentId, events, timeZoneForClient.getID());
      if (eodFile != null) {
        return eodFile;
      }
    }
    return new HtmlBlobWriter().writeNormalExperimentEventsAsHtml(anon, events, jobId, experimentId, timeZoneForClient.getID());
  }

  private String generateEODHtml(boolean anon, String jobId, String experimentId, List<Event> events, String timeZoneForClient) throws IOException {
    log.info("Checking referred experiment for job: " + jobId);
    Experiment referredExperiment = ExperimentRetriever.getInstance().getReferredExperiment(Long.parseLong(experimentId));
    if (referredExperiment != null) {
      List<EventDAO> eodEventDAOs = EventRetriever.convertEventsToDAOs(events);
      List<EventDAO> dailyPingEodEventDAOs = new EndOfDayEventProcessor().breakEodResponsesIntoIndividualDailyEventResponses(eodEventDAOs);
      return new HtmlBlobWriter().writeEndOfDayExperimentEventsAsHtml(anon, jobId, experimentId, dailyPingEodEventDAOs, timeZoneForClient);
    }
    return null;
  }

  private String generateCSVReport(boolean anon, String jobId, String experimentId, List<Event> events, DateTimeZone clientTimezone)
                                                                                                       throws IOException {
    if (!Strings.isNullOrEmpty(experimentId)) {
      String eodFile = generateEODCSV(anon, jobId, experimentId, events, clientTimezone.getID());
      if (eodFile != null) {
        return eodFile;
      }
    }
    return new CSVBlobWriter().writeNormalExperimentEventsAsCSV(anon, events, jobId);
  }

  private String generateEODCSV(boolean anon, String jobId, String experimentId, List<Event> events, String clientTimezone) throws IOException {
    Experiment referredExperiment = ExperimentRetriever.getInstance().getReferredExperiment(Long.parseLong(experimentId));
    if (referredExperiment != null) {
      List<EventDAO> eodEventDAOs = EventRetriever.convertEventsToDAOs(events);
      List<EventDAO> dailyPingEodEventDAOs = new EndOfDayEventProcessor().breakEodResponsesIntoIndividualDailyEventResponses(eodEventDAOs);
      return new CSVBlobWriter().writeEndOfDayExperimentEventsAsCSV(anon, dailyPingEodEventDAOs, jobId, clientTimezone);
    }
    return null;
  }
}
