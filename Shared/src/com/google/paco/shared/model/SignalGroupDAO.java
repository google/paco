package com.google.paco.shared.model;

import java.io.Serializable;
import java.util.Date;

public class SignalGroupDAO implements Serializable {

  public static final String DATE_FORMAT = "yyyy/MM/dd";

  private SignalingMechanismDAO[] signalingMechanisms;
  private InputDAO[] inputs;
  private FeedbackDAO[] feedback;

  private Boolean fixedDuration;

  private String startDate;

  private String endDate;

  public SignalGroupDAO() {
    this.inputs = new InputDAO[0];
    this.feedback = new FeedbackDAO[0];
    signalingMechanisms = new SignalingMechanismDAO[0];
  }

  public Boolean getFixedDuration() {
    return fixedDuration;
  }

  public void setFixedDuration(Boolean fixedDuration) {
    this.fixedDuration = fixedDuration;
  }

  public String getStartDate() {
    return startDate;
  }

  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String endDate) {
    if (endDate == null) { // For ongoing experiments.
      this.endDate = endDate;
    } else {
      setEndDateWithValidation(endDate);
    }
  }

  private void setEndDateWithValidation(String endDate) {
    Date startDateAsDate = getFormattedDate(startDate, DATE_FORMAT);
    Date endDateAsDate = getFormattedDate(endDate, DATE_FORMAT);
    if (startDateAsDate == null || endDateAsDate.before(startDateAsDate)) {
      throw new IllegalArgumentException("End date cannot be before start date.");
    }
    this.endDate = endDate;
  }

  private Date getFormattedDate(String inputDateStr, String dateFormat) {
    if (inputDateStr == null) {
      return null;
    }
    DateTimeFormat formatter = DateTimeFormat.getFormat(DATE_FORMAT);
    return formatter.parse(inputDateStr);
  }

  public SignalingMechanismDAO[] getSignalingMechanisms() {
    return signalingMechanisms;
  }

  public void setSignalingMechanisms(SignalingMechanismDAO[] signalingMechanisms) {
    this.signalingMechanisms = signalingMechanisms;
  }

  public InputDAO[] getInputs() {
    return inputs;
  }

  public void setInputs(InputDAO[] inputs) {
    this.inputs = inputs;
  }

  public FeedbackDAO[] getFeedback() {
    return feedback;
  }

  public void setFeedback(FeedbackDAO[] feedback) {
    this.feedback = feedback;
  }

  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer();
    for (SignalingMechanismDAO signalMech : signalingMechanisms) {
      buf.append(signalMech.toString());
    }
    return buf.toString();
  }





}
