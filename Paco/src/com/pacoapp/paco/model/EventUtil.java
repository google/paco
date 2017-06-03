package com.pacoapp.paco.model;

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

}
