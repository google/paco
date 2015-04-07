package com.pacoapp.paco.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.OperationCanceledException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.apps.paco.AccountChooser;
import com.google.android.apps.paco.PacoConstants;
import com.google.android.apps.paco.UserPreferences;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.pacoapp.paco.R;
import com.pacoapp.paco.net.AbstractAuthTokenTask;
import com.pacoapp.paco.net.GetAuthTokenInForeground;
import com.pacoapp.paco.net.NetworkClient;

public class SplashActivity extends Activity implements NetworkClient {

  public static final String EXTRA_ACCOUNTNAME = "extra_accountname";
  public static final String EXTRA_CHANGING_EXISTING_ACCOUNT = "extra_changing_existing_account";

  public static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
  public static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;
  public static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;

  protected static final int ACCOUNT_CHOOSER_REQUEST_CODE = 55;


  private UserPreferences userPrefs;
  private boolean changingExistingAccount;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.splash_screen);
    changingExistingAccount = getIntent().getBooleanExtra(EXTRA_CHANGING_EXISTING_ACCOUNT, false);

    userPrefs = new UserPreferences(getApplicationContext());

    Button loginButton = (Button)findViewById(R.id.loginButton);
    loginButton.setOnClickListener(new View.OnClickListener() {

      @SuppressLint("NewApi")
      @Override
      public void onClick(View v) {
        authenticateUser();
      }
    });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
          if (resultCode == RESULT_OK) {
              userPrefs.saveSelectedAccount(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
              changingExistingAccount = false; // unset so that we don't loop in the picker forever
              authenticateUser();
          } else if (resultCode == RESULT_CANCELED) {
              Toast.makeText(this, "You must pick an account", Toast.LENGTH_SHORT).show();
          }
      } else if ((requestCode == REQUEST_CODE_RECOVER_FROM_AUTH_ERROR ||
              requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR)
              && resultCode == RESULT_OK) {
          handleAuthorizeResult(resultCode, data);
          return;
      }
      super.onActivityResult(requestCode, resultCode, data);
  }

  private void handleAuthorizeResult(int resultCode, Intent data) {
    if (data == null) {
        show("Unknown error, click the button again");
        return;
    }
    if (resultCode == RESULT_OK) {
        Log.i(PacoConstants.TAG, "Retrying");
        getTask(this).execute();
        return;
    }
    if (resultCode == RESULT_CANCELED) {
        show("User rejected authorization.");
        return;
    }
    show("Unknown error, click the button again");
}
  protected void oldonActivityResult(int requestCode, int resultCode, Intent data) {
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
      Toast.makeText(this, "You must pick an account", Toast.LENGTH_SHORT).show();
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

    String authTokenType = AbstractAuthTokenTask.AUTH_TOKEN_TYPE_USERINFO_EMAIL;

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
              SplashActivity.this.finish();
//              finish();

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

  @Override
  protected void onResume() {
    super.onResume();
    if (changingExistingAccount) {
      authenticateUser();
    }
  }

  public void authenticateUser() {
    if (userPrefs.getSelectedAccount() == null || changingExistingAccount) {
      pickUserAccount();
    } else {
      if (isDeviceOnline()) {
        getTask(this).execute();
      } else {
        Toast.makeText(this, "No network connection available", Toast.LENGTH_SHORT).show();
      }
    }
  }

  private AbstractAuthTokenTask getTask(SplashActivity activity) {
    return new GetAuthTokenInForeground(activity);
  }


  @SuppressLint("NewApi")
  public void pickUserAccount() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      Account account = null;
      if (userPrefs.getSelectedAccount() != null) {
        account = getAccountFor(userPrefs.getSelectedAccount());
      }
      Intent intent = AccountManager.newChooseAccountIntent(account, null,
                                                            new String[]{"com.google"},
                                                            changingExistingAccount,
                                                            null,
                                                            AbstractAuthTokenTask.AUTH_TOKEN_TYPE_USERINFO_EMAIL,
                                                            null, null);
      startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    } else {
      Intent intent = new Intent(SplashActivity.this, AccountChooser.class);
      startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }
  }

  private Account getAccountFor(String selectedAccount) {
    AccountManager am = AccountManager.get(this);
    Account[] accounts = am.getAccountsByType("com.google");
    for (Account account : accounts) {
      if (account.name.equals(selectedAccount)) {
        return account;
      }
    }
    return null;
  }

  /** Checks whether the device currently has a network connection */
  private boolean isDeviceOnline() {
    ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    if (networkInfo != null && networkInfo.isConnected()) {
      return true;
    }
    return false;
  }

  public void show(final String message) {
    runOnUiThread(new Runnable() {
        @Override
        public void run() {
            Toast.makeText(SplashActivity.this, message, Toast.LENGTH_LONG);
        }
    });
}

  @Override
  public void handleException(final Exception e) {
    runOnUiThread(new Runnable() {
        @Override
        public void run() {
            if (e instanceof GooglePlayServicesAvailabilityException) {
                // The Google Play services APK is old, disabled, or not present.
                // Show a dialog created by Google Play services that allows
                // the user to update the APK
                int statusCode = ((GooglePlayServicesAvailabilityException)e)
                        .getConnectionStatusCode();
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
                        SplashActivity.this,
                        REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                dialog.show();
            } else if (e instanceof UserRecoverableAuthException) {
                // Unable to authenticate, such as when the user has not yet granted
                // the app access to the account, but the user can fix this.
                // Forward the user to an activity in Google Play services.
                Intent intent = ((UserRecoverableAuthException)e).getIntent();
                startActivityForResult(intent,
                        REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
            }
        }
    });
}

  public void showAndFinish(String string) {
    show(string);
    finish();

  }

  @Override
  public Context getContext() {
    return this.getApplicationContext();
  }

}
