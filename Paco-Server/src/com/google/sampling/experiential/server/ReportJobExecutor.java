package com.google.sampling.experiential.server;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.DateTimeZone;

import com.google.appengine.api.ThreadManager;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.TimeUtil;

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
                             final List<Query> query, final boolean anon) {
    final String jobId = DigestUtils.md5Hex(requestorEmail + Long.toString(System.currentTimeMillis())); // TODO get a real id function for jobs
    
    final ClassLoader cl = getClass().getClassLoader();
    final Thread thread2 = ThreadManager.createBackgroundThread(new Runnable() {
      
      @Override
      public void run() {
        log.info("ReportJobExecutor running");
        Thread.currentThread().setContextClassLoader(cl);
        
        try {          
          statusMgr.startReport(requestorEmail, jobId);
          String location = doJob(requestorEmail, timeZoneForClient, query, anon, jobId);
          statusMgr.completeReport(requestorEmail, jobId, location);
        } catch (IOException e) {
          statusMgr.failReport(requestorEmail, jobId, e.getMessage());
          log.severe("Could not run job: " + e.getMessage());
        }
      }
      
    });
    thread2.start();
    return jobId;
  }



  protected String doJob(String requestorEmail, DateTimeZone timeZoneForClient, List<Query> query, boolean anon, String jobId) throws IOException {
    String experimentId = null;
    for (Query query2 : query) {
      if (query2.getKey().equals("experimentId")) {
        experimentId = query2.getValue();
      }
    }

    TimeLogger.logTimestamp("T1: PreEventRetrieval");
    List<Event> events = EventRetriever.getInstance().getEvents(query, requestorEmail, timeZoneForClient, 0, 20000);
    
    TimeLogger.logTimestamp("T2: PostEventRetrieval");
    
    if (!Strings.isNullOrEmpty(experimentId)) {
      Experiment referredExperiment = ExperimentRetriever.getInstance().getReferredExperiment(Long.parseLong(experimentId));
      if (referredExperiment != null) {
        TimeLogger.logTimestamp("T5: PreEventsBreakup");
        List<EventDAO> eodEventDAOs = EventRetriever.convertEventsToDAOs(events);
        List<EventDAO> dailyPingEodEventDAOs = new EndOfDayEventProcessor().breakEodResponsesIntoIndividualDailyEventResponses(eodEventDAOs);
        TimeLogger.logTimestamp("T5: PostEventsBreakup");
        return new CSVBlobWriter().writeEndOfDayExperimentEventsAsCSV(anon, dailyPingEodEventDAOs, jobId);
      }
    }
    return new CSVBlobWriter().writeNormalExperimentEventsAsCSV(anon, events, jobId);

  }
  


}
