package com.pacoapp.paco.shared.scheduling;

import org.joda.time.DateTime;

import com.pacoapp.paco.shared.model2.ActionTrigger;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.PacoNotificationAction;

public class ActionSpecification implements Comparable<ActionSpecification> {
  public DateTime time;
  public ExperimentDAO experiment;
  public ExperimentGroup experimentGroup;


  public ActionTrigger actionTrigger;

  //used to set snoozecount
  public PacoNotificationAction action;
  public Long actionTriggerSpecId;

  public ActionSpecification(DateTime nextTime, ExperimentDAO experiment,
                             ExperimentGroup experimentGroup,
                             ActionTrigger actionTrigger,
                             PacoNotificationAction action, Long actionTriggerSpecId) {
    this.time= nextTime;
    this.experiment = experiment;
    this.experimentGroup = experimentGroup;
    this.actionTrigger = actionTrigger;
    this.action = action;
    this.actionTriggerSpecId = actionTriggerSpecId;
  }

  public int compareTo(ActionSpecification arg0) {
    return time.compareTo(arg0.time);
  }
}