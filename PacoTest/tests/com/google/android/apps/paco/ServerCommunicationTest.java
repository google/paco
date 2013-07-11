package com.google.android.apps.paco;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;

import android.test.AndroidTestCase;

public class ServerCommunicationTest extends AndroidTestCase {
  
  ServerCommunication serverCommunication;
  UserPreferences userPrefs;
  Long oldWakeupTime;
  
  protected void setUp() {
    serverCommunication = ServerCommunication.getInstance(getContext());
    userPrefs = new UserPreferences(getContext());
    oldWakeupTime = userPrefs.getNextServerCommunicationServiceAlarmTime();
  }

  public void testUpdateMoreThan24HoursBeforeWakeupTime() {
    DateTimeUtils.setCurrentMillisFixed(new DateTime(oldWakeupTime).minusHours(30).getMillis());
    serverCommunication.checkIn(true);
    long nextWakeupTime = userPrefs.getNextServerCommunicationServiceAlarmTime();
    assertTrue(new DateTime(nextWakeupTime).isAfterNow());
  }
  
  public void testUpdate24HoursBeforeWakeupTime() {
    DateTimeUtils.setCurrentMillisFixed(new DateTime(oldWakeupTime).minusHours(24).getMillis());
    serverCommunication.checkIn(true);
    long nextWakeupTime = userPrefs.getNextServerCommunicationServiceAlarmTime();
    assertTrue(new DateTime(nextWakeupTime).isAfterNow());
  }
  
  public void testUpdateFewerThan24HoursBeforeWakeupTime() {
    DateTimeUtils.setCurrentMillisFixed(new DateTime(oldWakeupTime).minusHours(20).getMillis());
    serverCommunication.checkIn(true);
    long nextWakeupTime = userPrefs.getNextServerCommunicationServiceAlarmTime();
    assertTrue(new DateTime(nextWakeupTime).isAfterNow());
  }
  
  public void testUpdateAtWakeupTime() {
    DateTimeUtils.setCurrentMillisFixed(oldWakeupTime);
    serverCommunication.checkIn(true);
    long nextWakeupTime = userPrefs.getNextServerCommunicationServiceAlarmTime();
    assertTrue(new DateTime(nextWakeupTime).isAfterNow());
  }
  
  public void testUpdateFewerThan24HoursAfterWakeupTime() {
    DateTimeUtils.setCurrentMillisFixed(new DateTime(oldWakeupTime).plusHours(20).getMillis());
    serverCommunication.checkIn(true);
    long nextWakeupTime = userPrefs.getNextServerCommunicationServiceAlarmTime();
    assertTrue(new DateTime(nextWakeupTime).isAfterNow());
  }
  
  public void testUpdate24HoursAfterWakeupTime() {
    DateTimeUtils.setCurrentMillisFixed(new DateTime(oldWakeupTime).plusHours(24).getMillis());
    serverCommunication.checkIn(true);
    long nextWakeupTime = userPrefs.getNextServerCommunicationServiceAlarmTime();
    assertTrue(new DateTime(nextWakeupTime).isAfterNow());
  }
  
  public void testUpdateMoreThan24HoursAfterWakeupTime() {
    DateTimeUtils.setCurrentMillisFixed(new DateTime(oldWakeupTime).plusHours(30).getMillis());
    serverCommunication.checkIn(true);
    long nextWakeupTime = userPrefs.getNextServerCommunicationServiceAlarmTime();
    assertTrue(new DateTime(nextWakeupTime).isAfterNow());
  }
  
  protected void tearDown() {
    DateTimeUtils.setCurrentMillisSystem();
    if (new DateTime(oldWakeupTime).isAfterNow()) {
      userPrefs.setNextServerCommunicationServiceAlarmTime(oldWakeupTime);
    } else {
      userPrefs.setNextServerCommunicationServiceAlarmTime(new DateTime().plusSeconds(10).getMillis());
    }
  }
  
}
