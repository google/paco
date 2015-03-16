package com.google.android.apps.paco.utils;

import android.content.Context;

import com.google.android.apps.paco.Experiment;
import com.google.android.apps.paco.ExperimentProviderUtil;
import com.google.android.apps.paco.JavascriptEventLoader;
import com.google.android.apps.paco.JavascriptExperimentLoader;
import com.google.android.apps.paco.JavascriptNotificationService;
import com.google.paco.shared.model2.ExperimentGroup;

public class AndroidJsInterpreterBuilder {


  private AndroidJsInterpreterBuilder() {

  }

  public static JsInterpreter createInterpreter(Context context, Experiment experiment, ExperimentGroup experimentGroup) {
    JsInterpreter interpreter = new JsInterpreter();
    ExperimentProviderUtil experimentProvider = new ExperimentProviderUtil(context);
    interpreter.bind("experimentLoader", new JavascriptExperimentLoader(context, experimentProvider, experiment));
    interpreter.bind("db", new JavascriptEventLoader(experimentProvider, experiment, null));
    interpreter.bind("notificationService", new JavascriptNotificationService(context, experiment, experimentGroup));
    return interpreter;

  }
}
