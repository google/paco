package com.google.sampling.experiential.server;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.libideas.logging.shared.Log;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.What;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.TimeUtil;

public class EndOfDayEventProcessor {

  private static final Logger log = Logger.getLogger(EndOfDayEventProcessor.class.getName());
  
  private DateTimeFormatter jodaFormatter = DateTimeFormat.forPattern(TimeUtil.DATETIME_FORMAT).withOffsetParsed();
  
  /**
   * This method expects events to have their responses named with very particular names as set up by the
   * EndOfDayInputExecutor, e.g., <daily_event.getIdForTime()>_<daily_event.input_name> where getIdforTime
   * contains a full version of a date object as a string, and input name is the name of one of the inputs recorded
   * in the daily event. For example, 2013/03/14 22:56:00+0400_why. 
   * @param eodEvents
   * @return Map by idForTimes of daily events and a new eventdao with the responses for the end of day response corresponding to the timestamp of the daily event to which it refers.
   */
  public Map<Date, EventDAO> breakEventDAOsIntoDailyPingResponses(List<EventDAO> eodEvents) {
    // TODO The assumption here is that this is for one person's data.
    // if it is not, the events are likely to clash with each other.
    List<EventDAO> rawListOfSingleEodEvents = breakEodResponsesIntoIndividualDailyEventResponses(eodEvents);
    Map<Date, EventDAO> eventsByPingDate = Maps.newHashMap();
    for (EventDAO eventDAO : rawListOfSingleEodEvents) {
      String dailyTime = eventDAO.getWhat().get("daily_event");
      if (eventsByPingDate.get(dailyTime) != null) {
        log.info("There is already an event for this date! " + dailyTime);
      }
      Date dailyDateTime = jodaFormatter.parseDateTime(dailyTime).toDate();
      eventsByPingDate.put(dailyDateTime, eventDAO);
    }
    
    return eventsByPingDate;
  }

  public List<EventDAO> breakEodResponsesIntoIndividualDailyEventResponses(List<EventDAO> eodEvents) {
    List<EventDAO> rawListOfSingleEodEvents = Lists.newArrayList();
    TimeLogger.logTimestamp("breakEventsIntoDaily start");
    int eodEventCounter = 0;
    for (EventDAO eodEvent : eodEvents) {
      EventDAO currentEodEvent = null;
      
      Map<String, String> outputs = eodEvent.getWhat();
      List<String> itemNames = Lists.newArrayList(outputs.keySet());
      Collections.sort(itemNames);
      
      for (String itemName : itemNames) {
        if (itemName.equals(EventDAO.REFERRED_EXPERIMENT_INPUT_ITEM_KEY)) {
          continue;
        }
        String itemValue = outputs.get(itemName);
        
        int date_itemNameSeparatorIndex = itemName.indexOf("_");
        if (date_itemNameSeparatorIndex == -1) {
          continue;          
        }
        
        String dateStr = itemName.substring(0, date_itemNameSeparatorIndex);
        String inputName = itemName.substring(date_itemNameSeparatorIndex + 1);
                
        if (currentEodEvent == null || !currentEodEvent.getWhat().get("daily_event").equals(dateStr) ) {
//          if (currentEodEvent != null) {
//            log.info("currentDailyEventTime: " + currentEodEvent.getWhat().get("daily_event") + ", date = " + dateStr);
//          } else {
//            log.info("new currentEODEvent because last was null. dateStr = " + dateStr);
//          }
          Map<String, String> newWhat = Maps.newHashMap();
          newWhat.put("daily_event", dateStr);
          currentEodEvent = new EventDAO(eodEvent.getWho(), eodEvent.getWhen(), eodEvent.getExperimentName(), eodEvent.getLat(), eodEvent.getLon(),
                                  eodEvent.getAppId(), eodEvent.getPacoVersion(), newWhat, eodEvent.isShared(), eodEvent.getResponseTime(),
                                  eodEvent.getScheduledTime(), eodEvent.getBlobs(), eodEvent.getExperimentId(), eodEvent.getExperimentVersion(),
                                  eodEvent.getTimezone());          
          rawListOfSingleEodEvents.add(currentEodEvent);
        }
        currentEodEvent.getWhat().put(inputName, itemValue);
      }
      if (eodEventCounter % 1000 == 0) {
        TimeLogger.logTimestamp("EodEvents: " + eodEventCounter + ": ");
      }
      eodEventCounter++;
    }
    log.info("# of new Events for EOD: " + rawListOfSingleEodEvents.size());
    log.info("# of eodEvents traversed: " + eodEventCounter);
    return rawListOfSingleEodEvents;
  }

  /**
   * Version for Events instead of EventDAOs
   * TODO - Unify these two with a common type. 
   * @param eodEvents
   * @return
   */
  public Map<String, Map<String, EventDAO>> breakEventsIntoDailyPingResponsesGroupedByWho(List<Event> eodEvents2) {
    List<EventDAO> eodEvents = EventRetriever.convertEventsToDAOs(eodEvents2);
    List<EventDAO> individualEODEvents = breakEodResponsesIntoIndividualDailyEventResponses(eodEvents);
    Map<String, Map<String, EventDAO>> eventsByWhoByDate = Maps.newHashMap();
    for (EventDAO eventDAO : individualEODEvents) {
      String who = eventDAO.getWho();
      Map<String, EventDAO> whoseEventsByDate = eventsByWhoByDate.get(who);
      if (whoseEventsByDate == null) {
        whoseEventsByDate = Maps.newHashMap();
        eventsByWhoByDate.put(who, whoseEventsByDate);
      }
      
      String dailyTime = eventDAO.getWhat().get("daily_event");
      if (whoseEventsByDate.get(dailyTime) != null) {
        log.info("There is already an event for this date! " + who + ", " + dailyTime);
      }
      whoseEventsByDate.put(dailyTime, eventDAO);
    }
    return eventsByWhoByDate;
  }
  
}
