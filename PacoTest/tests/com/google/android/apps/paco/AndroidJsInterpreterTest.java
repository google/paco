package com.google.android.apps.paco;

import org.junit.Test;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.mock.MockContext;

import com.google.android.apps.paco.utils.AndroidJsInterpreterBuilder;
import com.google.android.apps.paco.utils.JsInterpreter;

public class AndroidJsInterpreterTest extends AndroidTestCase {


  @Test
  public void testAndroidJsObjects() throws Exception {
    Context context = new MockContext();
    Experiment experiment = new Experiment();
    JsInterpreter interpreter = AndroidJsInterpreterBuilder.createInterpreter(context, experiment, null, null);
    assertTrue((Boolean)interpreter.eval("paco.notificationService !== null") == true);
  }

}
