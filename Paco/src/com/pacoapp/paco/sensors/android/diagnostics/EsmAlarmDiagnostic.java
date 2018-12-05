package com.pacoapp.paco.sensors.android.diagnostics;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import android.content.Context;
import android.database.Cursor;

import com.pacoapp.paco.R;
import com.pacoapp.paco.model.EsmSignalColumns;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.shared.util.TimeUtil;
import com.pacoapp.paco.triggering.AndroidEsmSignalStore;

public class EsmAlarmDiagnostic extends ListDiagnostic {

  public EsmAlarmDiagnostic(Context context) {
    super(context.getString(R.string.diagnostic_esm_alarms_type));
  }

  @Override
  public void run(Context context) {
    ExperimentProviderUtil experimentProviderUtil = new ExperimentProviderUtil(context);

    AndroidEsmSignalStore ep = new AndroidEsmSignalStore(context);
    List<String> nameAndTime = new ArrayList<String>();

    Cursor cursor = null;
    try {
      cursor = ep.getAllSignalsForCurrentPeriod();

      final int timeColumnIndex = cursor.getColumnIndex(EsmSignalColumns.TIME);
      final int experimentIdColumnIndex = cursor.getColumnIndex(EsmSignalColumns.EXPERIMENT_ID);
      final int experimentGroupColumnIndex = cursor.getColumnIndex(EsmSignalColumns.GROUP_NAME);

      while (cursor.moveToNext()) {
        DateTime dateTime = new DateTime(cursor.getLong(timeColumnIndex));
        //if (dateTime.isAfterNow()) {
          Long experimentId = cursor.getLong(experimentIdColumnIndex);
          String experimentGroupName = cursor.getString(experimentGroupColumnIndex);
          final Experiment experimentByServerId = experimentProviderUtil.getExperimentByServerId(experimentId);
          if (experimentByServerId == null) {
            continue;
          }
          String experimentName = experimentByServerId.getExperimentDAO().getTitle();
          String signalTime = TimeUtil.formatDateTimeShortNoZone(dateTime);
          nameAndTime.add(signalTime + "  " + experimentName + "/" + experimentGroupName );
        //}
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    setValue(nameAndTime);
  }

}
