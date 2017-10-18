package com.google.sampling.experiential.server.viz.appusage;

import java.util.List;

import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.shared.EventDAO;

public class PhoneSession extends BaseSession {

  private List<AppSession> childSessions = Lists.newArrayList();
  private AppSession currentChildSession;

  public PhoneSession(DateTime responseTime) {
    super(responseTime);
  }

  public PhoneSession handleEvent(EventDAO event, int minimumSessionBreak) {
    DateTime responseTime = new DateTime(event.getResponseTime());
    if (isSessionStartingEvent(event)) {
      if (getEndTime() == null) {
        return newSession(responseTime);
      } else {
        endSession(null);
      }
    } else if (isAppUsageEvent(event)) {
      addToPhoneSession(event);
    } else if (isSessionEndingEvent(event)) {
      endSession(responseTime);
      return null;
    }
    return this;

  }


  private PhoneSession newSession(DateTime responseTime) {
    endSession(responseTime);
    return new PhoneSession(responseTime);
  }

  private void addToPhoneSession(EventDAO event) {
    if (currentChildSession == null) {
      currentChildSession = new AppSession(getAppsUsedValue(event), new DateTime(event.getResponseTime()));
      childSessions.add(currentChildSession);
    }

    AppSession returnedAppSession = currentChildSession.handleEvent(event);
    if (!currentChildSession.equals(returnedAppSession)) {
      childSessions.add(returnedAppSession);
      currentChildSession = returnedAppSession;
    }
  }

  public void endSession(DateTime responseTime) {
    if (currentChildSession != null) {
      currentChildSession.endSession(responseTime);
    }
    super.endSession(responseTime);
  }

  private boolean isSessionStartingEvent(EventDAO event) {
    return isScreenOnEvent(event);
  }

  private boolean isScreenOnEvent(EventDAO event) {
    return event.getWhatByKey("userPresent") != null;
  }

  private boolean isPhoneOnEvent(EventDAO event) {
    String phoneOn = event.getWhatByKey("phoneOn");
    return phoneOn != null && Boolean.parseBoolean(phoneOn);
  }

  private boolean isSessionEndingEvent(EventDAO event) {
    return isScreenOffEvent(event) || isPhoneOffEvent(event) || isPhoneOnEvent(event) ;
  }

  private boolean isPhoneOffEvent(EventDAO event) {
    String phoneOn = event.getWhatByKey("phoneOn");
    return phoneOn != null && !Boolean.parseBoolean(phoneOn);
  }

  private boolean isScreenOffEvent(EventDAO event) {
    return event.getWhatByKey("userNotPresent") != null;
  }

  private boolean isAppUsageEvent(EventDAO event) {
    return getAppsUsedValue(event) != null;
  }

  private String getAppsUsedValue(EventDAO event) {
    return event.getWhatByKey("apps_used");
  }

  public List<AppSession> getAppSessions() {
    return childSessions;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("Phone session: \n");
    buf.append("  ");
    buf.append(super.toString());
    for (AppSession appSession : childSessions) {
      buf.append("\n  ");
      buf.append(appSession.toString());
    }

    return buf.toString();
  }

  protected DateTime addArtificialEndTime() {
    final AppSession lastChild = getLastChild();
    if (lastChild != null) {
    DateTime childEndTime = lastChild.ensureEndSession();
    setEndTime(childEndTime);
    return childEndTime;
    } else {
      return super.addArtificialEndTime();
    }
  }

  private AppSession getLastChild() {
    if (childSessions == null || childSessions.isEmpty()) {
      return null;
    }
    return childSessions.get(childSessions.size() - 1);
  }

}
