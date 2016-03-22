package com.google.sampling.experiential.server.stats.usage;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.shared.TimeUtil;

public class UsageStat implements java.io.Serializable {
  
  private DateTimeFormatter jodaFormatter = DateTimeFormat.forPattern(TimeUtil.DATETIME_FORMAT).withOffsetParsed();

  private DateTime date;
  private String adminDomainFilter;
  
  private int experimentCountTotal;
  private int unpublishedExperimentCountTotal;
  private int publishedExperimentCountTotal;
  private int publishedExperimentPublicCountTotal;
  private int publishedExperimentPrivateCountTotal;
  
  private int publishedExperimentFutureCountTotal;  // public & private
  private int publishedExperimentPastCountTotal;    // public & private
  private int publishedExperimentPresentCountTotal; // public & private
  private int publishedExperimentOngoingCountTotal; // public & private
  private List<Integer> publishedExperimentPrivateUserCountsTotal;  
  
  // these represent a subset of the above where we heuristically believe
  // they were not just pilots but real, externally launched experiments.
  // right now the heuristic is >= 5 participants
  // in the future we might try to perfect this heuristic with more info like # of events collected.
  private int publishedExperimentPublicCountNonPilotTotal; 
  private int publishedExperimentPrivateCountNonPilot; 
  private int publishedExperimentFutureCountNonPilot;       // public & private
  private int publishedExperimentPastCountNonPilot;         // public & private
  private int publishedExperimentPresentCountNonPilot;      // public & private
  private int publishedExperimentOngoingCountNonPilot;      // public & private
  private List<Integer> publishedExperimentPrivateUserCountsNonPilot;
  
  // non experiment stats
  private Long numberOfParticipants; // total number of participants in all experiments
  
  private long numberOfEvents; // total number of non-join, non-schedule-edit response sets across all experiments
  
  public UsageStat() {

  }
  
  public UsageStat(DateTime now) {
    this.date = now;
  }

  public UsageStat(UsageStat usageStat, UsageStat nonDomainOnSameDate) {
    this(usageStat.getDate());
    // skip adminDomainFilter
    this.experimentCountTotal = usageStat.experimentCountTotal + nonDomainOnSameDate.experimentCountTotal;
    this.unpublishedExperimentCountTotal = usageStat.unpublishedExperimentCountTotal + nonDomainOnSameDate.unpublishedExperimentCountTotal;
    this.publishedExperimentCountTotal = usageStat.publishedExperimentCountTotal + nonDomainOnSameDate.publishedExperimentCountTotal;
    this.publishedExperimentPublicCountTotal = usageStat.publishedExperimentPublicCountTotal + nonDomainOnSameDate.publishedExperimentPublicCountTotal;
    this.publishedExperimentPrivateCountTotal = usageStat.publishedExperimentPrivateCountTotal + nonDomainOnSameDate.publishedExperimentPrivateCountTotal;
    
    this.publishedExperimentFutureCountTotal = usageStat.publishedExperimentFutureCountTotal + nonDomainOnSameDate.publishedExperimentFutureCountTotal; 
    this.publishedExperimentPastCountTotal = usageStat.publishedExperimentPastCountTotal + nonDomainOnSameDate.publishedExperimentPastCountTotal;   
    this.publishedExperimentPresentCountTotal = usageStat.publishedExperimentPresentCountTotal + nonDomainOnSameDate.publishedExperimentPresentCountTotal;
    this.publishedExperimentOngoingCountTotal = usageStat.publishedExperimentOngoingCountTotal + nonDomainOnSameDate.publishedExperimentOngoingCountTotal;
    
    this.publishedExperimentPrivateUserCountsTotal = createCombinedList(usageStat.publishedExperimentPrivateUserCountsTotal,
                       nonDomainOnSameDate.publishedExperimentPrivateUserCountsTotal);
    
    this.publishedExperimentPublicCountNonPilotTotal = usageStat.publishedExperimentPublicCountNonPilotTotal + nonDomainOnSameDate.publishedExperimentPublicCountNonPilotTotal; 
    this.publishedExperimentPrivateCountNonPilot = usageStat.publishedExperimentPrivateCountNonPilot + nonDomainOnSameDate.publishedExperimentPrivateCountNonPilot; 
    this.publishedExperimentFutureCountNonPilot = usageStat.publishedExperimentFutureCountNonPilot + nonDomainOnSameDate.publishedExperimentFutureCountNonPilot;      
    this.publishedExperimentPastCountNonPilot = usageStat.publishedExperimentPastCountNonPilot + nonDomainOnSameDate.publishedExperimentPastCountNonPilot;        
    this.publishedExperimentPresentCountNonPilot = usageStat.publishedExperimentPresentCountNonPilot + nonDomainOnSameDate.publishedExperimentPresentCountNonPilot;     
    this.publishedExperimentOngoingCountNonPilot = usageStat.publishedExperimentOngoingCountNonPilot + nonDomainOnSameDate.publishedExperimentOngoingCountNonPilot;
    
    this.publishedExperimentPrivateUserCountsNonPilot = createCombinedList(usageStat.publishedExperimentPrivateUserCountsNonPilot,
                                                                           nonDomainOnSameDate.publishedExperimentPrivateUserCountsNonPilot);
    
    this.numberOfParticipants = usageStat.numberOfParticipants + nonDomainOnSameDate.numberOfParticipants; 
    this.numberOfEvents = usageStat.numberOfEvents + nonDomainOnSameDate.numberOfEvents; 

    
  }

  private List<Integer> createCombinedList(List<Integer> list1,
                                  List<Integer> list2) {
    List<Integer> combined = Lists.newArrayList();
    if (list1 != null) {
      combined.addAll(list1);
    }
    if (list2 != null) {
      combined.addAll(list2);
    }
    return combined;        
  }

  public void setTotalExperimentCount(int size) {
    this.experimentCountTotal = size;
  }

  public DateTime getDate() {
    return date;
  }

  public void setDate(DateTime now) {
    this.date = now;
  }

  public String getDateAsString() {
    return jodaFormatter.print(date);
  }

  public int getExperimentCountTotal() {
    return experimentCountTotal;
  }

  public void setExperimentCountTotal(int experimentCountTotal) {
    this.experimentCountTotal = experimentCountTotal;
  }

  public int getUnpublishedExperimentCountTotal() {
    return unpublishedExperimentCountTotal;
  }

  public void setUnpublishedExperimentCountTotal(int unpublishedExperimentCountTotal) {
    this.unpublishedExperimentCountTotal = unpublishedExperimentCountTotal;
  }

  public int getPublishedExperimentCountTotal() {
    return publishedExperimentCountTotal;
  }

  public void setPublishedExperimentCountTotal(int publishedExperimentCountTotal) {
    this.publishedExperimentCountTotal = publishedExperimentCountTotal;
  }

  public int getPublishedExperimentPublicCountTotal() {
    return publishedExperimentPublicCountTotal;
  }

  public void setPublishedExperimentPublicCountTotal(int publishedExperimentPublicCountTotal) {
    this.publishedExperimentPublicCountTotal = publishedExperimentPublicCountTotal;
  }

  public int getPublishedExperimentPrivateCountTotal() {
    return publishedExperimentPrivateCountTotal;
  }

  public void setPublishedExperimentPrivateCountTotal(int publishedExperimentPrivateCountTotal) {
    this.publishedExperimentPrivateCountTotal = publishedExperimentPrivateCountTotal;
  }

  public int getPublishedExperimentFutureCountTotal() {
    return publishedExperimentFutureCountTotal;
  }

  public void setPublishedExperimentFutureCountTotal(int publishedExperimentFutureCountTotal) {
    this.publishedExperimentFutureCountTotal = publishedExperimentFutureCountTotal;
  }

  public int getPublishedExperimentPastCountTotal() {
    return publishedExperimentPastCountTotal;
  }

  public void setPublishedExperimentPastCountTotal(int publishedExperimentPastCountTotal) {
    this.publishedExperimentPastCountTotal = publishedExperimentPastCountTotal;
  }

  public int getPublishedExperimentPresentCountTotal() {
    return publishedExperimentPresentCountTotal;
  }

  public void setPublishedExperimentPresentCountTotal(int publishedExperimentPresentCountTotal) {
    this.publishedExperimentPresentCountTotal = publishedExperimentPresentCountTotal;
  }

  public int getPublishedExperimentOngoingCountTotal() {
    return publishedExperimentOngoingCountTotal;
  }

  public void setPublishedExperimentOngoingCountTotal(int publishedExperimentOngoingCountTotal) {
    this.publishedExperimentOngoingCountTotal = publishedExperimentOngoingCountTotal;
  }

  public List<Integer> getPublishedExperimentPrivateUserCountsTotal() {
    return publishedExperimentPrivateUserCountsTotal;
  }

  public void setPublishedExperimentPrivateUserCountsTotal(List<Integer> publishedExperimentPrivateUserCountsTotal) {
    this.publishedExperimentPrivateUserCountsTotal = publishedExperimentPrivateUserCountsTotal;
  }

  public int getPublishedExperimentFutureCountNonPilot() {
    return publishedExperimentFutureCountNonPilot;
  }

  public void setPublishedExperimentFutureCountNonPilot(int publishedExperimentFutureCountNonPilot) {
    this.publishedExperimentFutureCountNonPilot = publishedExperimentFutureCountNonPilot;
  }

  public int getPublishedExperimentPastCountNonPilot() {
    return publishedExperimentPastCountNonPilot;
  }

  public void setPublishedExperimentPastCountNonPilot(int publishedExperimentPastCountNonPilot) {
    this.publishedExperimentPastCountNonPilot = publishedExperimentPastCountNonPilot;
  }

  public int getPublishedExperimentPresentCountNonPilot() {
    return publishedExperimentPresentCountNonPilot;
  }

  public void setPublishedExperimentPresentCountNonPilot(int publishedExperimentPresentCountNonPilot) {
    this.publishedExperimentPresentCountNonPilot = publishedExperimentPresentCountNonPilot;
  }

  public int getPublishedExperimentOngoingCountNonPilot() {
    return publishedExperimentOngoingCountNonPilot;
  }

  public void setPublishedExperimentOngoingCountNonPilot(int publishedExperimentOngoingCountNonPilot) {
    this.publishedExperimentOngoingCountNonPilot = publishedExperimentOngoingCountNonPilot;
  }

  public List<Integer> getPublishedExperimentPrivateUserCountsNonPilot() {
    return publishedExperimentPrivateUserCountsNonPilot;
  }

  public void setPublishedExperimentPrivateUserCountsNonPilot(List<Integer> publishedExperimentPrivateUserCountsNonPilot) {
    this.publishedExperimentPrivateUserCountsNonPilot = publishedExperimentPrivateUserCountsNonPilot;
  }

  public Long getNumberOfParticipants() {
    return numberOfParticipants;
  }

  public void setNumberOfParticipants(Long numberOfParticipants2) {
    this.numberOfParticipants = numberOfParticipants2;
  }

  public long getNumberOfEvents() {
    return numberOfEvents;
  }

  public void setNumberOfEvents(long numberOfEvents) {
    this.numberOfEvents = numberOfEvents;
  }

  public String getAdminDomainFilter() {
    return adminDomainFilter;
  }

  public void setAdminDomainFilter(String adminDomainFilter) {
    this.adminDomainFilter = adminDomainFilter;
  }

  public int getPublishedExperimentPublicCountNonPilotTotal() {
    return publishedExperimentPublicCountNonPilotTotal;
  }

  public void setPublishedExperimentPublicCountNonPilotTotal(int publishedExperimentPublicCountNonPilotTotal) {
    this.publishedExperimentPublicCountNonPilotTotal = publishedExperimentPublicCountNonPilotTotal;
  }

  public int getPublishedExperimentPrivateCountNonPilot() {
    return publishedExperimentPrivateCountNonPilot;
  }

  public void setPublishedExperimentPrivateCountNonPilot(int publishedExperimentPrivateCountNonPilot) {
    this.publishedExperimentPrivateCountNonPilot = publishedExperimentPrivateCountNonPilot;
  }


  

}
