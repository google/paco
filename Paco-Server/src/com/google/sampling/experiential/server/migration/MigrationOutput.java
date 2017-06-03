package com.google.sampling.experiential.server.migration;

public class MigrationOutput {
  private Long eventId;
  private String text;
  private String answer;
  public MigrationOutput(){
    
  }
  public MigrationOutput(Long eid, String text, String ans) { 
    this.eventId = eid;
    this.text = text;
    this.answer = ans;
  }
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
  @Override
  public String toString() {
    return "MigrationOutput [eventId=" + eventId + ", text=" + text + ", answer=" + answer + "]";
  }
}
