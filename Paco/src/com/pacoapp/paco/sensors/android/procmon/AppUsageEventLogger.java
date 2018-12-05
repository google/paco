package com.pacoapp.paco.sensors.android.procmon;

import java.util.List;

import org.joda.time.DateTime;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.google.common.base.Strings;
import com.pacoapp.paco.model.Event;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.Output;
import com.pacoapp.paco.shared.model2.EventStore;
import com.pacoapp.paco.shared.model2.ExperimentGroup;

/**
 * Logs apps used as Paco Events
 *
 */
public class AppUsageEventLogger {

  private Context context;
  private List<Experiment> experimentsNeedingEvent;
  private EventStore eventStore;

  public AppUsageEventLogger(Context context, List<Experiment> experimentsNeedingEvent, EventStore eu) {
    this.context = context;
    this.experimentsNeedingEvent = experimentsNeedingEvent;
    this.eventStore = eu;
  }

  public void logProcessesUsedSinceLastPolling(AppUsageEvent usageStats) {
    String prettyAppNames = getNamesForApps(usageStats);
    for (Experiment experiment : experimentsNeedingEvent) {
      Event event = createAppsUsedPacoEvent(prettyAppNames, usageStats.getAppIdentifier(), experiment);
      eventStore.insertEvent(event);
    }

  }

  private String getNamesForApps(AppUsageEvent usageStats) {
    PackageManager pm = context.getPackageManager();
      String activityName = usageStats.getPkgName();
      ApplicationInfo info = null;
      try {
        info = pm.getApplicationInfo(activityName, 0);
        String appName = pm.getApplicationLabel(info).toString();
        if (Strings.isNullOrEmpty(appName)) {
          appName = activityName;
        } else if (appName.equals("Google Search")) {
          String[] parts = appName.split(".");
          if (parts.length > 0) {
            String simpleActivityName = parts[parts.length - 1];
            if (simpleActivityName.equals("GEL")) {
              appName = "Launcher";
            }
          }
        }
        if (appName.equals("Launcher")) {
          appName = "Home";
        }
        return appName;
      } catch (final NameNotFoundException e) {
        return activityName;
      }
  }

  private Event createAppsUsedPacoEvent(String usedAppsPrettyNamesString, String usedAppsTaskNamesString,
                                        Experiment experiment) {
    Event event = new Event();
    event.setExperimentId(experiment.getId());
    event.setServerExperimentId(experiment.getServerId());
    event.setExperimentName(experiment.getExperimentDAO().getTitle());


    event.setExperimentGroupName(experimentGroupFor(experiment));
    event.setExperimentVersion(experiment.getExperimentDAO().getVersion());
    event.setResponseTime(new DateTime());

    Output responseForInput = new Output();
    responseForInput.setAnswer(usedAppsPrettyNamesString);
    responseForInput.setName("apps_used");
    event.addResponse(responseForInput);

    Output usedAppsNamesResponse = new Output();
    usedAppsNamesResponse.setAnswer(usedAppsTaskNamesString);
    usedAppsNamesResponse.setName("apps_used_raw");
    event.addResponse(usedAppsNamesResponse);

    Output startResponse = new Output();
    startResponse.setAnswer("true");
    startResponse.setName("foreground");
    event.addResponse(startResponse);

    return event;
  }

  /**
   * This takes the first group because there really should only be one.
   * @param experiment
   * @return
   */
  private String experimentGroupFor(Experiment experiment) {
    List<ExperimentGroup> groups = experiment.getExperimentDAO().getGroups();
    for (ExperimentGroup experimentGroup : groups) {
      if (experimentGroup.getLogActions() != null && experimentGroup.getLogActions() == true) {
        return experimentGroup.getName();
      }
    }
    return null;
  }




}
