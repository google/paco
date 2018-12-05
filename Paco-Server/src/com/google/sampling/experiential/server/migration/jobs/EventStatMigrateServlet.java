package com.google.sampling.experiential.server.migration.jobs;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskAlreadyExistsException;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.sampling.experiential.server.stats.participation.ParticipationStatsService;

public class EventStatMigrateServlet extends HttpServlet {
  public static final Logger log = Logger.getLogger(EventStatMigrateServlet.class.getName());
  private static final int TASK_SIZE = 10;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    
    String jobId = req.getParameter("jobId");
    
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery q = ds.prepare(new Query("Event"));
    String cursorStr = req.getParameter("cursor");
    FetchOptions options = FetchOptions.Builder.withLimit(TASK_SIZE);
    if (cursorStr != null) {
      Cursor cursor = Cursor.fromWebSafeString(cursorStr);
      options = options.startCursor(cursor);
    }
    
    QueryResultList<Entity> results = q.asQueryResultList(options);
    
    
    ParticipationStatsService ps = new ParticipationStatsService();
    for (Entity eventEntity : results) {
      
      
      boolean isJoinOrScheduleEvent = false;
      List<String> keysList = (List<String>)eventEntity.getProperty("keysList");
      if (keysList != null && (keysList.contains("joined") || keysList.contains("schedule"))) {
        isJoinOrScheduleEvent = true;
      }
      
      if (!isJoinOrScheduleEvent) {
        String experimentIdStr = (String)eventEntity.getProperty("experimentId");
        Long experimentId = Long.parseLong(experimentIdStr);
        String experimentGroupName = (String)eventEntity.getProperty("experimentGroupName");
        String who = (String)eventEntity.getProperty("who");
        
        Date rt = (Date)eventEntity.getProperty("responseTime");
        Date st = (Date)eventEntity.getProperty("scheduledTime");
        
        if (st != null && rt != null) {
          DateTime dateTime = new DateTime(st);
          log.info(jobId + ": Updating scheduled response Count for " + experimentId + ", " + experimentGroupName + ", " + who + ", " + dateTime.toString());
          ps.updateScheduledResponseCountForWho(experimentId, experimentGroupName, who, dateTime);
        } else if (st != null && rt == null) {
          DateTime dateTime = new DateTime(st);
          log.info(jobId + ": Updating missed response Count for " + experimentId + ", " + experimentGroupName + ", " + who + ", " + dateTime.toString());
          ps.updateMissedResponseCountForWho(experimentId, experimentGroupName, who, dateTime);
        } else if (st == null && rt != null) {        
          DateTime dateTimeRt = new DateTime(rt);
          log.info(jobId + ": Updating selfreport response Count for " + experimentId + ", " + experimentGroupName
                   + ", " + who + ", " + dateTimeRt.toString());
          ps.updateSelfResponseCountForWho(experimentId, experimentGroupName, who, dateTimeRt);
        }
      } else {
        log.info(jobId + ": join event");
      }
    }
    
    // queue next cursor
    
    Cursor newCursor = results.getCursor();
    String newCursorStr = null;
    if (results.size() == TASK_SIZE) {
      newCursorStr = newCursor.toWebSafeString();
      
//      ModulesService modulesApi = ModulesServiceFactory.getModulesService();
//      String backendAddress = modulesApi.getVersionHostname("reportworker", modulesApi.getDefaultVersion("reportworker"));
//      String moduleAddress = backendAddress + "/_ah/queue/migration";
//      log.info("Queue address: " + moduleAddress);
      
      Queue taskQueue = QueueFactory.getQueue("migration");
      
      String taskName = jobId + "_" + newCursorStr;
      
      taskName = taskName.replaceAll("[^a-zA-Z0-9_-]", "_");
      log.info("Launching new sub Task = " + taskName);
      
      TaskOptions taskOptions = TaskOptions.Builder
        //            .withUrl(moduleAddress)
                    .withTaskName(taskName)
                    .param("jobId", jobId)
                    .param("cursor", newCursorStr);
      
      try {
        taskQueue.add(taskOptions);
      } catch (TaskAlreadyExistsException e) {
        log.warning("Got exception adding task to queue: " + e.getMessage());
        throw e;
      }
      
    }


  }

}
