package com.google.android.apps.paco.utils;

import java.util.logging.Logger;

import android.content.Context;

import com.google.android.apps.paco.Experiment;
import com.google.android.apps.paco.ExperimentProviderUtil;
import com.google.android.apps.paco.JavascriptEventLoader;
import com.google.android.apps.paco.JavascriptExperimentLoader;
import com.google.android.apps.paco.JavascriptNotificationService;
import com.google.paco.shared.model2.ExperimentDAO;
import com.google.paco.shared.model2.ExperimentGroup;

public class AndroidJsInterpreterBuilder {


  private AndroidJsInterpreterBuilder() {

  }

  public static class JavascriptLogger {

    private Logger logger;

    public JavascriptLogger() {
      logger = Logger.getLogger("SCRIPT_EXECUTOR");
    }

    public void info(String message) {
       logger.info(message);
    }

    public void error(String message) {
      logger.severe(message);
    }

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
