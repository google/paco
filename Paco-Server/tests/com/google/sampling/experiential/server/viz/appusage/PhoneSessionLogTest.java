package com.google.sampling.experiential.server.viz.appusage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.shared.EventDAO;

public class PhoneSessionLogTest {

  @Test
  public void testSessionAnalysis_NoSession() {
    List<EventDAO> events = Lists.newArrayList();
    PhoneSessionLog analyzer = PhoneSessionLog.buildSessions("test@example.com", events);
    List<PhoneSession> sessions = analyzer.getPhoneSessions();
    assertTrue(sessions.isEmpty());
  }

  @Test
  public void testSessionAnalysis_OneSession() {
    /**
     * Phone PhoneSession from oneSession test data
     * userPrsent     1498069383000
     * Google App     1498069383000 0
     * Paco           1498069392000 9000
     * Google App     1498069404000 12000
     * Settings       1498069407000 3000
     * Google App     1498069412000 5000
     * Camera         1498069414000 2000
     * Google App     1498069421000 7000
     * Music          1498069423000 2000
     * Google App     1498069426000 3000
     * userNotPresent 1498069428000 2000
     */
    List<EventDAO> events = PhoneSessionTestConstants.getEventsForOneSession();
    PhoneSessionLog analyzer = PhoneSessionLog.buildSessions("test@example.com", events);
    List<PhoneSession> sessions = analyzer.getPhoneSessions();
    assertEquals(1, sessions.size());
    PhoneSession session = sessions.get(0);
    final List<AppSession> appSessions = session.getAppSessions();
    assertEquals(9, appSessions.size());

    assertAppSession(appSessions.get(0), "Google App", 1498069383000l, 1498069392000l, 9);
    assertAppSession(appSessions.get(1), "Paco", 1498069392000l, 1498069404000l, 12);
    assertAppSession(appSessions.get(2), "Google App", 1498069404000l, 1498069407000l, 3);
    assertAppSession(appSessions.get(3), "Settings", 1498069407000l, 1498069412000l, 5);
    assertAppSession(appSessions.get(4), "Google App", 1498069412000l, 1498069414000l, 2);
    assertAppSession(appSessions.get(5), "Camera", 1498069414000l, 1498069421000l, 7);
    assertAppSession(appSessions.get(6), "Google App", 1498069421000l, 1498069423000l, 2);
    assertAppSession(appSessions.get(7), "Music", 1498069423000l, 1498069426000l, 3);
    assertAppSession(appSessions.get(8), "Google App", 1498069426000l, 1498069428000l, 2);

  }

  @Test
  public void testMultipleSessions() throws Exception {
    List<EventDAO> events = PhoneSessionTestConstants.getEvents();
    PhoneSessionLog analyzer = PhoneSessionLog.buildSessions("test@example.com", events);
    List<PhoneSession> phoneSessions = analyzer.getPhoneSessions();

    assertEquals(8, phoneSessions.size());
    PhoneSession phoneSession1 = phoneSessions.get(0);

    final List<AppSession> appSessionsPhoneSession1 = phoneSession1.getAppSessions();
    assertEquals(1, appSessionsPhoneSession1.size());

    AppSession appSession1PhoneSession1 = appSessionsPhoneSession1.get(0);
    assertEquals("Paco", appSession1PhoneSession1.getAppName());
    assertEquals(1, appSession1PhoneSession1.getAppScreenSessions().size());

    PhoneSession phoneSession2 = phoneSessions.get(1);

    final List<AppSession> appSessionsPhoneSession2 = phoneSession2.getAppSessions();
    assertEquals(5, appSessionsPhoneSession2.size());

    AppSession appSession1PhoneSession2 = appSessionsPhoneSession2.get(0);
    assertEquals("Paco", appSession1PhoneSession2.getAppName());
    assertEquals(3, appSession1PhoneSession2.getAppScreenSessions().size());
  }

  @Test
  /**
   * phone session 1 ends userNotPresentEvent, then userPresentEvent, then
   * same app should be the same phoneSession if the phoneSession2Start - phoneSession1End
   * < N seconds. Where N is specifiable, but defaults to less than 5 seconds.
   *
   * @throws Exception
   */
  public void testOneSessionWithPhoneSleepInterruptionEvent() throws Exception {
    List<EventDAO> events = PhoneSessionTestConstants.getEventsForOneBrokenSession();
    PhoneSessionLog analyzer = PhoneSessionLog.buildSessions("test@example.com", events);
    // assert 3 sessions wthout a break detector
    assertEquals(3, analyzer.getPhoneSessions().size());

    // use small-break fixer
    PhoneSessionLog analyzerWithBreakDetector = PhoneSessionLog.buildSessionsWithOptions("test@example.com", events, 10);
    assertEquals(2, analyzerWithBreakDetector.getPhoneSessions().size());
  }

  private void assertAppSession(AppSession actualAppSession, final String expectedAppName, final long expectedStartTime,
                                final long expectedEndTime, final int expectedDurationSeconds) {
    assertEquals(expectedAppName, actualAppSession.getAppName());
    assertEquals(new DateTime(expectedStartTime), actualAppSession.getStartTime());
    assertEquals(new DateTime(expectedEndTime), actualAppSession.getEndTime());
    assertEquals(expectedDurationSeconds, actualAppSession.getDurationInSeconds());
  }

  @Test
  public void testNewPhoneSessionWithRandomEvent() throws Exception {
    List<EventDAO> events = PhoneSessionTestConstants.getUserResponseEvent();
    PhoneSessionLog log = PhoneSessionLog.buildSessions("test@example.com", events);
    assertEquals(1, log.getPhoneSessions().size());
  }
}
