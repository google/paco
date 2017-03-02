package com.pacoapp.paco.sensors.android.diagnostics;

import android.content.Context;

import com.pacoapp.paco.R;
import com.pacoapp.paco.sensors.android.AccessibilityEventMonitorService;

/**
 * This class is used by the DiagnosticsReporter to add to the report whether Paco has accessibility
 * access.
 */
public class AccessibilityDiagnostic extends Diagnostic<String> {
  public AccessibilityDiagnostic(Context context) {
    super(context.getString(R.string.diagnostic_accessibility_type));
  }

  @Override
  public void run(Context context) {
    setValue(context.getString(R.string.diagnostic_accessibility_type) + ": " + AccessibilityEventMonitorService.isRunning());
  }
}
