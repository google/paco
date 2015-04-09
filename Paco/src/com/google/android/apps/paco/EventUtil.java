package com.google.android.apps.paco;

import org.joda.time.DateTime;

public class EventUtil {

  public static Event createEvent(Experiment experiment, String experimentGroup, Long actionTriggerId,
                                  Long actionId, Long actionTriggerSpecId, Long scheduledTime) {
    Event event = new Event();
    event.setExperimentId(experiment.getId());
    event.setServerExperimentId(experiment.getServerId());
    event.setExperimentName(experiment.getExperimentDAO().getTitle());
    if (scheduledTime != null && scheduledTime != 0L) {
      event.setScheduledTime(new DateTime(scheduledTime));
    }
    event.setExperimentVersion(experiment.getExperimentDAO().getVersion());
    event.setExperimentGroupName(experimentGroup);
    event.setActionId(actionId);
    event.setActionTriggerId(actionTriggerId);
    event.setActionTriggerSpecId(actionTriggerSpecId);

    event.setResponseTime(new DateTime());
    return event;
  }

  public static Event createSitesVisitedPacoEvent(String usedAppsString, Experiment experiment, long startTime) {
    Event event = new Event();
    event.setExperimentId(experiment.getId());
    event.setServerExperimentId(experiment.getServerId());
    event.setExperimentName(experiment.getExperimentDAO().getTitle());
    event.setExperimentVersion(experiment.getExperimentDAO().getVersion());
    event.setResponseTime(new DateTime());

    Output responseForInput = new Output();

    responseForInput.setAnswer(usedAppsString);
    responseForInput.setName("sites_visited");
    event.addResponse(responseForInput);

    Output responseForInputSessionDuration = new Output();
    long sessionDuration = (System.currentTimeMillis() - startTime) / 1000;
    responseForInputSessionDuration.setAnswer(Long.toString(sessionDuration));
    responseForInputSessionDuration.setName("session_duration");

    event.addResponse(responseForInputSessionDuration);
    return event;
  }

}
