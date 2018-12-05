package com.google.android.apps.paco;

import org.junit.Test;

import com.pacoapp.paco.js.interpreter.AndroidJsInterpreterBuilder;
import com.pacoapp.paco.js.interpreter.JsInterpreter;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.triggering.AndroidActionExecutor;

import android.content.Context;
import android.test.AndroidTestCase;

public class AndroidJsInterpreterTest extends AndroidTestCase {


  @Test
  public void testAndroidJsObjects() throws Exception {
    Context context = getContext();
    Experiment experiment = new Experiment();
    JsInterpreter interpreter = AndroidJsInterpreterBuilder.createInterpreter(context, experiment, null, null, null, null, null);
    final AndroidActionExecutor executor = AndroidActionExecutor.getInstance(context);
    String baseScript = executor.interpreterBase() + executor.getBaseScript(context);


    assertTrue((Boolean)interpreter.eval(baseScript + "\n" + "paco.notificationService !== null") == true);
  }

  @Test
  public void testStringBundle() {
    Context context = getContext();
    Experiment experiment = new Experiment();
    JsInterpreter interpreter = AndroidJsInterpreterBuilder.createInterpreter(context, experiment, null, null, null, null, null);
    final AndroidActionExecutor executor = AndroidActionExecutor.getInstance(context);
    String baseScript = executor.interpreterBase() + executor.getBaseScript(context);

    assertTrue((Boolean)interpreter.eval(baseScript + "\n" + "paco.stringService !== null") == true);
    assertEquals("Paco", interpreter.eval("paco.stringService.getString(\"app_name\")"));
  }

}
