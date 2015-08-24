package com.google.sampling.experiential.server;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.sampling.experiential.shared.TimeUtil;

public class UsageStat implements java.io.Serializable {
  private DateTimeFormatter jodaFormatter = DateTimeFormat.forPattern(TimeUtil.DATETIME_FORMAT).withOffsetParsed();

  private DateTime now;
  private int experimentTotalCount;

  private int domainExperimentCount;

  private int domainFutureExperimentCount;

  private int domainPastExperimentCount;

  private int domainPresentExperimentCount;

  private int domainOngoingExperimentCount;

  private List<Integer> domainExperimentModifiedAges;

  private int domainPublishedExperimentCount;

  private List<Integer> domainPublishedUserCounts;

  public UsageStat(DateTime now) {
    this.now = now;
  }

  public void setTotalExperimentCount(int size) {
    this.experimentTotalCount = size;
  }

  public DateTime getNow() {
    return now;
  }

  public void setNow(DateTime now) {
    this.now = now;
  }

  public String getNowAsString() {
    return jodaFormatter.print(now);
  }

  public int getExperimentTotalCount() {
    return experimentTotalCount;
  }

  public void setExperimentTotalCount(int experimentTotalCount) {
    this.experimentTotalCount = experimentTotalCount;
  }

  public int getDomainExperimentCount() {
    return domainExperimentCount;
  }

  public void setDomainExperimentCount(int domainExperimentCount) {
    this.domainExperimentCount = domainExperimentCount;
  }

  public int getDomainFutureExperimentCount() {
    return domainFutureExperimentCount;
  }

  public void setDomainFutureExperimentCount(int domainFutureExperimentCount) {
    this.domainFutureExperimentCount = domainFutureExperimentCount;
  }

  public int getDomainPastExperimentCount() {
    return domainPastExperimentCount;
  }

  public void setDomainPastExperimentCount(int domainPastExperimentCount) {
    this.domainPastExperimentCount = domainPastExperimentCount;
  }

  public int getDomainPresentExperimentCount() {
    return domainPresentExperimentCount;
  }

  public void setDomainPresentExperimentCount(int domainPresentExperimentCount) {
    this.domainPresentExperimentCount = domainPresentExperimentCount;
  }

  public int getDomainOngoingExperimentCount() {
    return domainOngoingExperimentCount;
  }

  public void setDomainOngoingExperimentCount(int domainOngoingExperimentCount) {
    this.domainOngoingExperimentCount = domainOngoingExperimentCount;
  }

  public List<Integer> getDomainExperimentModifiedAges() {
    return domainExperimentModifiedAges;
  }

  public void setDomainExperimentModifiedAges(List<Integer> domainExperimentModifiedAges) {
    this.domainExperimentModifiedAges = domainExperimentModifiedAges;
  }

  public int getDomainPublishedExperimentCount() {
    return domainPublishedExperimentCount;
  }

  public void setDomainPublishedExperimentCount(int domainPublishedExperimentCount) {
    this.domainPublishedExperimentCount = domainPublishedExperimentCount;
  }

  public List<Integer> getDomainPublishedUserCounts() {
    return domainPublishedUserCounts;
  }

  public void setDomainPublishedUserCounts(List<Integer> domainPublishedUserCounts) {
    this.domainPublishedUserCounts = domainPublishedUserCounts;
  }


}
