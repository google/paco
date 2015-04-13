package com.pacoapp.paco.ui;

import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.Schedule;
import com.pacoapp.paco.shared.model2.ScheduleTrigger;

class ScheduleBundle {
  ExperimentGroup group;
  ScheduleTrigger trigger;
  Schedule schedule;

  public ScheduleBundle(ExperimentGroup group, ScheduleTrigger trigger, Schedule schedule) {
    super();
    this.group = group;
    this.trigger = trigger;
    this.schedule = schedule;
  }

}