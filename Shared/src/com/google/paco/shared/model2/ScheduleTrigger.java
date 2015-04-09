package com.google.paco.shared.model2;

import java.util.List;

import com.google.common.collect.Lists;

public class ScheduleTrigger extends ActionTrigger implements Validatable, java.io.Serializable  {

  private List<Schedule> schedules;

  public ScheduleTrigger() {
    super();
    this.type = "scheduleTrigger";
    this.schedules = Lists.newArrayList();
  }

  public ScheduleTrigger(List<Schedule> schedules) {
    super();
    this.type = "scheduleTrigger";
    if (schedules != null) {
      this.schedules = schedules;
    } else {
      this.schedules = Lists.newArrayList();
    }
  }

  public List<Schedule> getSchedules() {
    return schedules;
  }

  public void setSchedules(List<Schedule> schedules) {
    this.schedules = schedules;
  }

  public void validateWith(Validator validator) {
    super.validateWith(validator);
    validator.isNotNullAndNonEmptyCollection(schedules, "ScheduleTrigger needs at least one schedule");
    for (Schedule schedule : schedules) {
      schedule.validateWith(validator);
    }
  }

  public Schedule getSchedulesById(Long scheduleId) {
    for (Schedule schedule : schedules) {
      if (schedule.getId().equals(scheduleId)) {
        return schedule;
      }
    }
    return null;
  }
}
