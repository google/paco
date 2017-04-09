package com.pacoapp.paco.sensors.android.diagnostics;

import java.util.List;
import com.google.common.collect.Lists;
import com.pacoapp.paco.R;
import com.pacoapp.paco.model.Event;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import android.content.Context;

public class UnsyncedEventDiagnostic extends ListDiagnostic {

  public UnsyncedEventDiagnostic(Context context) {
    super(context.getString(R.string.diagnostic_unsynced_events_type));
  }

  @Override
  public void run(Context context) {
    List<String> results = Lists.newArrayList();
    
    ExperimentProviderUtil ep = new ExperimentProviderUtil(context);
    List<Event> unUploaded = ep.getEventsNeedingUpload();
    if (unUploaded == null) {
      results.add(context.getString(R.string.diagnostic_events_unsynced_label) + ": 0");
    } else {
      results.add(context.getString(R.string.diagnostic_events_unsynced_label) + ": " + unUploaded.size());      
    }
    setValue(results);
  }
  
}
