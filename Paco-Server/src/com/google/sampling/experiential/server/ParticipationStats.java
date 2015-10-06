package com.google.sampling.experiential.server;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.google.common.collect.Lists;

public class ParticipationStats implements java.io.Serializable {

    public static class ParticipantParticipationStat implements java.io.Serializable, Comparable<ParticipantParticipationStat>  {
      private String who;

      int todaySignalCount;
      int todaySignalResponseCount;
      int todaySelfReportCount;

      int totalSignalCount;
      int totalSignalResponseCount;
      int totalSelfReportCount;

      public ParticipantParticipationStat(String who,
                                          int todaySignalCount,
                                          int todaySignalResponseCount,
                                          int todaySelfReportCount,
                                          int totalSignalCount,
                                          int totalSignalResponseCount,
                                          int totalSelfReportCount) {
        super();
        this.who = who;
        this.todaySignalCount = todaySignalCount;
        this.todaySignalResponseCount = todaySignalResponseCount;
        this.todaySelfReportCount = todaySelfReportCount;
        this.totalSignalCount = totalSignalCount;
        this.totalSignalResponseCount = totalSignalResponseCount;
        this.totalSelfReportCount = totalSelfReportCount;
      }

      public String getWho() {
        return who;
      }

      public void setWho(String who) {
        this.who = who;
      }

      public int getTodaySignalCount() {
        return todaySignalCount;
      }

      public void setTodaySignalCount(int todaySignalCount) {
        this.todaySignalCount = todaySignalCount;
      }

      public int getTodaySignalResponseCount() {
        return todaySignalResponseCount;
      }

      public void setTodaySignalResponseCount(int todaySignalResponseCount) {
        this.todaySignalResponseCount = todaySignalResponseCount;
      }

      public int getTodaySelfReportCount() {
        return todaySelfReportCount;
      }

      public void setTodaySelfReportCount(int todaySelfReportCount) {
        this.todaySelfReportCount = todaySelfReportCount;
      }

      public int getTotalSignalCount() {
        return totalSignalCount;
      }

      public void setTotalSignalCount(int totalSignalCount) {
        this.totalSignalCount = totalSignalCount;
      }

      public int getTotalSignalResponseCount() {
        return totalSignalResponseCount;
      }

      public void setTotalSignalResponseCount(int totalSignalResponseCount) {
        this.totalSignalResponseCount = totalSignalResponseCount;
      }

      public int getTotalSelfReportCount() {
        return totalSelfReportCount;
      }

      public void setTotalSelfReportCount(int totalSelfReportCount) {
        this.totalSelfReportCount = totalSelfReportCount;
      }

      @Override
      @JsonIgnore
      public int compareTo(ParticipantParticipationStat o) {
        if (o.getTodaySignalResponseCount() > o.getTodaySignalResponseCount()) {
          return 1;
        } else if (o.getTodaySignalResponseCount() < o.getTodaySignalResponseCount()) {
          return -1;
        } else {
          return 0;
        }
      }
    }


    List<ParticipantParticipationStat> participants = Lists.newArrayList();
    String cursor;

    public ParticipationStats(List<ParticipantParticipationStat> participants, String cursor) {
      super();
      if (participants != null) {
        this.participants = participants;
      }
      if (cursor != null) {
        this.cursor = cursor;
      }
    }

    public int getCount() {
      return participants.size();
    }

    public List<ParticipantParticipationStat> getParticipants() {
      return participants;
    }

    public void setParticipants(List<ParticipantParticipationStat> participants) {
      this.participants = participants;
    }

    public String getCursor() {
      return cursor;
    }

    public void setCursor(String cursor) {
      this.cursor = cursor;
    }



}
