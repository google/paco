package com.google.android.apps.paco;

import org.junit.Test;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.mock.MockContext;

import com.pacoapp.paco.js.interpreter.AndroidJsInterpreterBuilder;
import com.pacoapp.paco.js.interpreter.JsInterpreter;
import com.pacoapp.paco.model.Experiment;

public class AndroidJsInterpreterTest extends AndroidTestCase {


  @Test
  public void testAndroidJsObjects() throws Exception {
    Context context = new MockContext();
    Experiment experiment = new Experiment();
    JsInterpreter interpreter = AndroidJsInterpreterBuilder.createInterpreter(context, experiment, null, null);
    assertTrue((Boolean)interpreter.eval("paco.notificationService !== null") == true);
  }
  
  @Test
  public void testStringBundle() {
    Context context = new MockContext();
    Experiment experiment = new Experiment();
    JsInterpreter interpreter = AndroidJsInterpreterBuilder.createInterpreter(context, experiment, null, null);
    assertTrue((Boolean)interpreter.eval("paco.stringService !== null") == true);
    assertEquals("Paco", interpreter.eval("paco.stringService.getString(\"app_name\""));
  }

}
