package com.google.sampling.experiential.server;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.collect.Maps;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.TimeUtil;

public class EndOfDayEventProcessor {

  private DateTimeFormatter jodaFormatter = DateTimeFormat.forPattern(TimeUtil.DATETIME_FORMAT).withOffsetParsed();
  
  /**
   * This method expects events to have their responses named with very particular names as set up by the
   * EndOfDayInputExecutor, e.g., <daily_event.getIdForTime()>_<daily_event.input_name> where getIdforTime
   * contains a full version of a date object as a string, and input name is the name of one of the inputs recorded
   * in the daily event. For example, 2013/03/14 22:56:00+0400_why. 
   * @param eodEvents
   * @return Map by idForTimes of daily events and a new eventdao with the responses for the end of day response corresponding to the timestamp of the daily event to which it refers.
   */
  public Map<Date, EventDAO> breakEventsIntoDailyPingResponses(List<EventDAO> eodEvents) {
    Map<Date, EventDAO> eventByDailyDate = Maps.newHashMap();
    for (EventDAO eodEvent : eodEvents) {
      Map<String, String> outputs = eodEvent.getWhat();
      for (Entry<String, String> whatKey : outputs.entrySet()) {
        String key = whatKey.getKey();
        String value = whatKey.getValue();
        
        int dateInputNameSeparatorIndex = key.indexOf("_");
        if (dateInputNameSeparatorIndex == -1 || key.substring(0,dateInputNameSeparatorIndex).equals("referred")) {
          continue;          
        }
        
        String dateStr = key.substring(0, dateInputNameSeparatorIndex);
        String inputName = key.substring(dateInputNameSeparatorIndex + 1);
        
        Date date = jodaFormatter.parseDateTime(dateStr).toDate();
        EventDAO newEvent = eventByDailyDate.get(date);
        if (newEvent == null) {
          Map<String, String> newWhat = Maps.newHashMap();
          newEvent = new EventDAO(eodEvent.getWho(), eodEvent.getWhen(), eodEvent.getExperimentName(), eodEvent.getLat(), eodEvent.getLon(),
                                  eodEvent.getAppId(), eodEvent.getPacoVersion(), newWhat, eodEvent.isShared(), eodEvent.getResponseTime(),
                                  eodEvent.getScheduledTime(), eodEvent.getBlobs(), eodEvent.getExperimentId(), eodEvent.getExperimentVersion(),
                                  eodEvent.getTimezone());
          eventByDailyDate.put(date, newEvent);
        }
        newEvent.getWhat().put(inputName, value);
      }
    }
    
    return eventByDailyDate;
  }
}
