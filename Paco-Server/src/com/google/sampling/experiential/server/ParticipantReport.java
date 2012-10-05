package com.google.sampling.experiential.server;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.model.Event;

public class ParticipantReport {

  private String who;
  private List<Event> events;
  private boolean alreadyComputed;
  
  private int scheduled;
  private int selfReportResponses;
  
  private int todaysSelfReportResponses;
  private int todaysScheduled;
  private int todaysSignaledResponses;
  private int signaledResponses;

  public ParticipantReport(String who) {
    this.who = who;
    this.events = Lists.newArrayList();    
  }

  public void computeStats() {
    if (alreadyComputed) {
      throw new IllegalArgumentException("Already computed stats");
    }
    alreadyComputed = true;
    compute();
  }
  
  public void addEvent(Event event) {
    if (alreadyComputed) {
      throw new IllegalArgumentException("Already computed stats");
    }
    this.events.add(event);    
  }

  public String getWho() {
    return who;
  }

  public void setWho(String who) {
    this.who = who;
  }

  public List<Event> getEvents() {
    return events;
  }

  public void setEvents(List<Event> events) {
    this.events = events;
  }

  public int getSelfReportResponseCount() {
    ensureComputed();
    return selfReportResponses;
  }

  public int getScheduledCount() {
    ensureComputed();
    return scheduled;
  }

  public float getSignaledResponseRate() {
    ensureComputed();
    return scheduled > 0 ? ((float)signaledResponses / (float)scheduled) : 0f;
  }


  public int getTodaysSignaledResponseCount() {
    ensureComputed();
    return todaysSignaledResponses;
  }

  public int getTodaysScheduledCount() {
    ensureComputed();
    return todaysScheduled;
  }

  public float getTodaysSignaledResponseRate() {
    ensureComputed();
    return todaysScheduled > 0 ? ((float)todaysSignaledResponses / (float)todaysScheduled): 0;
  }

  public int getTodaysSelfReportResponseCount() {
    ensureComputed();
    return todaysSelfReportResponses;
  }



  private void compute() {
    scheduled = 0;
    selfReportResponses = 0;
    signaledResponses = 0;
    for (Event event : events) {
      if (event.isJoined()) {
        continue;
      }
      Date scheduledTime = event.getScheduledTime();
      Date responseTime = event.getResponseTime();

      if (scheduledTime != null) {
        scheduled++;
        if (responseTime != null) {
          signaledResponses++;
        }
      } else if (responseTime != null) {
        selfReportResponses++;
      }          

    }
    computeTodaysResponses();
  }
  
  private void computeTodaysResponses() {
    todaysSelfReportResponses = 0;
    todaysScheduled = 0;
    todaysSignaledResponses = 0;
    for (Event event : events) {
      if (event.isJoined()) {
        continue;
      }
      Date scheduledTime = event.getScheduledTime();
      Date responseTime = event.getResponseTime();
      if ((scheduledTime != null && isToday(scheduledTime))) {
        todaysScheduled++;
        if (responseTime != null) {
          todaysSignaledResponses++;
        }
      } else if (responseTime != null && isToday(responseTime)) {
        todaysSelfReportResponses++;
      }          
    }
  }

  private boolean isToday(Date scheduledTime) {
    if (scheduledTime == null) {
      return false;
    }
    Date date = Calendar.getInstance().getTime();
    return date.getDate() == scheduledTime.getDate() &&
           date.getMonth() == scheduledTime.getMonth() &&
           date.getYear() == scheduledTime.getYear();

  }
  
  private void ensureComputed() {
    if (!alreadyComputed) {
      throw new IllegalArgumentException("Have not computed stats yet");
    }
  }

  public int getSelfReportAndSignaledResponseCount() {
    return signaledResponses + selfReportResponses;
  }

  public int getTodaysMissedCount() {
    return todaysScheduled - todaysSignaledResponses;
  }

  public int getSignaledResponseCount() {
    return signaledResponses;
  }

  public int getMissedCount() {
    return scheduled - signaledResponses;
  }

  
}
