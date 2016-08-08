package com.google.android.apps.paco.test;


import android.test.AndroidTestCase;

import com.pacoapp.paco.sensors.android.RuntimePermissionMonitorService;
import com.pacoapp.paco.sensors.android.procmon.EncounteredPermissionRequest;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class RuntimePermissionMonitorServiceTest extends AndroidTestCase {
  RuntimePermissionMonitorService runtimePermissionMonitorService;
  Method extractInformationFromEventText;
  Method setCurrentlyHandledAppName;

  @Before
  public void setUp() throws Exception {
    runtimePermissionMonitorService = new RuntimePermissionMonitorService();
    // Initialize variables
    Method init = runtimePermissionMonitorService.getClass().getDeclaredMethod("onServiceConnected");
    init.setAccessible(true);
    init.invoke(runtimePermissionMonitorService);
    // Make necessary methods accessible
    extractInformationFromEventText = runtimePermissionMonitorService.getClass().getDeclaredMethod("extractInformationFromEventText", List.class);
    extractInformationFromEventText.setAccessible(true);
    setCurrentlyHandledAppName = runtimePermissionMonitorService.getClass().getDeclaredMethod("setCurrentlyHandledAppName", CharSequence.class);
    setCurrentlyHandledAppName.setAccessible(true);
  }

  @Test
  public void testEventTextExtraction() throws Exception {
    List<CharSequence> input = new ArrayList();
    input.add("Allow Bramapp to access this device's location?");
    extractInformationFromEventText.invoke(runtimePermissionMonitorService, input);
    EncounteredPermissionRequest lastRequest = runtimePermissionMonitorService.getLastEncounteredPermissionRequest();
    assertEquals(lastRequest.getAppName(), "Bramapp");
    assertEquals(lastRequest.getPermissionString(), "Location");
  }

  @Test
  public void testEventTextExtraction2() throws Exception {
    List<CharSequence> input = new ArrayList();
    input.add("Allow Bramapp to take pictures and record video?");
    extractInformationFromEventText.invoke(runtimePermissionMonitorService, input);
    EncounteredPermissionRequest lastRequest = runtimePermissionMonitorService.getLastEncounteredPermissionRequest();
    assertEquals(lastRequest.getAppName(), "Bramapp");
    assertEquals(lastRequest.getPermissionString(), "Camera");
  }

  @Test
  public void testEventTextExtractionUnknownPermission() throws Exception {
    List<CharSequence> input = new ArrayList();
    input.add("Allow Bramapp2 to access an as of yet unknown thing?");
    extractInformationFromEventText.invoke(runtimePermissionMonitorService, input);
    EncounteredPermissionRequest lastRequest = runtimePermissionMonitorService.getLastEncounteredPermissionRequest();
    assertEquals(lastRequest.getAppName(), "Bramapp2");
    assertEquals(lastRequest.getPermissionString(), "access an as of yet unknown thing");
  }

  @Test
  public void testRunning() throws Exception {
    assert(runtimePermissionMonitorService.isRunning());
    runtimePermissionMonitorService.stopSelf();
    assert(!runtimePermissionMonitorService.isRunning());
  }
}
