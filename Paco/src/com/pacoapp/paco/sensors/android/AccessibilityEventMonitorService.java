package com.pacoapp.paco.sensors.android;

import java.util.List;

import org.joda.time.DateTime;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.InterruptCue;
import com.pacoapp.paco.shared.model2.InterruptTrigger;
import com.pacoapp.paco.shared.util.ExperimentHelper;
import com.pacoapp.paco.shared.util.TimeUtil;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.accessibility.AccessibilityEvent;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

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
  private NotificationEventHandler notificationHandler;


  private String getEventType(AccessibilityEvent event) {
    return AccessibilityEvent.eventTypeToString(event.getEventType());
  }

  private String getEventText(AccessibilityEvent event) {
    StringBuilder sb = new StringBuilder();
    for (CharSequence s : event.getText()) {
      sb.append(s);
    }
    return sb.toString();
  }

  /**
   * Called to allow experiments that trigger on accessibility events.
   * {@inheritDoc}
   */
  @Override
  public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
//    String formattedMessage = String.format("ev:[t] %s [c] %s [p] %s [tm] %s [tx] %s [cd]",
//            getEventType(accessibilityEvent), accessibilityEvent.getClassName(), accessibilityEvent.getPackageName(),
//            accessibilityEvent.getEventTime(), getEventText(accessibilityEvent), accessibilityEvent.getContentDescription());
//    Log.info("Paco: " + formattedMessage);

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
    final boolean doesAnyExperimentCareAboutAccessibilityEvents = ExperimentHelper.doesAnyExperimentCareAboutAccessibilityEvents(experimentDAOs);
    List<ExperimentGroup> experimentGroupsLoggingNotificationEvents = ExperimentHelper.getExperimentsLoggingNotificationEvents(experimentDAOs);
    if (!doesAnyExperimentCareAboutAccessibilityEvents && experimentGroupsLoggingNotificationEvents.isEmpty()) {
      //Log.debug("No experiments running that care about accessibility events");
      return;
    } else {
      //Log.debug("1 or more experiments running that care about accessibility events");
      interestingTriggers = ExperimentHelper.getAccessibilityTriggersForAllExperiments(experimentDAOs);
    }
    CharSequence packageName = accessibilityEvent.getPackageName();
    Integer eventCode = null;
    if (RuntimePermissionsAccessibilityEventHandler.isPackageInstallerEvent(packageName)) {
      //Log.debug("runtime permissions checking accessibility events");
      runtimePermissionsEventHandler.handleRuntimePermissionEvents(accessibilityEvent);
      return;
    } else if (isViewClickEventOfInterest(accessibilityEvent, interestingTriggers) ) {
      Log.info("Paco", "Accessibility View Click Event is interesting for non-runtime permissions triggers: ");
      inspectEvent(accessibilityEvent);
      triggerBroadcastService(accessibilityEvent, InterruptCue.ACCESSIBILITY_EVENT_VIEW_CLICKED);
    } else if ((eventCode = notificationHandler.handleAccessibilityEvent(accessibilityEvent)) != null) {
      if (eventCode != null /*isMatchingInterruptEventCode(eventCode, interestingTriggers)*/) {
        triggerBroadcastService(accessibilityEvent, eventCode);
      }
    } else {
//      inspectEvent(accessibilityEvent);
//      final AccessibilityNodeInfo source = accessibilityEvent.getSource();
//      if (source != null) {
//        final CharSequence contentDescription = source.getContentDescription();
//        Log.info("scd: " + contentDescription);
//      }
    }
  }

  private boolean isMatchingInterruptEventCode(Integer eventCode, List<InterruptTrigger> interestingTriggers) {
    for (InterruptTrigger interruptTrigger : interestingTriggers) {
      List<InterruptCue> cues = interruptTrigger.getCues();
      for (InterruptCue interruptCue : cues) {
        if (interruptCue.getCueCode().equals(eventCode)) {
          return true;
        }
      }
    }
    return false;
  }

  private void triggerBroadcastService(AccessibilityEvent accessibilityEvent, int eventCueCode) {
    Intent broadcastTriggerServiceIntent = new Intent(this, BroadcastTriggerService.class);
    broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGERED_TIME, DateTime.now().toString(TimeUtil.DATETIME_FORMAT));
    broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGER_EVENT, eventCueCode);
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
    accessibilityPayload.putString(ACCESSIBILITY_EVENT_TYPE, AccessibilityEvent.eventTypeToString(accessibilityEvent.getEventType()));
    accessibilityPayload.putCharSequence(ACCESSIBILITY_EVENT_PACKAGE, accessibilityEvent.getPackageName());
    accessibilityPayload.putCharSequence(ACCESSIBILITY_EVENT_CLASS, accessibilityEvent.getClassName());

    broadcastTriggerServiceIntent.putExtra(RuntimePermissionsAccessibilityEventHandler.PACO_ACTION_ACCESSIBILITY_PAYLOAD,
                                           accessibilityPayload);
    startService(broadcastTriggerServiceIntent);
  }

  private boolean isViewClickEventOfInterest(AccessibilityEvent accessibilityEvent, List<InterruptTrigger> interestingTriggers) {
    int eventType = accessibilityEvent.getEventType();
    if (eventType != InterruptCue.VIEW_CLICKED) {
      return false;
    }

    CharSequence eventPackage = accessibilityEvent.getPackageName();
    CharSequence eventClass = accessibilityEvent.getClassName();
    CharSequence eventContentDescription = accessibilityEvent.getContentDescription();
    CharSequence eventText = getFirstTextEntryFromEvent(accessibilityEvent);

    for (InterruptTrigger trigger : interestingTriggers) {
      List<InterruptCue> cues = trigger.getCues();
      for (InterruptCue interruptCue : cues) {
        if (eventMatchesCue(eventPackage, eventClass, eventType, eventContentDescription, eventText, interruptCue)) {
          return true;
        }
      }
    }
    return false;
  }

  @Nullable
  private CharSequence getFirstTextEntryFromEvent(AccessibilityEvent accessibilityEvent) {
    final List<CharSequence> textList = accessibilityEvent.getText();
    CharSequence text = null;
    if (textList != null && textList.size() > 0) {
      text = textList.get(0); // only check first text in event.
    }
    return text;
  }

  private boolean eventMatchesCue(CharSequence packageName, CharSequence className, int eventType, CharSequence contentDescription, CharSequence text, InterruptCue interruptCue) {
    // All specified cue parameters must match
    if (interruptCue.getCueCode() != InterruptCue.ACCESSIBILITY_EVENT_VIEW_CLICKED) {
       return false;
    }
    // if (interruptCue.getCueAEEventType() != null) {
    if (eventType != InterruptCue.VIEW_CLICKED) {
      return false;
    }
    if (cueHasParameter(interruptCue.getCueSource()) &&
            !eventValueMatchesCueParameter(packageName, interruptCue.getCueSource())) {
        return false;
    }
    if (cueHasParameter(interruptCue.getCueAEContentDescription()) &&
            !contentDescriptionMatchesEither(contentDescription, text, interruptCue)) {
        return false;
    }
    if (cueHasParameter(interruptCue.getCueAEClassName()) &&
            !eventValueMatchesCueParameter(className, interruptCue.getCueAEClassName())) {
        return false;
    }

    return true;
  }

  private boolean eventValueMatchesCueParameter(CharSequence eventValue, String cueParameter) {
    return eventValue != null && cueParameter.equals(eventValue);
  }

  private boolean cueHasParameter(String parameter) {
    return !Strings.isNullOrEmpty(parameter) && parameter.length() > 0;
  }

  private boolean contentDescriptionMatchesEither(CharSequence aeContentDescription, CharSequence aeText, InterruptCue interruptCue) {
    return eventValueMatchesCueParameter(aeContentDescription, interruptCue.getCueAEContentDescription()) ||
            eventValueMatchesCueParameter(aeText, interruptCue.getCueAEContentDescription());
//    return ((aeContentDescription != null && interruptCue.getCueAEContentDescription().equals(aeContentDescription)) ||
//            (aeText != null && interruptCue.getCueAEContentDescription().equals(aeText)));
  }

  private boolean isNotificationEventOfInterest(AccessibilityEvent accessibilityEvent, List<InterruptTrigger> interestingTriggers) {
    // TODO - unify this with View Click Event after implementation is done.
    CharSequence packageName = accessibilityEvent.getPackageName();
    CharSequence className = accessibilityEvent.getClassName();
    int eventType = accessibilityEvent.getEventType();
    CharSequence contentDescription = accessibilityEvent.getContentDescription();
    CharSequence firstEventTextRecord = getFirstTextEntryFromEvent(accessibilityEvent);

    for (InterruptTrigger trigger : interestingTriggers) {
      List<InterruptCue> cues = trigger.getCues();
      for (InterruptCue interruptCue : cues) {
        boolean matches = true;
        if (!isNotificationCue(interruptCue)) {
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
        if (!isEventTypeMatch(eventType, interruptCue.getCueCode())) {
          matches = false;
        }
        if (matches) {
          return true;
        }
      }

    }
    return false;
  }

  private boolean isEventTypeMatch(int eventType, Integer cueCode) {
    return true;
//    Too many accessibility events involved
//    switch (eventType) {
//    case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
//      return true;
//    case AccessibilityEvent.TYPE_ANNOUNCEMENT:
//      return cueCode == InterruptCue.NOTIFICATION_TRAY_OPENED
//      || cueCode == InterruptCue.NOTIFICATION_TRAY_CANCELLED;
//    default:
//      break;
//    }
//    return false;
  }

  private boolean isNotificationCue(InterruptCue interruptCue) {
    return interruptCue.getCueCode() == InterruptCue.NOTIFICATION_CREATED
            || interruptCue.getCueCode() == InterruptCue.NOTIFICATION_TRAY_CANCELLED
            || interruptCue.getCueCode() == InterruptCue.NOTIFICATION_TRAY_CLEAR_ALL
            || interruptCue.getCueCode() == InterruptCue.NOTIFICATION_TRAY_OPENED
            || interruptCue.getCueCode() == InterruptCue.NOTIFICATION_TRAY_SWIPE_DISMISS
            || interruptCue.getCueCode() == InterruptCue.NOTIFICATION_CLICKED;
  }


  private void inspectEvent(AccessibilityEvent accessibilityEvent) {
    Log.info("Paco", eventToString(accessibilityEvent));
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
    Log.info("Paco", "Connected to the accessibility service");
    initializeRuntimePermissionsMonitoringState();
    notificationHandler = new NotificationEventHandler(getApplicationContext());
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
    Log.info("Paco", "Accessibility service destroyed");
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
