/*
* Copyright 2011 Google Inc. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance  with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package com.google.android.apps.paco;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.paco.shared.model.FeedbackDAO;

public class Experiment implements Parcelable {



  public static class Creator implements Parcelable.Creator<Experiment> {

    public Experiment createFromParcel(Parcel source) {
      ClassLoader classLoader = getClass().getClassLoader();
      Experiment experiment = new Experiment();
      experiment.id = source.readLong();
      experiment.serverId = source.readLong();
      experiment.title = source.readString();
      experiment.version = source.readInt();
      experiment.description = source.readString();

      // TODO (bobevans):set icon from parcelable bytes
      byte[] iconBytes = new byte[source.readInt()];
      source.readByteArray(iconBytes);
      experiment.icon = iconBytes;

      experiment.creator = source.readString();
      experiment.hash = source.readString();
      experiment.informedConsentForm = source.readString();
      experiment.questionsChange = source.readInt() == 1;

      experiment.joinDate = source.readString();

      experiment.schedule = source.readParcelable(classLoader);
      experiment.fixedDuration = source.readInt() == 1;

      if (experiment.fixedDuration) {
        experiment.startDate = source.readString();
        experiment.endDate = source.readString();
      }

      int numberOfInputs = source.readInt();
      for (int i=0;i<numberOfInputs;i++) {
        Input input = source.readParcelable(classLoader);
        experiment.inputs.add(input);
      }

      int numberOfFeedbacks = source.readInt();
      for (int i=0;i<numberOfFeedbacks;i++) {
        Feedback feedback = source.readParcelable(classLoader);
        experiment.feedback.add(feedback);
      }

      experiment.webRecommended = source.readInt() == 1;

      experiment.json = source.readString();

      Trigger trigger = source.readParcelable(classLoader);
      experiment.trigger = trigger;

      experiment.customRendering = source.readInt() == 1;
      experiment.customRenderingCode = source.readString();

      experiment.feedbackType = source.readInt();
      return experiment;
    }

    public Experiment[] newArray(int size) {
      return new Experiment[size];
    }
  }

  public static final Creator CREATOR = new Creator();
  @JsonIgnore
  private Long id;

  @JsonProperty("id")
  private Long serverId;
  private String title = "";
  private String description = "";
  private String creator = "";
  private String informedConsentForm = "";
  private String hash = "";
  private  byte[] icon;
  private Boolean questionsChange = false;

  @JsonIgnore
  private SignalSchedule schedule;
  private Boolean fixedDuration;
  private String startDate;
  private String endDate;
  public Boolean webRecommended;

  private Trigger trigger;
  private List<SignalingMechanism> signalingMechanisms;
  private Boolean customRendering = false;
  private String customRenderingCode;



  private List<Input> inputs = new ArrayList<Input>();
  private List<Event> events = new ArrayList<Event>();

  private String joinDate;
  private String modifyDate;
  private Integer version;

  @JsonIgnore
  private String json;

  private List<Feedback> feedback = new ArrayList<Feedback>();
  public Integer feedbackType = FeedbackDAO.FEEDBACK_TYPE_RETROSPECTIVE; // The traditional qs-retrospective style feedback.

  public static final String SCHEDULED_TIME = "scheduledTime";

  public static final String URI_AS_EXTRA = "uriAsExtra";
  public static final String TRIGGERED_TIME = "triggeredTime";
  public static final String TRIGGER_EVENT = "trigger_event";
  public static final String TRIGGER_SOURCE_IDENTIFIER = "sourceIdentifier";


  public String getModifyDate() {
    return modifyDate;
  }

  public void setModifyDate(String modifyDate) {
    this.modifyDate = modifyDate;
  }

  public void setFeedback(List<Feedback> feedback) {
    this.feedback = feedback;
  }

  public Experiment(String title, String description, String creator,
	  SignalSchedule schedule, Integer time, Integer frequency,
	  Boolean fixedSchedule,
	  String startDate, String endDate, String informedConsentForm, String hash) {
	this.title = title;
	this.description = description;
	this.creator = creator;
	this.schedule = schedule;
	this.fixedDuration = fixedSchedule;
	this.startDate = startDate;
	this.endDate = endDate;

	this.informedConsentForm = informedConsentForm;
	this.hash = hash;
  }

  public Experiment() {
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public String getInformedConsentForm() {
    return informedConsentForm;
  }

  public void setInformedConsentForm(String informedConsentForm) {
    this.informedConsentForm = informedConsentForm;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  public String getInformedConsent() {
	return informedConsentForm;
  }

  @JsonProperty("id")
  public Long getServerId() {
    return serverId;
  }

  @JsonProperty("id")
  public void setServerId(Long serverId) {
    this.serverId = serverId;
  }

  public byte[] getIcon() {
    return icon;
  }

  public void setIcon(byte[] icon) {
    this.icon = icon;
  }

  public boolean isQuestionsChange() {
    return questionsChange;
  }

  @JsonIgnore
  public boolean isOver(DateTime now) {
    return isFixedDuration() != null && isFixedDuration() && now.isAfter(getEndDateTime());
  }


  public void setQuestionsChange(boolean questionsChange) {
    this.questionsChange = questionsChange;
  }

  public List<Input> getInputs() {
    return inputs;
  }

  public void setInputs(List<Input> inputs) {
    this.inputs = inputs;
  }

  public List<Feedback> getFeedback() {
    return feedback;
  }

  public void setFeeback(List<Feedback> feedback) {
    this.feedback = feedback;
  }

  public String getJoinDate() {
    return joinDate;
  }

  public void setJoinDate(String joinDate) {
    this.joinDate = joinDate;
  }

  public Boolean isFixedDuration() {
    return fixedDuration;
  }

  public void setFixedDuration(Boolean fixedSchedule) {
    this.fixedDuration = fixedSchedule;
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
    this.endDate = endDate;
  }

  @JsonIgnore
  public void setId(Long long1) {
    this.id = long1;
  }

  @JsonIgnore
  public Long getId() {
    return id;
  }

  public int describeContents() {
    return 0;
  }

  public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(id);
    dest.writeLong(serverId);
    dest.writeString(title);
    dest.writeInt(version);
    dest.writeString(description);
    dest.writeInt(icon.length);
    dest.writeByteArray(icon);
    dest.writeString(creator);
    dest.writeString(hash);
    dest.writeString(informedConsentForm);
    dest.writeInt(questionsChange ? 1 : 0);

    // TODO (bobevans):set icon from parcelable bytes
    // byte[] iconBytes = icon.getBytes();
    // dest.writeInt(iconBytes.length);
    // dest.writeByteArray(iconBytes);

    dest.writeString(joinDate);

    dest.writeParcelable(schedule, 0);
    dest.writeInt(fixedDuration ? 1: 0);

    if (fixedDuration) {
      dest.writeString(startDate);
      dest.writeString(endDate);
    }

    dest.writeInt(inputs.size());
    for (Input input : inputs) {
      dest.writeParcelable(input, 0);
    }

    dest.writeInt(feedback.size());
    for (Feedback feedbackItem : feedback) {
      dest.writeParcelable(feedbackItem, 0);
    }

    dest.writeInt(webRecommended ? 1 : 0);
    dest.writeString(json);

    dest.writeParcelable(trigger, 0);
    dest.writeInt(customRendering ? 1 : 0);
    dest.writeString(customRenderingCode);
    dest.writeInt(feedbackType);
  }

  @JsonIgnore
  public SignalSchedule getSchedule() {
    return schedule;
  }

  @JsonIgnore
  public void setSchedule(SignalSchedule schedule) {
    this.schedule = schedule;
  }



  public Trigger getTrigger() {
    return trigger;
  }

  public void setTrigger(Trigger trigger) {
    this.trigger = trigger;
  }

  public void unsetId() {
    this.id = null;
  }

  // we may need to discriminate more on inputs. For QOTD, there may be old
  // inputs, unless we delete them, so the experiment could know how to
  // return only appropriate inputs (for today in the case of QOTD).
  // this assumes an experiment knows it's type is qotd which requires more fields
  // than just canQuestionsChange.
  // TODO (bobevans) Figure out right answer, at least for qotd
  // e.g. if (IAmQOTD) {
  //         inputs = getInputsForToday() {
  //                    for each input
  //                      if (input.getField("date") == today)
  //                        inputs.add(input);
  //                  }
  //         return inputs.size() > 0
  //      }
  public boolean hasFreshInputs() {
    if (!isQuestionsChange()) {
      return true;
    }
    DateMidnight dateMidnight = new DateMidnight();

    for (Input input : getInputs()) {
      if (dateMidnight.isEqual(new DateMidnight(input.getScheduleDate().getTime()))) {
        return true;
      }
    }
    return false;
  }

  public void setEvents(List<Event> list) {
    this.events = list;
  }

  public List<Event> getEvents() {
    return events;
  }

  @JsonIgnore
  public DateTime getNextTime(DateTime now, Context context) {
    if (now == null || getTrigger() != null || isExperimentOver(now)) {
      return null;
    }
    if (isExperimentNotStartedYet(now)) {
      now = TimeUtil.unformatDate(getStartDate()).toDateMidnight().toDateTime();
    }
    if (getSchedule().getScheduleType().equals(SignalSchedule.ESM)) {
      return scheduleESM(now, context);
    } else {
      return schedule.getNextAlarmTime(now);
    }
  }

  private boolean isExperimentNotStartedYet(DateTime now) {
    return isFixedDuration()
            && now.isBefore(getStartDateAsDateMidnight());
  }

  private DateMidnight getStartDateAsDateMidnight() {
    return TimeUtil.unformatDate(getStartDate()).toDateMidnight();
  }

  //@VisibleForTesting
  DateTime scheduleESM(DateTime now, Context context) {
    if (schedule.convertEsmPeriodToDays() == 1 && !schedule.getEsmWeekends() && TimeUtil.isWeekend(now)) {
      now = TimeUtil.skipWeekends(now);
    }
    ensureScheduleIsGeneratedForPeriod(now, context);
    // generate at least the next period, so we always have a next time for ESMs.
    DateTime nextPeriod = now.plusDays(getSchedule().convertEsmPeriodToDays());
    if (schedule.convertEsmPeriodToDays() == 1 && !schedule.getEsmWeekends() && TimeUtil.isWeekend(nextPeriod)) {
      nextPeriod = TimeUtil.skipWeekends(nextPeriod);
    }

    ensureScheduleIsGeneratedForPeriod(nextPeriod, context);
    DateTime next = lookupNextTimeOnEsmSchedule(now, context); // anymore this period
    if (next != null) {
      return next;
    }
    return lookupNextTimeOnEsmSchedule(nextPeriod, context);
  }

  @JsonIgnore
  public DateTime getEndDateTime() {
    DateTime lastTime = null;
    for (SignalingMechanism signalingMechanism : getSignalingMechanisms()) {
      DateTime lastTimeForSignalGroup = null;
      if (signalingMechanism instanceof SignalSchedule) {
        if (((SignalSchedule) signalingMechanism).getScheduleType().equals(SignalSchedule.WEEKDAY)) {
          List<Long> times = schedule.getTimes();
          Collections.sort(times);
          DateTime lastTimeForDay = new DateTime().plus(times.get(times.size() - 1));
          lastTimeForSignalGroup = new DateMidnight(TimeUtil.unformatDate(getEndDate())).toDateTime()
                                                                      .withMillisOfDay(lastTimeForDay.getMillisOfDay());
        } else {
          lastTimeForSignalGroup = new DateMidnight(TimeUtil.unformatDate(getEndDate())).plusDays(1).toDateTime();
        }
      } else {
        lastTimeForSignalGroup = new DateMidnight(TimeUtil.unformatDate(getEndDate())).plusDays(1).toDateTime();
      }
      if (lastTime == null || lastTimeForSignalGroup.isAfter(lastTime)) {
        lastTime = lastTimeForSignalGroup;
      }
    }
    return lastTime;
  }

  @JsonIgnore
  public DateTime getStartDateTime() {
    DateTime firstTime = null;
    for (SignalingMechanism signalingMechanism : getSignalingMechanisms()) {
      DateTime firstTimeForSignalGroup = null;
      if (signalingMechanism instanceof SignalSchedule) {
        if (((SignalSchedule) signalingMechanism).getScheduleType().equals(SignalSchedule.WEEKDAY)) {
          List<Long> times = schedule.getTimes();
          Collections.sort(times);
          DateTime firstTimeForDay = new DateTime().plus(times.get(0));
          firstTimeForSignalGroup = new DateMidnight(TimeUtil.unformatDate(getStartDate())).toDateTime()
                                                                      .withMillisOfDay(firstTimeForDay.getMillisOfDay());
        } else {
          firstTimeForSignalGroup = new DateMidnight(TimeUtil.unformatDate(getStartDate())).toDateTime();
        }
      } else {
        firstTimeForSignalGroup = new DateMidnight(TimeUtil.unformatDate(getStartDate())).toDateTime();
      }
      if (firstTime == null || firstTimeForSignalGroup.isBefore(firstTime)) {
        firstTime = firstTimeForSignalGroup;
      }
    }
    return firstTime;
  }


  private boolean isExperimentOver(DateTime now) {
    return isFixedDuration() != null && isFixedDuration() && now.isAfter(getEndDateTime());
  }

  private DateTime lookupNextTimeOnEsmSchedule(DateTime now, Context context) {
    AlarmStore alarmStore = new AlarmStore(context);
    List<DateTime> signals = alarmStore.getSignals(getId(), getPeriodStart(now).getMillis());

    DateTime next = getNextSignalAfterNow(now, signals);
    if (next != null) {
    	return next;
    }
    DateTime nextPeriod = now.plusDays(getSchedule().convertEsmPeriodToDays());
    if (schedule.convertEsmPeriodToDays() == 1 && !schedule.getEsmWeekends() && TimeUtil.isWeekend(nextPeriod)) {
      nextPeriod = TimeUtil.skipWeekends(nextPeriod);
    }

    ensureScheduleIsGeneratedForPeriod(nextPeriod, context);
    signals = alarmStore.getSignals(getId(), getPeriodStart(nextPeriod).getMillis());
    return getNextSignalAfterNow(now, signals);
  }

  private DateTime getNextSignalAfterNow(DateTime now, List<DateTime> signals) {
    if (signals.size() == 0) {
      return null;
    }
    Collections.sort(signals);
    for (DateTime dateTime : signals) {
      if (!now.isAfter(dateTime)) {
        return dateTime;
      }
    }
    return null;
  }

  private void ensureScheduleIsGeneratedForPeriod(DateTime now, Context context) {
    DateMidnight periodStart = getPeriodStart(now);
    AlarmStore alarmStore = new AlarmStore(context);
    List<DateTime> signalTimes = alarmStore.getSignals(getId(),
        periodStart.getMillis());

    if (signalTimes.size() == 0) {
      generateNextPeriod(periodStart, alarmStore);
    }

  }

  @JsonIgnore
  DateMidnight getPeriodStart(DateTime now) {
    switch (schedule.getEsmPeriodInDays()) {
    case SignalSchedule.ESM_PERIOD_DAY:
      return now.toDateMidnight();
    case SignalSchedule.ESM_PERIOD_WEEK:
      return now.dayOfWeek().withMinimumValue().toDateMidnight();
    case SignalSchedule.ESM_PERIOD_MONTH:
      return now.dayOfMonth().withMinimumValue().toDateMidnight();
    default:
      throw new IllegalStateException("Cannot get here.");
    }
  }

  private void generateNextPeriod(DateMidnight generatingPeriodStart, AlarmStore alarmStore) {
    if (isExperimentOver(generatingPeriodStart.toDateTime())) {
      return;
    }
    alarmStore.deleteSignalsForPeriod(getId(), generatingPeriodStart.getMillis());

    List<DateTime> signalTimes = generateSignalTimesForPeriod(generatingPeriodStart);
    storeSignalTimes(generatingPeriodStart, signalTimes, alarmStore);
  }

  private List<DateTime> generateSignalTimesForPeriod(DateMidnight periodStart) {
    return new EsmGenerator2().generateForSchedule(periodStart.toDateTime(), getSchedule());
  }

  private void storeSignalTimes(DateMidnight periodStart, List<DateTime> times, AlarmStore alarmStore) {
    long periodStartMillis = periodStart.getMillis();
    for (DateTime alarmTime : times) {
      alarmStore.storeSignal(periodStartMillis, getId(), alarmTime.getMillis());
    }
  }

  @JsonIgnore
  public Integer getExpirationTimeInMinutes() {
    SignalingMechanism signalingMechanism = getSignalingMechanisms().get(0);
    if (signalingMechanism instanceof Trigger) {
      return  signalingMechanism.getTimeout();
    } else {
      Integer timeout = ((SignalSchedule) signalingMechanism).getTimeout();
      return timeout != null ? timeout : getOldDefaultValuesForTimeout();
    }
  }

  private Integer getOldDefaultValuesForTimeout() {
    if (getSchedule().getScheduleType().equals(SignalSchedule.ESM)) {
      return 59;
    } else {
      return 479;
    }
  }

  @JsonIgnore
  public long getExpirationTimeInMillis() {
      return getExpirationTimeInMinutes() * 60000;
  }

  @JsonIgnore
  public boolean isExpiringAlarm() {
    return true;
  }

  @JsonIgnore
  public Input getInputById(long inputServerId) {
    for (Input input : inputs) {
      if (input.getServerId().equals(inputServerId)) {
        return input;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return getTitle();
  }

  public Boolean isWebRecommended() {
    return webRecommended;
  }

  public void setWebRecommended(Boolean webRecommended) {
    this.webRecommended = webRecommended;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  @JsonIgnore
  public boolean shouldTriggerBy(int event, String sourceIdentifier) {
    return trigger != null && trigger.match(event, sourceIdentifier);
  }

  @JsonIgnore
  public boolean shouldPoll() {
    return trigger != null && trigger.getEventCode() == Trigger.APP_USAGE;
  }


  @JsonIgnore
  public void setJson(String json) {
    this.json = json;
  }

  @JsonIgnore
  public String getJson() {
    return this.json;
  }

  public List<SignalingMechanism> getSignalingMechanisms() {
    return signalingMechanisms;
  }

  public void setSignalingMechanisms(List<SignalingMechanism> signalingMechanisms) {
    this.signalingMechanisms = signalingMechanisms;
    SignalingMechanism signalingMechanism = signalingMechanisms.get(0);
    if (signalingMechanism instanceof SignalSchedule) {
      this.schedule = (SignalSchedule) signalingMechanism;
    } else if (signalingMechanism instanceof Trigger) {
      this.trigger = (Trigger)signalingMechanism;
    }
  }

  public Boolean isCustomRendering() {
    return customRendering;
  }

  public void setCustomRendering(Boolean customRendering) {
    this.customRendering = customRendering;
  }

  public String getCustomRenderingCode() {
    return customRenderingCode;
  }

  public void setCustomRenderingCode(String customRenderingCode) {
    this.customRenderingCode = customRenderingCode;
  }

  public Integer getFeedbackType() {
    return feedbackType;
  }

  public void setFeedbackType(Integer feedbackType) {
    this.feedbackType = feedbackType;
  }

  public boolean isRunning(DateTime now) {
    return  (isFixedDuration() == null || !isFixedDuration()) || (isFixedDuration() && isStarted(now) && !isOver(now));
  }

  public boolean isStarted(DateTime now) {
    return isFixedDuration() == null || !isFixedDuration() || !now.isBefore(getStartDateTime());
  }


}
