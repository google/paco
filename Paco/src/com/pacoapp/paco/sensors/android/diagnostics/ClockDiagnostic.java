package com.pacoapp.paco.sensors.android.diagnostics;

import org.joda.time.DateTime;
import android.content.Context;
import com.pacoapp.paco.R;

public class ClockDiagnostic extends Diagnostic<String> {

  public ClockDiagnostic(Context context) {
    super(context.getString(R.string.diagnostic_clock_type));
  }

  @Override
  public void run(Context context) {
    setValue(context.getString(R.string.diagnostics_localtime_label) + ": " + new DateTime().toString());    
  }

 
}
