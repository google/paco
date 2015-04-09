package com.pacoapp.paco.js.interpreter;

import android.content.Context;

import com.google.paco.shared.model2.ExperimentDAO;
import com.google.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.js.bridge.JavascriptEventLoader;
import com.pacoapp.paco.js.bridge.JavascriptExperimentLoader;
import com.pacoapp.paco.js.bridge.JavascriptLogger;
import com.pacoapp.paco.js.bridge.JavascriptNotificationService;
import com.pacoapp.paco.js.bridge.JavascriptSensorManager;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;

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
