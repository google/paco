package com.pacoapp.paco.sensors.android.diagnostics;

import com.pacoapp.paco.R;
import com.pacoapp.paco.UserPreferences;
import android.content.Context;

public class AccountDiagnostic extends Diagnostic<String> {

  public AccountDiagnostic(Context context) {
    super(context.getString(R.string.diagnostic_account_type));
  }

  @Override
  public void run(Context context) {
    UserPreferences userPrefs = new UserPreferences(context);
    String acct = userPrefs.getSelectedAccount();
    String accountLabel = context.getString(R.string.diagnostics_account_label);
    if (acct == null) {     
      acct = accountLabel + ": null";
    } 
    setValue(accountLabel + ": " + acct);
  }


}
