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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.dao.CSEventOutputDao;
import com.google.sampling.experiential.dao.CSFailedEventDao;
import com.google.sampling.experiential.dao.impl.CSEventOutputDaoImpl;
import com.google.sampling.experiential.dao.impl.CSFailedEventDaoImpl;
import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.util.Constants;
import com.pacoapp.paco.shared.util.ErrorMessages;
import com.pacoapp.paco.shared.util.SearchUtil;

import net.sf.jsqlparser.JSQLParserException;

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
    String getQueryForEventIdSql = null;
    List<Long> fixedList = Lists.newArrayList();
    List<Long> notFixedList = Lists.newArrayList();
    List<Long> mysteryList = Lists.newArrayList();
    
    CSFailedEventDao failedDaoImpl = new CSFailedEventDaoImpl();
    CSEventOutputDao eventOutputDaoImpl = new CSEventOutputDaoImpl();
    
    try {
      // Get failed events of reprocessing status false
      Map<Long, String> failedEvents = failedDaoImpl.getFailedEvents();
      boolean withOutputs = false;
      Boolean oldColumnName = true;
      for ( Long failedId : failedEvents.keySet()) {
        id = failedId;
        final JSONObject currentEvent = new JSONObject(failedEvents.get(failedId));
        
        // find the id  from failed json
        if (currentEvent.has(Constants.ID)) {
          // check whether failed json id is there in events table
          getQueryForEventIdSql = SearchUtil.getQueryForEventRetrieval(currentEvent.getString(Constants.ID));
          List<EventDAO> evtList = eventOutputDaoImpl.getEvents(getQueryForEventIdSql, withOutputs, oldColumnName);
          if (evtList.size() == 0) {
            toBeFixed.put(eventId, false);
            // send it to cs insert
            String results = EventJsonUploadProcessor.create().processJsonEvents(true, failedEvents.get(failedId),
                                                                                 null, null,null);
            // verify whether it is there in events table
            getQueryForEventIdSql = SearchUtil.getQueryForEventRetrieval(currentEvent.getString(Constants.ID));
            List<EventDAO> evts = eventOutputDaoImpl.getEvents(getQueryForEventIdSql, withOutputs, oldColumnName);
            if (evts.size() >0) {
              toBeFixed.put(eventId, true);
              failedDaoImpl.updateFailedEventsRetry(id, Constants.TRUE);
            }
          } else {
            log.warning("It is in failed Events table and also in events table"+ eventId);
            failedDaoImpl.updateFailedEventsRetry(id, "resolved");
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
        failedDaoImpl.updateFailedEventsRetry(id, Constants.TRUE);
      } catch (SQLException sqle) {
        log.warning(ErrorMessages.SQL_EXCEPTION +  sqle.getMessage());
      }
    } catch (JSQLParserException e) {
      log.warning(ErrorMessages.JSQL_PARSER_EXCEPTION.getDescription() + " : " + ExceptionUtil.getStackTraceAsString(e));
    } catch (Exception e) {
      log.warning(ErrorMessages.GENERAL_EXCEPTION.getDescription() + " : " + ExceptionUtil.getStackTraceAsString(e));
    } 
    resp.getWriter().println("success");
  }

  private void setCharacterEncoding(HttpServletRequest req,
                                    HttpServletResponse resp) throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }

}