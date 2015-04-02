package com.pacoapp.paco.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.OperationCanceledException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.apps.paco.AccountChooser;
import com.google.android.apps.paco.PacoConstants;
import com.google.android.apps.paco.UserPreferences;
import com.google.android.apps.paco.utils.PacoService;
import com.pacoapp.paco.R;

public class SplashActivity extends Activity {

  protected static final int ACCOUNT_CHOOSER_REQUEST_CODE = 55;
  private UserPreferences userPrefs;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.splash_screen);

    userPrefs = new UserPreferences(getApplicationContext());

    Button loginButton = (Button)findViewById(R.id.loginButton);
    loginButton.setOnClickListener(new View.OnClickListener() {

      @SuppressLint("NewApi")
      @Override
      public void onClick(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
          Intent intent = AccountManager.newChooseAccountIntent(null, null,
                                                                new String[]{"com.google"},
                                                                false,
                                                                null,
                                                                PacoService.AUTH_TOKEN_TYPE_USERINFO_EMAIL,
                                                                null, null);
          startActivityForResult(intent, ACCOUNT_CHOOSER_REQUEST_CODE);
        } else {
          Intent intent = new Intent(SplashActivity.this, AccountChooser.class);
          startActivityForResult(intent, ACCOUNT_CHOOSER_REQUEST_CODE);
        }
      }
    });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == ACCOUNT_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

      String accountName = null;
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
        accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
      } else {
        accountName = data.getStringExtra(AccountChooser.ACCOUNT_NAME);
      }
      if (accountName != null) {
        userPrefs.saveSelectedAccount(accountName);
        getAuthAccessToken(accountName);
//        String token = GoogleAuthUtil.getToken(this, accountName, PacoService.AUTH_TOKEN_TYPE_USERINFO_EMAIL);
//        finish();
      } else {
        finish(); // TODO handler errors
      }
    } else {
      finish(); // TODO handler errors
    }
  }

  private void getAuthAccessToken(final String accountName) {
    AccountManager accountManager = AccountManager.get(this);
    Account[] accounts = accountManager.getAccountsByType("com.google");
    Account account = null;
    for (Account currentAccount : accounts) {
      if (currentAccount.name.equals(accountName)) {
        account = currentAccount;
        break;
      }
    }

    String accessToken = getAccessToken();
    if (accessToken != null) {
      Log.i(PacoConstants.TAG, "Invalidating previous OAuth2 access token: " + accessToken);
      accountManager.invalidateAuthToken(account.type, accessToken);
      setAccessToken(null);
    }

    String authTokenType = PacoService.AUTH_TOKEN_TYPE_USERINFO_EMAIL;

    Log.i(PacoConstants.TAG, "Get access token for " + accountName + " using authTokenType " + authTokenType);
    accountManager.getAuthToken(account, authTokenType, null, this,
        new AccountManagerCallback<Bundle>() {
          @Override
          public void run(AccountManagerFuture<Bundle> future) {
            try {
              String accessToken = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
              Log.i(PacoConstants.TAG, "Got OAuth2 access token: " + accessToken);
              setAccessToken(accessToken);
//
//              Intent result = new Intent();
//              result.putExtra(AccountChooser.ACCOUNT_NAME, accountName);
//              SplashActivity.this.setResult(0, result);
//              SplashActivity.this.finish();
              finish();

            } catch (OperationCanceledException e) {
              Log.e(PacoConstants.TAG, "The user has denied you access to the API");
            } catch (Exception e) {
              Log.e(PacoConstants.TAG, e.getMessage());
              Log.w("Exception: ", e);
            }
          }
        }, null);
  }

  private void setAccessToken(String token) {
    userPrefs.setAccessToken(token);

  }

  private String getAccessToken() {
    return userPrefs.getAccessToken();
  }


}
