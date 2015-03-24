package com.google.android.apps.paco.js.interpreter;

import android.content.Context;

import com.google.android.apps.paco.Experiment;
import com.google.android.apps.paco.ExperimentProviderUtil;
import com.google.android.apps.paco.js.bridge.JavascriptEventLoader;
import com.google.android.apps.paco.js.bridge.JavascriptExperimentLoader;
import com.google.android.apps.paco.js.bridge.JavascriptLogger;
import com.google.android.apps.paco.js.bridge.JavascriptNotificationService;
import com.google.android.apps.paco.js.bridge.JavascriptSensorManager;
import com.google.paco.shared.model2.ExperimentDAO;
import com.google.paco.shared.model2.ExperimentGroup;

public class AndroidJsInterpreterBuilder {


  private AndroidJsInterpreterBuilder() {

  }

  public static JsInterpreter createInterpreter(Context context, Experiment androidExperiment, ExperimentDAO experiment, ExperimentGroup experimentGroup) {
    JsInterpreter interpreter = new JsInterpreter();
    ExperimentProviderUtil experimentProvider = new ExperimentProviderUtil(context);
    interpreter.newBind("db", new JavascriptEventLoader(experimentProvider, androidExperiment, experiment, experimentGroup));

    interpreter.newBind("experimentLoader", new JavascriptExperimentLoader(context, experimentProvider, experiment, androidExperiment));
    interpreter.newBind("notificationService", new JavascriptNotificationService(context, experiment, experimentGroup));
    interpreter.newBind("log", new JavascriptLogger());

    interpreter.newBind("sensors", new JavascriptSensorManager(context));
    return interpreter;

  }
}
