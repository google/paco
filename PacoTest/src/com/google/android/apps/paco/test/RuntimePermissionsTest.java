package com.google.android.apps.paco.test;


import android.content.Context;
import android.content.Intent;
import android.test.AndroidTestCase;
import android.view.accessibility.AccessibilityNodeInfo;

import com.pacoapp.paco.sensors.android.RuntimePermissions;
import com.pacoapp.paco.sensors.android.procmon.EncounteredPermissionRequest;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class RuntimePermissionsTest extends AndroidTestCase {
  RuntimePermissions runtimePermissions;
  Method extractInformationFromEventText;
  Method setCurrentlyHandledAppName;

  @Before
  public void setUp() throws Exception {
    runtimePermissions = new RuntimePermissions();
    // Initialize variables
    Method init = runtimePermissions.getClass().getDeclaredMethod("onServiceConnected");
    init.setAccessible(true);
    init.invoke(runtimePermissions);
    // Make necessary methods accessible
    extractInformationFromEventText = runtimePermissions.getClass().getDeclaredMethod("extractInformationFromEventText", List.class);
    extractInformationFromEventText.setAccessible(true);
    setCurrentlyHandledAppName = runtimePermissions.getClass().getDeclaredMethod("setCurrentlyHandledAppName", CharSequence.class);
    setCurrentlyHandledAppName.setAccessible(true);
  }

  @Test
  public void testEventTextExtraction() throws Exception {
    List<CharSequence> input = new ArrayList();
    input.add("Allow Bramapp to access this device's location?");
    extractInformationFromEventText.invoke(runtimePermissions, input);
    EncounteredPermissionRequest lastRequest = runtimePermissions.getLastEncounteredPermissionRequest();
    assertEquals(lastRequest.getAppName(), "Bramapp");
    assertEquals(lastRequest.getPermissionString(), "Location");
  }

  @Test
  public void testEventTextExtraction2() throws Exception {
    List<CharSequence> input = new ArrayList();
    input.add("Allow Bramapp to take pictures and record video?");
    extractInformationFromEventText.invoke(runtimePermissions, input);
    EncounteredPermissionRequest lastRequest = runtimePermissions.getLastEncounteredPermissionRequest();
    assertEquals(lastRequest.getAppName(), "Bramapp");
    assertEquals(lastRequest.getPermissionString(), "Camera");
  }

  @Test
  public void testEventTextExtractionUnknownPermission() throws Exception {
    List<CharSequence> input = new ArrayList();
    input.add("Allow Bramapp2 to access an as of yet unknown thing?");
    extractInformationFromEventText.invoke(runtimePermissions, input);
    EncounteredPermissionRequest lastRequest = runtimePermissions.getLastEncounteredPermissionRequest();
    assertEquals(lastRequest.getAppName(), "Bramapp2");
    assertEquals(lastRequest.getPermissionString(), "access an as of yet unknown thing");
  }

  @Test
  public void testRunning() throws Exception {
    assert(runtimePermissions.isRunning());
    runtimePermissions.stopSelf();
    assert(!runtimePermissions.isRunning());
  }
}
