package com.pacoapp.paco.net;


import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableNotifiedException;
import com.pacoapp.paco.ui.SplashActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class GetInBackground extends AbstractAuthTokenTask {

  private static Logger Log = LoggerFactory.getLogger(GetInBackground.class);

  public GetInBackground(NetworkClient client) {
    super(client);
  }

  /**
   * Get a authentication token if one is not available. If the error was
   * recoverable then a notification will automatically be pushed. The callback
   * provided will be fired once the notification is addressed by the user
   * successfully. If the error is not recoverable then it displays the error
   * message on parent networkClient.
   */
  @Override
  protected String fetchToken() throws IOException {
    try {
      return GoogleAuthUtil.getTokenWithNotification(networkClient.getContext(), userPrefs.getSelectedAccount(), oAuthScope,
                                                     null,
                                                     makeCallback(userPrefs.getSelectedAccount()));
    } catch (UserRecoverableNotifiedException userRecoverableException) {
      // Unable to authenticate, but the user can fix this.
      // Because we've used getTokenWithNotification(), a Notification is
      // created automatically so the user can recover from the error
      onError("Could not fetch token.", null);
    } catch (GoogleAuthException fatalException) {
      onError("Unrecoverable error " + fatalException.getMessage(), fatalException);
    }
    return null;
  }

  private Intent makeCallback(String accountName) {
    Intent intent = new Intent();
    intent.setAction("com.pacoapp.paco.net.Callback");
    return intent;
  }

  /**
   * Note: Make sure that the receiver can be called from outside the app. You
   * can do that by adding android:exported="true" in the manifest file.
   */
  public static class CallbackReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent callback) {
      // TODO will this create an infinite loop actually? What happens when the user addresses the notification
      // that this could not authenticate?
      // seems like we would continue the work we were doing - which is dependent on who called this.

      Bundle extras = callback.getExtras();
      Intent intent = new Intent(context, SplashActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.putExtras(extras);
      Log.info("Received broadcast for background task. Resurrecting networkClient");
      context.startActivity(intent);
    }
  }
}
