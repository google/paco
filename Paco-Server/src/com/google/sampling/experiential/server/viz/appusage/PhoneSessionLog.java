package com.google.sampling.experiential.server.viz.appusage;

import java.util.List;

import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.shared.EventDAO;

/**
 * Contains phone sessions which contain appsessions which contain appscreen sessions which may contain clicks.
 *
 *
 */
public class PhoneSessionLog {



  public List<PhoneSession> buildSessions(List<EventDAO> events) {
    List<PhoneSession> phoneSessions = Lists.newArrayList();
    PhoneSession currentPhoneSession = null;

    for (EventDAO event : events) {
      if (currentPhoneSession == null) {
        currentPhoneSession = new PhoneSession(new DateTime(event.getResponseTime()));
      }
      PhoneSession returnedPhoneSession = currentPhoneSession.handleEvent(event);
      if (returnedPhoneSession == null) {
        currentPhoneSession = null;
      } else if (!returnedPhoneSession.equals(currentPhoneSession)) {
        phoneSessions.add(returnedPhoneSession);
        currentPhoneSession = returnedPhoneSession;
      } else if (phoneSessions.isEmpty()) {
        phoneSessions.add(currentPhoneSession);
      }
    }
    return phoneSessions;
  }
}
