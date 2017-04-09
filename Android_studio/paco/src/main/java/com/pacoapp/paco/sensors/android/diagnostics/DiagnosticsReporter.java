package com.pacoapp.paco.sensors.android.diagnostics;

import android.content.Context;

public class DiagnosticsReporter {

  private DiagnosticReport report;

  public DiagnosticReport runTests(Context context) {
    report = new DiagnosticReport();

    checkPhoneDateTimeAndTimeZone(report, context);
    checkPhoneDetails(report, context);
    checkRingerVolume(report, context);
    checkPacoVersion(report, context);
    checkAccountUsed(report, context);
    checkNetworkAvailability(report, context);
    //checkPacoReachability(report);
    getJoinedExperiments(report, context);
    getUnsyncedEventCount(report, context);
    checkAppUsageAccess(report, context);
    checkAccessibilityAccess(report, context);
    checkEsmAlarms(report, context);
    report.run(context);
    return report;

  }

  private void checkPacoVersion(DiagnosticReport report2, Context context) {
    report.add(new PacoVersionDiagnostic(context));

  }

  private void checkEsmAlarms(DiagnosticReport report2, Context context) {
    report.add(new EsmAlarmDiagnostic(context));

  }

  private void checkAppUsageAccess(DiagnosticReport report2, Context context) {
    report.add(new AppUsageAccessDiagnostic(context));
  }

  private void checkAccessibilityAccess(DiagnosticReport report, Context context) {
    report.add(new AccessibilityDiagnostic(context));
  }

  private void checkRingerVolume(DiagnosticReport report, Context context) {
    report.add(new RingerDiagnostic(context));
  }

  private void getUnsyncedEventCount(DiagnosticReport report, Context context) {
    report.add(new UnsyncedEventDiagnostic(context));
  }

  private void getJoinedExperiments(DiagnosticReport report, Context context) {
    report.add(new PacoJoinedExperimentDiagnostic(context));

  }

  private void checkPacoReachability(DiagnosticReport report, Context context) {
    report.add(new PacoServerReachabilityDiagnostic(context));
  }

  private void checkNetworkAvailability(DiagnosticReport report, Context context) {
    report.add(new NetworkDiagnostic(context));

  }

  private void checkAccountUsed(DiagnosticReport report, Context context) {
    report.add(new AccountDiagnostic(context));

  }

  private void checkPhoneDetails(DiagnosticReport report, Context context) {
    report.add(new PhoneDiagnostic(context));
  }

  private void checkPhoneDateTimeAndTimeZone(DiagnosticReport report, Context context) {
    ClockDiagnostic clockDiagnostic = new ClockDiagnostic(context);
    report.add(clockDiagnostic);
  }





}
