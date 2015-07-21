package com.google.android.apps.paco.test;

import com.pacoapp.paco.triggering.AlarmCreator2;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Context;
import android.test.InstrumentationTestCase;

public class AlarmCreator2Test extends InstrumentationTestCase {

  public void testAlarmCreatorNoExperiments() throws Exception {
    Context targetContext = getInstrumentation().getTargetContext();
    AlarmCreator2 ac = AlarmCreator2.createAlarmCreator(targetContext);
    ac.updateAlarm();
    AlarmManager am = (AlarmManager)targetContext.getSystemService(Service.ALARM_SERVICE);
    assertNotNull(am);
  }
}
