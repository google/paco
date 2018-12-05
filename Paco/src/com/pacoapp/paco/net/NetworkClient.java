package com.pacoapp.paco.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;

public interface NetworkClient {

  Context getContext();

  void show(String msg);

  void showAndFinish(String string);

  void handleException(Exception Exception);

  public static class BackgroundNetworkClient implements NetworkClient {
    private static Logger Log = LoggerFactory.getLogger(BackgroundNetworkClient.class);
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
      Log.info(msg);

    }

    @Override
    public void showAndFinish(String msg) {
      // no-op
      Log.info(msg);

    }

    @Override
    public void handleException(Exception exception) {
      Log.error("Could not do netowrk task", exception);
    }

  }

}
