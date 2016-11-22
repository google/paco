package com.pacoapp.paco.triggering;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pacoapp.paco.js.interpreter.AndroidJsInterpreterBuilder;
import com.pacoapp.paco.js.interpreter.JsInterpreter;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.shared.model2.ActionTrigger;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.PacoAction;
import com.pacoapp.paco.shared.model2.PacoActionAllOthers;
import com.pacoapp.paco.shared.scheduling.ActionScheduleGenerator;
import com.pacoapp.paco.shared.scheduling.ActionSpecification;

import android.content.Context;
import android.content.res.AssetManager;

public class AndroidActionExecutor {

  private static Logger Log = LoggerFactory.getLogger(AndroidActionExecutor.class);

  private Context context;
  private ExperimentProviderUtil experimentProviderUtil;

  public AndroidActionExecutor(Context context) {
    this.context = context;
  }

  public static AndroidActionExecutor getInstance(Context context) {
    return new AndroidActionExecutor(context);
  }

  public void runAllActionsForAlarmTime(long alarmTime) {
    experimentProviderUtil = new ExperimentProviderUtil(context);
    DateTime alarmAsDateTime = new DateTime(alarmTime);
    Log.info("Running all actions for last minute from signaled alarmTime: " + alarmAsDateTime.toString());

    Map<ExperimentDAO, Experiment> experimentDAOtoExperimentMap = Maps.newHashMap(); // TODO Just until we remove the Android experiment id from Event.
    List<ExperimentDAO> experimentDAOs = Lists.newArrayList();
    for (Experiment experiment : experimentProviderUtil.getJoinedExperiments()) {
      final ExperimentDAO experimentDAO = experiment.getExperimentDAO();
      experimentDAOs.add(experimentDAO);

      experimentDAOtoExperimentMap.put(experimentDAO, experiment);
    }
    List<ActionSpecification> times = ActionScheduleGenerator.getAllAlarmsWithinOneMinuteofNow(alarmAsDateTime.minusSeconds(59),
        experimentDAOs, new AndroidEsmSignalStore(context), experimentProviderUtil);

    for (ActionSpecification timeExperiment : times) {
      if (timeExperiment.action != null) {
        continue; // skip notification actions
      }
      final ActionTrigger actionTrigger = timeExperiment.actionTrigger;
      List<PacoAction> actions = actionTrigger.getActions();
      Experiment experiment = experimentDAOtoExperimentMap.get(timeExperiment.experiment);
      for (PacoAction pacoAction : actions) {
        runAction(context, pacoAction, experiment, timeExperiment.experiment, timeExperiment.experimentGroup, timeExperiment.actionTriggerSpecId, timeExperiment.actionTrigger.getId());
      }
  }

}

  public static void runAction(Context context, PacoAction pacoAction, Experiment experiment, ExperimentDAO experimentDAO, ExperimentGroup experimentGroup, Long actionTriggerSpecId, Long actionTriggerId) {
    int actionCode = pacoAction.getActionCode();
    switch (actionCode) {
    case PacoAction.NOTIFICATION_ACTION_CODE:
      break;
    case PacoAction.LOG_EVENT_ACTION_CODE:
      break;

    case PacoAction.EXECUTE_SCRIPT_ACTION_CODE:
      JsInterpreter interpreter = AndroidJsInterpreterBuilder.createInterpreter(context, experiment,
                                                                                experimentDAO,
                                                                                experimentGroup,
                                                                                actionTriggerSpecId,
                                                                                actionTriggerId,
                                                                                pacoAction.getId());
      String customScript = ((PacoActionAllOthers) pacoAction).getCustomScript();
      if (customScript != null) {
        // TODO - Either sanitize the code here, or, when it is uploaded to the
        // server.
        String baseScript = interpreterBase() + getBaseScript(context);
        Log.info("Evaluating Script action in interpreter.");
        interpreter.eval(baseScript + "\n" + customScript);
        interpreter.exit();
      }
      break;
    default:
      throw new IllegalArgumentException("Undefined action code!");
    }
  }

  public static String interpreterBase() {
    return "function alert(msg) { log.error(msg); };\n";
  }

  public static String getBaseScript(Context context) {
    AssetManager assetManager = context.getAssets();
    StringBuilder buf;
    try {
      InputStream ims = assetManager.open("custom_base_interpreter.js");
      BufferedReader reader = new BufferedReader(new InputStreamReader(ims));
      String line;
      buf = new StringBuilder();
      while ((line = reader.readLine()) != null) {
        buf.append(line);
        buf.append("\n");
      }
      return buf.toString();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return "";
  }
}
