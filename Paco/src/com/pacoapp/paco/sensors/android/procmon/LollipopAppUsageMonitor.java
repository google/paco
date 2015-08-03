package com.pacoapp.paco.sensors.android.procmon;

import java.util.List;

import org.joda.time.DateTime;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pacoapp.paco.PacoConstants;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.sensors.android.BroadcastTriggerReceiver;
import com.pacoapp.paco.sensors.android.BroadcastTriggerService;
import com.pacoapp.paco.shared.model2.InterruptCue;
import com.pacoapp.paco.shared.util.TimeUtil;

@SuppressLint("NewApi")
public class LollipopAppUsageMonitor {

  private boolean inBrowser;

  // system services
  private Context context;
  private AppUsageEventsService usageStatsManager;
  private AppUsageEventLogger processUsageEventBuilder;
  private LollipopAppUseChangeDetector appUseChangeDetector;
  private AppChangeListener appChangeListener;

  public LollipopAppUsageMonitor(List<String> tasksOfInterest,
                                      List<String> tasksOfInterestForClosing,
                                      Context context,
                                      AppUsageEventsService usageEventsService,
                                      AppUsageEventLogger pueb) {
    this.context = context;
    this.usageStatsManager = usageEventsService;
    this.processUsageEventBuilder = pueb;

    inBrowser = false; //BroadcastTriggerReceiver.isInBrowser(context);
                       // is the concern that we got restarted and lost the
                       //fact that we were in the browser before this service got restarted?

    this.appChangeListener = createAppChangeListener();
    this.appUseChangeDetector = new LollipopAppUseChangeDetector(tasksOfInterest,
                                                                 tasksOfInterestForClosing,
                                                                 appChangeListener);
  }

  public AppChangeListener createAppChangeListener() {
    return new AppChangeListener() {

      @Override
      public void appOpened(AppUsageEvent event) {
        final boolean shouldLogActions = BroadcastTriggerReceiver.shouldLogActions(context);
        if (isBrowserTask(event) && shouldLogActions) {
          BroadcastTriggerReceiver.createBrowserHistoryStartSnapshot(context);
          BroadcastTriggerReceiver.toggleInBrowser(context, true);
          inBrowser = true;
        }

        triggerAppUsed(event.getAppIdentifier());


        if (shouldLogActions) {
          // nutso that we use the same flag for logging apps used and sites visited
          processUsageEventBuilder.logProcessesUsedSinceLastPolling(event);
        }

      }

      @Override
      public void appClosed(AppUsageEvent event) {
        if (inBrowser == true && isBrowserTask(event)) {
          inBrowser = false;
          BroadcastTriggerReceiver.toggleInBrowser(context, false);
          BroadcastTriggerReceiver.createBrowserHistoryEndSnapshot(context);
        }
        triggerAppClosed(event.getAppIdentifier());
      }
    };
  }

  public void detectUsageEvents() {
    List<AppUsageEvent> usageEventsFriendly = getUsageEvents();
    //printEvents(usageEventsFriendly);
    appUseChangeDetector.newEvents(usageEventsFriendly);
  }

  private boolean isBrowserTask(AppUsageEvent usageStats) {
    String packageName = usageStats.getPkgName();
    return packageName.startsWith("com.android.browser")
        || packageName.startsWith("com.android.chrome")
        || packageName.startsWith("org.mozilla.firefox");
  }

  public List<AppUsageEvent> getUsageEvents() {
    return usageStatsManager.getUsageEvents();
  }

  public void printEvents(List<AppUsageEvent> usageEventsFriendly) {
    for (AppUsageEvent usageEvent : usageEventsFriendly) {
      StringBuffer b = new StringBuffer();
      b.append("event = new AppUsageEvent(\"").append(usageEvent.getPkgName());
      b.append("\", \"").append(usageEvent.getClassName());
      b.append("\", ").append(usageEvent.getType());
      b.append(", ").append(usageEvent.getTimestamp()).append("l);");

      Log.i(PacoConstants.TAG, b.toString() + "\n");
    }
  }

  private void triggerAppUsed(String appIdentifier) {
    Log.i(PacoConstants.TAG, "Paco App Usage poller trigger app used: " + appIdentifier);
    triggerCodeForAppTrigger(appIdentifier, InterruptCue.APP_USAGE);
  }

  private void triggerAppClosed(String appIdentifier) {
    Log.i(PacoConstants.TAG, "Paco App Usage poller trigger app used: " + appIdentifier);
    triggerCodeForAppTrigger(appIdentifier, InterruptCue.APP_CLOSED);
  }

  // Visible for overriding for testing
  protected void triggerCodeForAppTrigger(String appIdentifier, int triggerCode) {
    Intent broadcastTriggerServiceIntent = new Intent(context, BroadcastTriggerService.class);
    broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGERED_TIME, DateTime.now().toString(TimeUtil.DATETIME_FORMAT));

    broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGER_EVENT, triggerCode);
    broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGER_SOURCE_IDENTIFIER, appIdentifier);
    context.startService(broadcastTriggerServiceIntent);
  }

}
