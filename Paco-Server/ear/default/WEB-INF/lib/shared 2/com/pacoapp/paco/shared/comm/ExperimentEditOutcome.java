package com.pacoapp.paco.shared.comm;


public class ExperimentEditOutcome extends Outcome {

  private Long experimentId;


  public ExperimentEditOutcome(long eventId, Long experimentId) {
    super(eventId);
    this.experimentId = experimentId;
  }

  public ExperimentEditOutcome(long eventId, String errorMessage, Long experimentId) {
    super(eventId, errorMessage);
    this.experimentId = experimentId;
  }

  public ExperimentEditOutcome(int eventId) {
    super(eventId);
  }

  public Long getExperimentId() {
    return experimentId;
  }

  public void setExperimentId(Long experimentId) {
    this.experimentId = experimentId;
  }


}
