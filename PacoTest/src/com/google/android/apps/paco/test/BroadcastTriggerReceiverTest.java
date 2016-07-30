package com.google.android.apps.paco.test;

import java.lang.reflect.Method;

import org.junit.Test;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.test.mock.MockContext;

import com.pacoapp.paco.sensors.android.BroadcastTriggerReceiver;

/**
 * TODO: this test class needs to be extended to cover other parts of the
 * BroadcastTriggerReceiver, notably the onReceive() call. This would however
 * require some refactoring of that class, as e.g. the PowerManager is not
 * mockable in its current state.
 */
public class BroadcastTriggerReceiverTest extends AndroidTestCase {
  class FakeContext extends MockContext {
    private Intent startedService;

    @Override
    public String getPackageName() {
      return "com.pacoapp.paco";
    }
    @Override
    public ComponentName startService(Intent intent) {
      startedService = intent;
      return null;
    }
    public Intent getStartedService() {
      return startedService;
    }
  }

  @Test
  public void testAppInstall() throws Exception {
    Context context = new FakeContext();
    Intent installIntent = new Intent(Intent.ACTION_PACKAGE_ADDED);
    Uri.Builder uriBuilder = new Uri.Builder();
    installIntent.setData(uriBuilder.build());
    BroadcastTriggerReceiver broadcastTriggerReceiver = new BroadcastTriggerReceiver();
    Method isPackageAdded = broadcastTriggerReceiver.getClass().getDeclaredMethod("isPackageAdded", Context.class, Intent.class);
    isPackageAdded.setAccessible(true);
    assertEquals(isPackageAdded.invoke(broadcastTriggerReceiver, context, installIntent), true);
  }

  @Test
  public void testAppUpdate() throws Exception {
    Context context = new FakeContext();
    Intent updateIntent = new Intent(Intent.ACTION_PACKAGE_ADDED);
    Uri.Builder uriBuilder = new Uri.Builder();
    updateIntent.setData(uriBuilder.build());
    updateIntent.putExtra(Intent.EXTRA_REPLACING, true);
    BroadcastTriggerReceiver broadcastTriggerReceiver = new BroadcastTriggerReceiver();
    // Use reflection to test a private method
    Method isPackageAdded = broadcastTriggerReceiver.getClass().getDeclaredMethod("isPackageAdded", Context.class, Intent.class);
    isPackageAdded.setAccessible(true);
    assertEquals(isPackageAdded.invoke(broadcastTriggerReceiver, context, updateIntent), false);
  }
}
