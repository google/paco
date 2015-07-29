package com.pacoapp.paco.shared.model2;

import java.util.ArrayList;
import java.util.List;

public class ScheduleTrigger extends ActionTrigger implements Validatable, java.io.Serializable  {

  private List<Schedule> schedules;

  public ScheduleTrigger() {
    super();
    this.type = SCHEDULE_TRIGGER_TYPE_SPECIFIER;
    this.schedules = new ArrayList();
  }

  public ScheduleTrigger(List<Schedule> schedules) {
    super();
    this.type = SCHEDULE_TRIGGER_TYPE_SPECIFIER;
    if (schedules != null) {
      this.schedules = schedules;
    } else {
      this.schedules = new ArrayList();
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
//    System.out.println("VALIDATING SCHEDULETRIGGER");
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
