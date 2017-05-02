package com.google.sampling.experiential.server.migration;

public class MigrationOutput {
  private Long eventId;
  private String text;
  private String answer;
  public Long getEventId() {
    return eventId;
  }
  public void setEventId(Long eventId) {
    this.eventId = eventId;
  }
  public String getText() {
    return text;
  }
  public void setText(String text) {
    this.text = text;
  }
  public String getAnswer() {
    return answer;
  }
  public void setAnswer(String answer) {
    this.answer = answer;
  }
}
