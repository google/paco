package com.google.paco.shared.comm;

public class ExperimentSaveOutcome extends Outcome {

  private Long experimentId;


  public ExperimentSaveOutcome(long eventId, Long experimentId) {
    super(eventId);
    this.experimentId = experimentId;
  }

  public ExperimentSaveOutcome(long eventId, String errorMessage, Long experimentId) {
    super(eventId, errorMessage);
    this.experimentId = experimentId;
  }

  public ExperimentSaveOutcome(int eventId) {
    super(eventId);
  }

  public Long getExperimentId() {
    return experimentId;
  }

  public void setExperimentId(Long experimentId) {
    this.experimentId = experimentId;
  }


}
