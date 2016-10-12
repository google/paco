package com.google.android.apps.paco.test;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.pacoapp.paco.sensors.android.RuntimePermissionsAccessibilityEventHandler;
import com.pacoapp.paco.sensors.android.procmon.EncounteredPermissionRequest;

import android.annotation.TargetApi;
import android.os.Build;
import android.test.AndroidTestCase;

public class RuntimePermissionMonitorServiceTest extends AndroidTestCase {
  RuntimePermissionsAccessibilityEventHandler runtimePermissionMonitorService;
  Method extractInformationFromEventText;
  Method setCurrentlyHandledAppName;
  Field previouslyEncounteredPermissionRequests;

  @Before
  public void setUp() throws Exception {
    runtimePermissionMonitorService = new RuntimePermissionsAccessibilityEventHandler(null);
    // Initialize variables
    // Make necessary methods and fields accessible
    extractInformationFromEventText = runtimePermissionMonitorService.getClass().getDeclaredMethod("extractInformationFromEventText", List.class);
    extractInformationFromEventText.setAccessible(true);
    setCurrentlyHandledAppName = runtimePermissionMonitorService.getClass().getDeclaredMethod("setCurrentlyHandledAppName", CharSequence.class);
    setCurrentlyHandledAppName.setAccessible(true);
    previouslyEncounteredPermissionRequests = runtimePermissionMonitorService.getClass().getDeclaredField("previouslyEncounteredPermissionRequests");
    previouslyEncounteredPermissionRequests.setAccessible(true);
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

  @TargetApi(Build.VERSION_CODES.GINGERBREAD)
  @Test
  public void testDuplicateAdd() throws Exception {
    List<CharSequence> input1 = new ArrayList();
    input1.add("Allow Bramapp to access this device's location?");
    List<CharSequence> input2 = new ArrayList();
    input2.add("Allow Bramapp to take pictures and record video?");
    extractInformationFromEventText.invoke(runtimePermissionMonitorService, input1);
    extractInformationFromEventText.invoke(runtimePermissionMonitorService, input2);
    extractInformationFromEventText.invoke(runtimePermissionMonitorService, input2);

    Deque<EncounteredPermissionRequest> encounteredPermissionRequests = (Deque<EncounteredPermissionRequest>) previouslyEncounteredPermissionRequests.get(runtimePermissionMonitorService);

    // Make sure the last add was ignored
    assertEquals(encounteredPermissionRequests.size(), 2);
    assertThat(encounteredPermissionRequests.getFirst().getPermissionString(), not(equalTo(encounteredPermissionRequests.getLast().getPermissionString())));
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

}
