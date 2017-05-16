package com.pacoapp.paco.shared.model2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class InterruptTrigger extends ActionTrigger implements Validatable, MinimumBufferable, Serializable {

  private List<InterruptCue> cues;
  protected Integer minimumBuffer = 0;
  private boolean timeWindow = false;
  private int startTimeMillis = 0;
  private int endTimeMillis = 0;
  private boolean weekends = true;


  public InterruptTrigger() {
    super();
    this.type = INTERRUPT_TRIGGER_TYPE_SPECIFIER;
    cues = new ArrayList();
  }

  public List<InterruptCue> getCues() {
    return cues;
  }

  public void setCues(List<InterruptCue> cues) {
    this.cues = cues;
  }

  public Integer getMinimumBuffer() {
    return minimumBuffer;
  }

  public void setMinimumBuffer(Integer minimumBuffer) {
    this.minimumBuffer = minimumBuffer;
  }

  public Integer getDefaultMinimumBuffer() {
    // TODO minimum buffer cannot be shorter than the longest timeout for any notification actions
    // that this actiontrigger contains.
    // we need to compute this by iterating all the actions we contain and returning the
    int longestMinimum = 0;
    for (PacoAction action : actions) {
      if (action instanceof PacoNotificationAction) {
        longestMinimum = Math.max(longestMinimum, ((PacoNotificationAction)action).getTimeout());
      }
    }
    if (longestMinimum == 0) {
      return MinimumBufferable.DEFAULT_MIN_BUFFER;
    } else {
      return longestMinimum;
    }
  }

  public void validateWith(Validator validator) {
    super.validateWith(validator);
//    System.out.println("VALIDATING INTERRUPT");
    validator.isNotNull(minimumBuffer, "minimumBuffer is not properly initialized");
    validator.isNotNullAndNonEmptyCollection(cues, "InterruptTrigger needs at least one cue");

    for (InterruptCue cue: cues) {
      cue.validateWith(validator);
    }
  }

  public boolean getTimeWindow() {
    return timeWindow;
  }

  public void setTimeWindow(boolean hasWindow) {
    this.timeWindow = hasWindow;
  }

  public int getStartTimeMillis() {
    return startTimeMillis;
  }

  public int getEndTimeMillis() {
    return endTimeMillis ;
  }

  public void setStartTimeMillis(int startTimeMillis) {
    this.startTimeMillis = startTimeMillis;
  }

  public void setEndTimeMillis(int endTimeMillis) {
    this.endTimeMillis = endTimeMillis;
  }

  public boolean getWeekends() {
    return weekends;
  }

  public void setWeekends(boolean weekends) {
    this.weekends = weekends;
  }



}
