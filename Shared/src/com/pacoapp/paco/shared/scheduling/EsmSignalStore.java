package com.pacoapp.paco.shared.scheduling;

import java.util.List;

import org.joda.time.DateTime;

public interface EsmSignalStore {

  public abstract void storeSignal(Long date, Long experimentId, Long alarmTime,
                                   String groupName, Long actionTriggerId, Long scheduleId);

  public abstract List<DateTime> getSignals(Long experimentId, Long periodStart,
                                            String groupName, Long actionTriggerId, Long scheduleId);

  public abstract void deleteAll();

  public abstract void deleteAllSignalsForSurvey(Long experimentId);

  public abstract void deleteSignalsForPeriod(Long experimentId,
                                              Long periodStart,
                                              String groupName, Long actionTriggerId, Long scheduleId);

}