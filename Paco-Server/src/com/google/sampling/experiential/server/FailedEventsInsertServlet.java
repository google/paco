package com.google.sampling.experiential.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.labs.repackaged.com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.util.Constants;
import com.pacoapp.paco.shared.util.ErrorMessages;


@SuppressWarnings("serial")
public class FailedEventsInsertServlet extends HttpServlet {
  public static final Logger log = Logger.getLogger(FailedEventsInsertServlet.class.getName());
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    doPost(req, resp);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    Map<Long,Boolean> toBeFixed = Maps.newHashMap();
    setCharacterEncoding(req, resp);
    Long id = null;
    Long eventId = null;
    List<Long> fixedList = Lists.newArrayList();
    List<Long> notFixedList = Lists.newArrayList();
    List<Long> mysteryList = Lists.newArrayList();
    
    CloudSQLDaoImpl sqlDao = new CloudSQLDaoImpl();
    try {

      // Get failed events of reprocessing status false
      Map<Long, String> failedEvents = sqlDao.getFailedEvents();
      for ( Long failedId : failedEvents.keySet()) {
        id = failedId;
        final JSONObject currentEvent = new JSONObject(failedEvents.get(failedId));
        // find the id  from failed json
        if (currentEvent.has("id")) {
          eventId = Long.parseLong(currentEvent.getString("id"));
          // check whether failed json id is there in events table
          List<EventDAO> evtList = sqlDao.getEvents("select * from events where _id="+eventId, null);
          if (evtList.size() == 0) {
            toBeFixed.put(eventId, false);
            // send it to cs insert
            String results = EventJsonUploadProcessor.create().processJsonEvents(true, failedEvents.get(failedId),
                                                                                 null, null,null);
            sqlDao.updateFailedEventsRetry(id, Constants.TRUE);
            // verify whether it is there in events table
            List<EventDAO> evts = sqlDao.getEvents("select * from events where _id="+eventId, null);
            if (evts.size() >0) {
              toBeFixed.put(eventId, true);
            }
          } else {
            log.warning("It is in failed Events table and also in events table"+ eventId);
            mysteryList.add(eventId);
          }
        }  
      } // for loop    
      // list of events fixed and not fixed in this iteration
      for (Long s : toBeFixed.keySet()) {
        if (toBeFixed.get(s) == true) {
          fixedList.add(s);
        } else {
          notFixedList.add(s);
        }
      }
      log.info("To be fixed"+ toBeFixed.keySet());
      log.info("FixedList"+ fixedList);
      log.info("NotFixedList"+ notFixedList);
      log.info("Mystery List(Present in both events table and failed events table. So we are not processing these event ids : )"+ mysteryList);
    } catch (SQLException | JSONException | ParseException e) {
      try {
        sqlDao.updateFailedEventsRetry(id, Constants.TRUE);
      } catch (SQLException sqle) {
        log.warning(ErrorMessages.SQL_EXCEPTION +  sqle.getMessage());
      }
    } 
    resp.getWriter().println("success");
  }

  private void setCharacterEncoding(HttpServletRequest req,
                                    HttpServletResponse resp) throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }

}