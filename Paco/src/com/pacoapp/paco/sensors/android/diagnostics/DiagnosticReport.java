package com.pacoapp.paco.sensors.android.diagnostics;

import java.util.List;

import org.joda.time.DateTime;

import android.content.Context;

import com.google.common.collect.Lists;
import com.pacoapp.paco.R;

public class DiagnosticReport {

  private List<Diagnostic> diagnostics;
  private DateTime generationTime;
  private Context context;
  
  public DiagnosticReport() {
    this.diagnostics = Lists.newArrayList();
    this.generationTime = new DateTime();
  }

  public void add(Diagnostic createClockDiagnostic) {
    this.diagnostics.add(createClockDiagnostic);    
  }

  public void run(Context context) {
    this.context = context;
    for (Diagnostic diagnostic : diagnostics) {
      diagnostic.run(context);
    }
  }
  
  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer();
    String generatedLabel = "Repor generated";
    if (context != null) {
      generatedLabel = context.getString(R.string.diagnostics_report_generated_label);
    }
    buf.append(generatedLabel + ": " + generationTime.toString());
    buf.append("\n-------------------\n");
    
    for (Diagnostic d : diagnostics) {
      buf.append(d.toString());
      buf.append("\n");
    }
    return buf.toString();
  }

}
