package com.pacoapp.paco.net;

import android.content.Context;
import android.util.Log;

import com.google.android.apps.paco.PacoConstants;

public interface NetworkClient {

  Context getContext();

  void show(String msg);

  void showAndFinish(String string);

  void handleException(Exception Exception);

  public static class BackgroundNetworkClient implements NetworkClient {

    private Context context;

    public BackgroundNetworkClient(Context context) {
      this.context = context;
    }
    @Override
    public Context getContext() {
      return context;
    }

    @Override
    public void show(String msg) {
      Log.i(PacoConstants.TAG, msg);

    }

    @Override
    public void showAndFinish(String msg) {
      // no-op
      Log.i(PacoConstants.TAG, msg);

    }

    @Override
    public void handleException(Exception exception) {
      Log.e(PacoConstants.TAG, "Could not do netowrk task", exception);
    }

  }

}
