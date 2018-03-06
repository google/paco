package com.google.sampling.experiential.server.viz.appusage;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.shared.EventDAO;

/**
 * Contains phone sessions which contain appsessions which contain appscreen sessions which may contain clicks.
 *
 *
 */
public class PhoneSessionLog extends BaseSession {

  private List<PhoneSession> phoneSessions;
  private String who;

  public PhoneSessionLog(String who, List<PhoneSession> phoneSessions) {
    super();
    this.who = who;
    this.phoneSessions = phoneSessions;
    if (!phoneSessions.isEmpty()) {
      setStartTime(phoneSessions.get(0).getStartTime());
      setEndTime(phoneSessions.get(phoneSessions.size() - 1).getEndTime());
    }
  }

  public static PhoneSessionLog buildSessions(String who, List<EventDAO> events) {
    return buildSessionsWithOptions(who, events, 0);
  }

  public static PhoneSessionLog buildSessionsWithOptions(String who, List<EventDAO> events, int minimumSessionBreak) {
    List<PhoneSession> phoneSessions = Lists.newArrayList();
    PhoneSession currentPhoneSession = null;

    for (EventDAO event : events) {
      if (currentPhoneSession == null) {
        if (!phoneSessions.isEmpty() && isContinuationOfSession(phoneSessions.get(phoneSessions.size() - 1),
                                                                new DateTime(event.getResponseTime()),
                                                                minimumSessionBreak)) {
          currentPhoneSession = phoneSessions.get(phoneSessions.size() - 1); // resume because we got a userPresent to close to the last userNotPresent
          currentPhoneSession.endSession(null);
          continue;
        } else {
          currentPhoneSession = new PhoneSession(new DateTime(event.getResponseTime()));
        }
      }
      PhoneSession returnedPhoneSession = currentPhoneSession.handleEvent(event, minimumSessionBreak);
      if (returnedPhoneSession == null) {
        currentPhoneSession = null;
      } else if (!returnedPhoneSession.equals(currentPhoneSession)) {
//        if (isContinuationOfSession(currentPhoneSession, returnedPhoneSession.getStartTime(), minimumSessionBreak)) {
//          mergeIntoCurrentSession(currentPhoneSession, returnedPhoneSession);
//        } else {
          phoneSessions.add(returnedPhoneSession);
          currentPhoneSession = returnedPhoneSession;
        //}
      } else if (phoneSessions.isEmpty()) {
        phoneSessions.add(currentPhoneSession);
      }
    }
    ensureEndToLastSession(currentPhoneSession);
    return new PhoneSessionLog(who, phoneSessions);
  }

  public static PhoneSessionLog buildPhoneSessionsWithOptionsV2(String who, List<EventDAO> events, int minimumSessionBreakSeconds) {
    List<PhoneSession> phoneSessions = Lists.newArrayList();
    PhoneSession currentPhoneSession = null;

    for (EventDAO event : events) {
      if (currentPhoneSession == null) {
        if (!phoneSessions.isEmpty() && isContinuationOfSession(phoneSessions.get(phoneSessions.size() - 1),
                                                                new DateTime(event.getResponseTime()),
                                                                minimumSessionBreakSeconds)) {
          currentPhoneSession = phoneSessions.get(phoneSessions.size() - 1); // res
        } else {
          currentPhoneSession = new PhoneSession(new DateTime(event.getResponseTime()));
        }
      }
      PhoneSession returnedPhoneSession = currentPhoneSession.handleEvent(event, minimumSessionBreakSeconds);
      if (returnedPhoneSession == null) {
        currentPhoneSession = null;
      } else if (!returnedPhoneSession.equals(currentPhoneSession)) {
        if (isContinuationOfSession(currentPhoneSession, returnedPhoneSession.getStartTime(), minimumSessionBreakSeconds)) {
          mergeIntoCurrentSession(currentPhoneSession, returnedPhoneSession);
        } else {
          phoneSessions.add(returnedPhoneSession);
          currentPhoneSession = returnedPhoneSession;
        }
      } else if (phoneSessions.isEmpty()) {
        phoneSessions.add(currentPhoneSession);
      }
    }
    ensureEndToLastSession(currentPhoneSession);
    return new PhoneSessionLog(who, phoneSessions);
  }

  private static void mergeIntoCurrentSession(PhoneSession currentPhoneSession, PhoneSession returnedPhoneSession) {
    currentPhoneSession.getAppSessions().addAll(returnedPhoneSession.getAppSessions());
    currentPhoneSession.setEndTime(returnedPhoneSession.getEndTime());
  }

  private static boolean isContinuationOfSession(PhoneSession currentPhoneSession, DateTime newEventStartTime,
                                                 int minimumSessionBreak) {
    DateTime lastEnd = currentPhoneSession.getEndTime();
    int gap = Seconds.secondsBetween(lastEnd, newEventStartTime).getSeconds();
    return gap < minimumSessionBreak;
  }

  private static void ensureEndToLastSession(PhoneSession currentPhoneSession) {
    if (currentPhoneSession != null) {
      currentPhoneSession.ensureEndSession();
    }
  }

  public List<PhoneSession> getPhoneSessions() {
    return phoneSessions;
  }

  public void setPhoneSessions(List<PhoneSession> phoneSessions) {
    this.phoneSessions = phoneSessions;
  }

  public int phoneSessionCount() {
    if (phoneSessions == null) {
      return 0;
    }
    return phoneSessions.size();
  }

  public String getWho() {
    return who;
  }

  public void setWho(String who) {
    this.who = who;
  }

}
