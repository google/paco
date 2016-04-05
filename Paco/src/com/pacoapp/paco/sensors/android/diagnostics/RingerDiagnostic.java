package com.pacoapp.paco.sensors.android.diagnostics;

import java.util.List;
import com.google.common.collect.Lists;
import com.pacoapp.paco.R;
import android.content.Context;
import android.media.AudioManager;

public class RingerDiagnostic extends ListDiagnostic {

  public RingerDiagnostic(Context context) {
    super(context.getString(R.string.diagnostic_ringer_type));
  }

  @Override
  public void run(Context context) {
    List<String> results = Lists.newArrayList();
    
    AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);    
    
    int ringerMode = am.getRingerMode();
    switch (ringerMode) {
    case AudioManager.RINGER_MODE_NORMAL:
      results.add(context.getString(R.string.diagnostic_ringer_ringer_mode_label) + ": " + context.getString(R.string.diagnostic_ringer_normal_label));
      break;
    case AudioManager.RINGER_MODE_SILENT:
      results.add(context.getString(R.string.diagnostic_ringer_ringer_mode_label) + ": " + context.getString(R.string.diagnostic_ringer_silent_label));
      break;
    case AudioManager.RINGER_MODE_VIBRATE:
      results.add(context.getString(R.string.diagnostic_ringer_ringer_mode_label) + ": " + context.getString(R.string.diagnostic_ringer_vibrate_label));
      break;
    }
    
    int notificationVolume = am.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
    int notificationVolumeMax = am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
    results.add(context.getString(R.string.diagnostic_ringer_notification_volume_label) + ": " + notificationVolume);
    results.add(context.getString(R.string.diagnostic_ringer_notification_volume_max_label) + ": " + notificationVolumeMax);

    setValue(results);
  }

}
