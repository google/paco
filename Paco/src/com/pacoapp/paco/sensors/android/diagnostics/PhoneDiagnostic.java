package com.pacoapp.paco.sensors.android.diagnostics;

import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.view.Display;
import com.google.common.collect.Lists;
import com.pacoapp.paco.R;

public class PhoneDiagnostic extends ListDiagnostic {

  public PhoneDiagnostic(Context context) {
    super(context.getString(R.string.diagnostic_phone_type));    
  }

  @Override
  public void run(Context context) {
    List<String> values = Lists.newArrayList();
    if (context instanceof Activity) {
      Display defaultDisplay = ((Activity)context).getWindowManager().getDefaultDisplay();
      String size = Integer.toString(defaultDisplay.getHeight()) + "x" + Integer.toString(defaultDisplay.getWidth());
      values.add(context.getString(R.string.diagnostic_phone_display_label) + ": " + size);
    } else {
      values.add(context.getString(R.string.diagnostic_phone_display_label) + ": " + context.getString(R.string.diagnostic_phone_unavailable_label));
    }

    values.add(context.getString(R.string.diagnostic_phone_make_label) + ": " + Build.MANUFACTURER);
    values.add(context.getString(R.string.diagnostic_phone_model_label) + ": " + Build.MODEL);
    values.add(context.getString(R.string.diagnostic_phone_android_label) + ": " + Build.VERSION.RELEASE);
    values.add("language" + ": " + Locale.getDefault().getISO3Language());
    TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    String carrierName = manager.getNetworkOperatorName();
    values.add(context.getString(R.string.diagnostic_phone_carrier_label) + ": " + carrierName);
    setValue(values);
  }

}
