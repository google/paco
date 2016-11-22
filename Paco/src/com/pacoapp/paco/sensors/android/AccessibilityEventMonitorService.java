package com.pacoapp.paco.sensors.android;

import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.InterruptCue;
import com.pacoapp.paco.shared.model2.InterruptTrigger;
import com.pacoapp.paco.shared.util.ExperimentHelper;
import com.pacoapp.paco.shared.util.TimeUtil;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;

/**
 *
 * This service is enabled when Paco is granted the Accessibility permission. The
 * BroadcastTriggerService will make sure that only experiments enabling accessibility logging will
 * receive accessibility events.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP) // TODO: update to Marshmallow when project SDK changes
public class AccessibilityEventMonitorService extends AccessibilityService {

  public static final String ACCESSIBILITY_EVENT_TEXT = "accessibilityEventText";
  public static final String ACCESSIBILITY_EVENT_PACKAGE = "accessibilityEventPackage";
  public static final String ACCESSIBILITY_EVENT_TYPE = "accessibilityEventType";
  public static final String ACCESSIBILITY_EVENT_CLASS = "accessibilityEventClass";
  public static final String ACCESSIBILITY_EVENT_CONTENT_DESCRIPTION = "accessibilityEventContentDescription";

  private static Logger Log = LoggerFactory.getLogger(AccessibilityEventMonitorService.class);

    // Keeps whether the service is connected
  private static boolean running = false;
  private RuntimePermissionsAccessibilityEventHandler runtimePermissionsEventHandler;

  /**
   * Called to allow experiments that trigger on accessibility events.
   * {@inheritDoc}
   */
  @Override
  public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
    // TODO make this a flag when joining or stopping or expiring or refreshing experiments
    // TODO or at least cache the experiments somewhere, like in the Application Object
    List<Experiment> experiments = new ExperimentProviderUtil(this).getJoinedExperiments();
    List<ExperimentDAO> experimentDAOs = Lists.newArrayList();
    final DateTime now = DateTime.now();
    for (Experiment experiment : experiments) {
      if (experiment.isRunning(now)) {
        experimentDAOs.add(experiment.getExperimentDAO());
      }
    }
    List<InterruptTrigger> interestingTriggers;
    if (!ExperimentHelper.doesAnyExperimentCareAboutAccessibilityEvents(experimentDAOs)) {
      //Log.debug("No experiments running that care about accessbility events");
      return;
    } else {
      //Log.debug("1 or more experiments running that care about accessbility events");
      interestingTriggers = ExperimentHelper.getAccessibilityTriggersForAllExperiments(experimentDAOs);
    }
    CharSequence packageName = accessibilityEvent.getPackageName();
    if (RuntimePermissionsAccessibilityEventHandler.isPackageInstallerEvent(packageName)) {
      //Log.debug("runtime permissions checking accessibility events");
      runtimePermissionsEventHandler.handleRuntimePermissionEvents(accessibilityEvent);
      return;
    } else if (isViewClickEventOfInterest(accessibilityEvent, interestingTriggers)) {
      Log.debug("Accessibility View Click Event is interesting for non-runtime permissions triggers: ");
      inspectEvent(accessibilityEvent);
      triggerBroadcastService(accessibilityEvent);
    } else {
//      inspectEvent(accessibilityEvent);
//      final AccessibilityNodeInfo source = accessibilityEvent.getSource();
//      if (source != null) {
//        final CharSequence contentDescription = source.getContentDescription();
//        Log.info("scd: " + contentDescription);
//      }
    }
  }

  private void triggerBroadcastService(AccessibilityEvent accessibilityEvent) {
    Intent broadcastTriggerServiceIntent = new Intent(this, BroadcastTriggerService.class);
    broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGERED_TIME, DateTime.now().toString(TimeUtil.DATETIME_FORMAT));
    broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGER_EVENT, InterruptCue.ACCESSIBILITY_EVENT_VIEW_CLICKED);
    broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGER_SOURCE_IDENTIFIER, accessibilityEvent.getPackageName());

    Bundle accessibilityPayload = new Bundle();
    final List<CharSequence> textList = accessibilityEvent.getText();
    if (textList != null && !textList.isEmpty()) {
      final CharSequence text = textList.get(0);
      accessibilityPayload.putCharSequence(ACCESSIBILITY_EVENT_TEXT, text);
    }
    if (accessibilityEvent.getContentDescription() != null) {
      accessibilityPayload.putCharSequence(ACCESSIBILITY_EVENT_CONTENT_DESCRIPTION, accessibilityEvent.getContentDescription());
    }
    accessibilityPayload.putInt(ACCESSIBILITY_EVENT_TYPE, accessibilityEvent.getEventType());
    accessibilityPayload.putCharSequence(ACCESSIBILITY_EVENT_PACKAGE, accessibilityEvent.getPackageName());
    accessibilityPayload.putCharSequence(ACCESSIBILITY_EVENT_CLASS, accessibilityEvent.getClassName());

    broadcastTriggerServiceIntent.putExtra(RuntimePermissionsAccessibilityEventHandler.PACO_ACTION_ACCESSIBILITY_PAYLOAD,
                                           accessibilityPayload);
    startService(broadcastTriggerServiceIntent);
  }

  private boolean isViewClickEventOfInterest(AccessibilityEvent accessibilityEvent, List<InterruptTrigger> interestingTriggers) {
    CharSequence packageName = accessibilityEvent.getPackageName();
    CharSequence className = accessibilityEvent.getClassName();
    int eventType = accessibilityEvent.getEventType();
    CharSequence contentDescription = accessibilityEvent.getContentDescription();
    final List<CharSequence> text2 = accessibilityEvent.getText();
    CharSequence text = null;
    if (text2 != null && text2.size() > 0) {
      text = text2.get(0);
    }

    for (InterruptTrigger trigger : interestingTriggers) {
      List<InterruptCue> cues = trigger.getCues();
      for (InterruptCue interruptCue : cues) {
        boolean matches = true;
        if (interruptCue.getCueCode() != InterruptCue.ACCESSIBILITY_EVENT_VIEW_CLICKED) {
           continue;
        }
        if (interruptCue.getCueSource() != null) {
          if (packageName == null || !interruptCue.getCueSource().equals(packageName)) {
            matches = false;
          }
        }
        if (interruptCue.getCueAEContentDescription() != null) {
          if (contentDescription == null || !interruptCue.getCueAEContentDescription().equals(contentDescription)) {
            matches = false;
          }
        }
        if (interruptCue.getCueAEClassName() != null) {
          if (className == null || !interruptCue.getCueAEClassName().equals(className)) {
            matches = false;
          }
        }
        // if (interruptCue.getCueAEEventType() != null) {
        if (eventType != InterruptCue.VIEW_CLICKED) {
          matches = false;
        }
        // }
        if (matches) {
          return true;
        }
      }

    }
    return false;
  }

  private void inspectEvent(AccessibilityEvent accessibilityEvent) {
    Log.debug(eventToString(accessibilityEvent));
  }

  private String getStringForEventType(int eventType) {
    switch (eventType) {
    case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
      return "windowstatechange";
    case AccessibilityEvent.TYPE_ANNOUNCEMENT:
      return "announcement";
    case AccessibilityEvent.TYPE_VIEW_CLICKED:
      return "viewclicked";
    case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
      return "notificationstate";
    default:
      return "unknown or uninteresting accessibility event type: " + eventType;
    }
  }

  private String eventToString(AccessibilityEvent accessibilityEvent) {
    int eventType = accessibilityEvent.getEventType();
    return String.format("EVENT: type: %s, p: %s, c: %s, cd: %s",
                         getStringForEventType(eventType),
                         accessibilityEvent.getPackageName(),
                         accessibilityEvent.getClassName(),
                         accessibilityEvent.getContentDescription());
  }

  /**
   * Returns whether the service is running and connected.
   * @return true if we have accessibility permissions and the service is connected
   */
  public static boolean isRunning() {
    return running;
  }

  /**
   * Called by the Android system when it connects the accessibility service. We use this to keep
   * track of whether we have the accessibility permission.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  protected void onServiceConnected() {
    running = true;
    Log.debug("Connected to the accessibility service");
    initializeRuntimePermissionsMonitoringState();
  }

  private void initializeRuntimePermissionsMonitoringState() {
    runtimePermissionsEventHandler = new RuntimePermissionsAccessibilityEventHandler(getApplicationContext());
  }

  /**
   * Called by the Android system when the accessibility service is stopped (e.g. because the user
   * disables accessibility permissions for the app)
   */
  @Override
  public void onDestroy() {
    Log.debug("Accessibility service destroyed");
    running = false;
    runtimePermissionsEventHandler = null;
  }

  /**
   * Called by the Android system when it wants to interrupt feedback
   */
  @Override
  public void onInterrupt() {
    // Ignore, since we are not actually a screen reader.
  }
}
