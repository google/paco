package com.google.sampling.experiential.server.stats.participation;

import org.joda.time.DateTime;


public class ResponseStat implements Comparable<ResponseStat> {
  
  public long experimentId;
  public String experimentGroupName;
  public String who;
  public int schedR;
  public int missedR;
  public int selfR;
  
  // jackson 2 @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy/MM/dd", timezone="UTC")
  public DateTime date;
  public DateTime lastContactDateTime;
  
  
  public ResponseStat(long experimentId, String experimentGroupName, String who, int schedR, int missedR, int selfR) {
    super();
    this.experimentId = experimentId;
    this.experimentGroupName = experimentGroupName;
    this.who = who;
    this.schedR = schedR;
    this.missedR = missedR;
    this.selfR = selfR;
  }
  
  public ResponseStat(long experimentId, String experimentGroupName, String who, DateTime dateOfInterest, int schedR, int missedR, int selfR) {
    this(experimentId, experimentGroupName, who, schedR, missedR, selfR);
    this.date = dateOfInterest;  
  }

  public ResponseStat(Long experimentId, String experimentGroupName, String who, DateTime dateOfInterest, int schedR,
                      int missedR, int selfR, DateTime lastContactDateTime) {
    this(experimentId, experimentGroupName, who, dateOfInterest, schedR, missedR, selfR);
    this.lastContactDateTime = lastContactDateTime;
  }

  /**
   * We want to order Response Stats by who and then by date, if those fields
   * are available. Otherwise we do not care.
   */
  @Override
  public int compareTo(ResponseStat o) {
    if (o == null) {
      return 1;
    } 
    if (who != null && o.who != null) {      
      int whoComparison = who.compareTo(o.who);
      if (whoComparison != 0) {
        return whoComparison;
      }
    }
    if (date != null && o.date != null) {
      return date.compareTo(o.date);
    }
    return 0;
  }

  public String getDate() {
    if (date != null) {
      return date.toString(com.pacoapp.paco.shared.util.TimeUtil.dateFormatter);
    } else {
      return null;
    }
  }

  public DateTime getLastContactDateTime() {
    return lastContactDateTime;
  }

  public void setLastContactDateTime(DateTime lastContactDateTime) {
    this.lastContactDateTime = lastContactDateTime;
  }

  
}
