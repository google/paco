package com.google.sampling.experiential.server.viz.appusage;

import java.util.List;

import org.joda.time.DateTime;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.shared.EventDAO;

public class AppSession extends BaseSession {

  private String appName;
  AppScreenSession currentChildSession = null;
  private List<AppScreenSession> childSessions = Lists.newArrayList();

  public AppSession(String appName, DateTime responseTime) {
    super(responseTime);
    this.appName = appName;
  }

  public AppSession handleEvent(EventDAO event) {
    String eventAppName = event.getWhatByKey("apps_used");
    String eventAppScreenName = event.getWhatByKey("apps_used_raw");
    final DateTime responseTime = new DateTime(event.getResponseTime());

    if (isNewAppSession() || isPartOfCurrentAppSession(eventAppName)) {
      newAppScreenSession(eventAppScreenName, responseTime);
    } else if (!isPartOfCurrentAppSession(eventAppName)) {
      endSession(responseTime);
      return new AppSession(eventAppName, responseTime).handleEvent(event);
    }
    return this;
  }

  private boolean isPartOfCurrentAppSession(String eventAppName) {
    return appName.equals(eventAppName);
  }

  private boolean isNewAppSession() {
    return currentChildSession == null;
  }

  private void newAppScreenSession(String eventAppScreenName, DateTime responseTime) {
    endAppScreenSession(responseTime);
    currentChildSession = new AppScreenSession(eventAppScreenName, responseTime);
    childSessions.add(currentChildSession);
  }

  private void endAppScreenSession(DateTime responseTime) {
    if (currentChildSession != null) {
      currentChildSession.endSession(responseTime);
    }
  }

  public void endSession(DateTime responseTime) {
    endAppScreenSession(responseTime);
    super.endSession(responseTime);
  }

  public String getAppName() {
    return appName;
  }

  @Override
  public String toString() {
    return "App Name: " + appName + "\n    " + super.toString() + "\n    App Screens: \n" + appScreensToString();
  }

  private String appScreensToString() {
    return Joiner.on("\n").join(childSessions);
  }

  public List<AppScreenSession> getAppScreenSessions() {
    return childSessions;
  }

  protected DateTime addArtificialEndTime() {
    DateTime childEndTime = getLastChild().ensureEndSession();
    setEndTime(childEndTime);
    return childEndTime;
  }

  private AppScreenSession getLastChild() {
    if (childSessions == null || childSessions.isEmpty()) {
      return null;
    }
    return childSessions.get(childSessions.size() - 1);
  }


}
