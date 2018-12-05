package com.pacoapp.paco.sensors.android;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.pacoapp.paco.sensors.android.procmon.AppChangeListener;
import com.pacoapp.paco.sensors.android.procmon.AppUsageEvent;
import com.pacoapp.paco.sensors.android.procmon.AppUsageEventsService;
import com.pacoapp.paco.sensors.android.procmon.LollipopAppUseChangeDetector;

public class LollipopAppUsageMonitorTest {

  class FakeUsageEventsService extends AppUsageEventsService {

    private List<List<AppUsageEvent>> sequences;
    private int currentSequence = 0;

    public FakeUsageEventsService(List<List<AppUsageEvent>> eventSequences) {
      super();
      this.sequences = eventSequences;
    }


    @Override
    public List<AppUsageEvent> getUsageEvents() {
      List<AppUsageEvent> seq = sequences.get(currentSequence);
      currentSequence++;
      return seq;
    }

  }

//  @Test
//  public void testAppClosed() {
//    // SETUP
//    final List<AppUsageEvent> openSequence = Lists.newArrayList();
//    addOpenChromeSequence(AppUsageEvent.ANDROID_LOLLIPOP_HOME_APP_PKG,
//                          AppUsageEvent.ANDROID_LOLLIPOP_HOME_APP_CLASS,
//                          openSequence);
//
//    final List<AppUsageEvent> closeSequence = Lists.newArrayList();
//    addChromeCloseSequence(closeSequence);
//
//    final List<List<AppUsageEvent>> sequenceOfSequences = Lists.newArrayList();
//    sequenceOfSequences.add(openSequence);
//    sequenceOfSequences.add(closeSequence);
//
//    // Class under test config
//    List<String> tasksOfInterestForOpening = Lists.newArrayList();
//    List<String> tasksOfInterestForClosing = Lists.newArrayList("com.android.chrome");
//    FakeUsageEventsService usageEventsService = new FakeUsageEventsService(sequenceOfSequences);
//
//    AppUsageEventLogger appUsageLogger = new AppUsageEventLogger(null, Lists.<Experiment>newArrayList(), null) {
//
//      @Override
//      public void logProcessesUsedSinceLastPolling(List<AppUsageEvent> newlyUsedApps) {
//      }
//
//    };
//
//    // results prep
//    final Map<String, Boolean> results = Maps.newHashMap();
//    results.put("firedClosedTrigger", false);
//
//
//    LollipopAppUsageMonitor monitor = new LollipopAppUsageMonitor(tasksOfInterestForOpening,
//                                                                            tasksOfInterestForClosing,
//                                                                            null,
//                                                                            usageEventsService,
//                                                                            appUsageLogger) {
//
//      @Override
//      protected void triggerCodeForAppTrigger(String appIdentifier,
//                                              int triggerCode) {
//        results.put("firedClosedTrigger",
//                    (triggerCode == InterruptCue.APP_CLOSED &&
//                     appIdentifier.equals(AppUsageEvent.ANDROID_LOLLIPOP_HOME_APP_PKG)));
//      }
//
//    };
//    // test sequence 1 (opening chrome);
//    monitor.detectTriggersFromUsageEvents();
//    assertFalse(results.get("firedClosedTrigger"));
//
//    // test sequence 2 (closing chrome);
//    monitor.detectTriggersFromUsageEvents();
//    assertTrue(results.get("firedClosedTrigger"));
//  }

  @Test
  public void testAppClosedNewStrategy() throws Exception {
    final List<AppUsageEvent> openSequence = Lists.newArrayList();
    addOpenChromeSequence(AppUsageEvent.ANDROID_LOLLIPOP_HOME_APP_PKG,
                          AppUsageEvent.ANDROID_LOLLIPOP_HOME_APP_CLASS,
                          openSequence);

    final List<AppUsageEvent> closeSequence = Lists.newArrayList();
    addChromeCloseSequence(closeSequence);

    // Class under test config
    List<String> tasksOfInterestForClosing = Lists.newArrayList(AppUsageEvent.COM_ANDROID_CHROME_PKG_NAME+ "/" + AppUsageEvent.CHROME_APP_DOCUMENT_ACTIVITY_CLASS_NAME);

    final List<AppUsageEvent> openedApps = Lists.newArrayList();
    final List<AppUsageEvent> closedApps = Lists.newArrayList();
    AppChangeListener listener = new AppChangeListener() {
      public void appOpened(AppUsageEvent event, boolean shouldTrigger) {
        openedApps.add(event);
      }
      public void appClosed(AppUsageEvent event, boolean shouldTrigger) {
        closedApps.add(event);
      }
    };

    LollipopAppUseChangeDetector changeDetector = new LollipopAppUseChangeDetector(null, tasksOfInterestForClosing, listener);
    changeDetector.newEvents(closeSequence);
    assertEquals(1, closedApps.size());
    assertEquals(AppUsageEvent.COM_ANDROID_CHROME_PKG_NAME, closedApps.get(0).getPkgName());
  }

  @Test
  public void testAppClosedWrongAppNewStrategy() throws Exception {
    final List<AppUsageEvent> closeSequence = Lists.newArrayList();
    addDialerClosedSequence(closeSequence);

    // Class under test config
    List<String> tasksOfInterestForClosing = Lists.newArrayList(AppUsageEvent.COM_ANDROID_CHROME_PKG_NAME+ "/" + AppUsageEvent.CHROME_APP_DOCUMENT_ACTIVITY_CLASS_NAME);

    final List<AppUsageEvent> openedApps = Lists.newArrayList();
    final List<AppUsageEvent> closedApps = Lists.newArrayList();
    AppChangeListener listener = new AppChangeListener() {
      public void appOpened(AppUsageEvent event, boolean shouldTrigger) {
        openedApps.add(event);
      }
      public void appClosed(AppUsageEvent event, boolean shouldTrigger) {
        closedApps.add(event);
      }
    };
    LollipopAppUseChangeDetector changeDetector = new LollipopAppUseChangeDetector(null, tasksOfInterestForClosing, listener);
    changeDetector.newEvents(closeSequence);
    assertEquals(0, closedApps.size());
  }

  /**
   * This tests when we might get the same event due to polling frequencies that return the
   * same events twice.
   *
   * We know that the timestamp is the same on the close event so we can ignore it.
   *
   * @throws Exception
   */
  @Test
  public void testAppClosedTwiceSameTimestampNewStrategy() throws Exception {
    final List<AppUsageEvent> closeSequence = Lists.newArrayList();
    addChromeCloseSequence(closeSequence);

    // Class under test config
    List<String> tasksOfInterestForClosing = Lists.newArrayList(AppUsageEvent.COM_ANDROID_CHROME_PKG_NAME + "/" + AppUsageEvent.CHROME_APP_DOCUMENT_ACTIVITY_CLASS_NAME);

    final List<AppUsageEvent> openedApps = Lists.newArrayList();
    final List<AppUsageEvent> closedApps = Lists.newArrayList();
    AppChangeListener listener = new AppChangeListener() {
      public void appOpened(AppUsageEvent event, boolean shouldTrigger) {
        openedApps.add(event);
      }
      public void appClosed(AppUsageEvent event, boolean shouldTrigger) {
        closedApps.add(event);
      }
    };

    LollipopAppUseChangeDetector changeDetector = new LollipopAppUseChangeDetector(null, tasksOfInterestForClosing, listener);
    changeDetector.newEvents(closeSequence);
    assertEquals(1, closedApps.size());
    assertEquals(AppUsageEvent.COM_ANDROID_CHROME_PKG_NAME, closedApps.get(0).getPkgName());

    closedApps.clear();
    changeDetector.newEvents(closeSequence);
    assertEquals(0, closedApps.size());
  }

  @Test
  public void testAppClosedTwiceNewTimestampNewStrategy() throws Exception {
    final List<AppUsageEvent> closeSequence = Lists.newArrayList();
    addSession_closeChrome(closeSequence);

    // Class under test config
    List<String> tasksOfInterestForClosing = Lists.newArrayList(AppUsageEvent.COM_ANDROID_CHROME_PKG_NAME+ "/" + AppUsageEvent.CHROME_APP_DOCUMENT_ACTIVITY_CLASS_NAME);

    final List<AppUsageEvent> openedApps = Lists.newArrayList();
    final List<AppUsageEvent> closedApps = Lists.newArrayList();
    AppChangeListener listener = new AppChangeListener() {
      public void appOpened(AppUsageEvent event, boolean shouldTrigger) {
        openedApps.add(event);
      }
      public void appClosed(AppUsageEvent event, boolean shouldTrigger) {
        closedApps.add(event);
      }
    };

    LollipopAppUseChangeDetector changeDetector = new LollipopAppUseChangeDetector(null, tasksOfInterestForClosing, listener);
    changeDetector.newEvents(closeSequence);
    assertEquals(1, closedApps.size());
    assertEquals(AppUsageEvent.COM_ANDROID_CHROME_PKG_NAME, closedApps.get(0).getPkgName());

    closedApps.clear();
    closeSequence.clear();
    addSession_closeChromeAgain(closeSequence);
    changeDetector.newEvents(closeSequence);
    assertEquals(1, closedApps.size());
  }

//
//  @Test
//  public void testAppOpened() {
//    fail("not implemented yet");
//  }
//
//  @Test
//  public void testLogProcesses() {
//    fail("not implemented yet");
//  }
//
//
//  @Test
//  public void testStartBrowserLog() {
//    fail("not implemented yet");
//  }
//
//  @Test
//  public void testStopBrowserLog() {
//    fail("not implemented yet");
//  }
//
//// more speculative, maybe they belong on a different test class testing a different part of the monitoring service
//
//  @Test
//  public void testPollingInterval() {
//    fail("not implemented yet");
//  }
//
//  @Test
//  public void testFireAppOpenedTrigger() {
//    fail("not implemented yet");
//  }
//
//  @Test
//  public void testFireAppClosedTrigger() {
//    fail("not implemented yet");
//  }
  // end speculative


  public void addOpenChromeSequence(final String previousAppPkgName, final String previousAppClassName,
                                    List<AppUsageEvent> eventSequence) {
    eventSequence.add(new AppUsageEvent(previousAppPkgName, previousAppClassName, 2, 1437157396182l));
    eventSequence.add(new AppUsageEvent(AppUsageEvent.COM_ANDROID_CHROME_PKG_NAME, AppUsageEvent.CHROME_APP_MAIN_CLASS_NAME,1,1437157396205l));
    eventSequence.add(new AppUsageEvent(AppUsageEvent.COM_ANDROID_CHROME_PKG_NAME, AppUsageEvent.CHROME_APP_MAIN_CLASS_NAME,2,1437157396244l));
    eventSequence.add(new AppUsageEvent(AppUsageEvent.COM_ANDROID_CHROME_PKG_NAME, AppUsageEvent.CHROME_APP_FIRST_RUN_ACTIVITY_STAGING_CLASS_NAME,1,1437157396267l));
    eventSequence.add(new AppUsageEvent(AppUsageEvent.COM_ANDROID_CHROME_PKG_NAME, AppUsageEvent.CHROME_APP_FIRST_RUN_ACTIVITY_STAGING_CLASS_NAME,2,1437157396326l));
    eventSequence.add(new AppUsageEvent(AppUsageEvent.COM_ANDROID_CHROME_PKG_NAME, AppUsageEvent.CHROME_APP_MAIN_CLASS_NAME,1,1437157396369l));
    eventSequence.add(new AppUsageEvent(AppUsageEvent.COM_ANDROID_CHROME_PKG_NAME, AppUsageEvent.CHROME_APP_MAIN_CLASS_NAME,2,1437157396389l));
    eventSequence.add(new AppUsageEvent(AppUsageEvent.COM_ANDROID_CHROME_PKG_NAME, AppUsageEvent.CHROME_APP_DOCUMENT_ACTIVITY_CLASS_NAME,1,1437157396395l));
  }

  public void addDialerClosedSequence(List<AppUsageEvent> eventSequence) {
    eventSequence.add( new AppUsageEvent("com.google.android.dialer", "com.google.android.dialer.extensions.GoogleDialtactsActivity", 2, 1437159208526l));
    eventSequence.add( new AppUsageEvent(AppUsageEvent.ANDROID_LOLLIPOP_HOME_APP_PKG, AppUsageEvent.ANDROID_LOLLIPOP_HOME_APP_CLASS, 1, 1437159208538l));
  }

  public void addChromeCloseSequence(List<AppUsageEvent> eventSequence) {
    eventSequence.add(new AppUsageEvent(AppUsageEvent.COM_ANDROID_CHROME_PKG_NAME, AppUsageEvent.CHROME_APP_DOCUMENT_ACTIVITY_CLASS_NAME, 2, 1437164432583l));
    eventSequence.add(new AppUsageEvent(AppUsageEvent.ANDROID_LOLLIPOP_HOME_APP_PKG, AppUsageEvent.ANDROID_LOLLIPOP_HOME_APP_CLASS, 1, 1437164432597l));
  }

  public void addIncognitoModeTab(List<AppUsageEvent> eventSequence) {
    eventSequence.add(new AppUsageEvent("com.android.chrome", "com.google.android.apps.chrome.document.DocumentActivity", 2, 1437165631111l));
    eventSequence.add(new AppUsageEvent("com.android.chrome", "com.google.android.apps.chrome.document.IncognitoDocumentActivity", 1, 1437165631131l));
    eventSequence.add(new AppUsageEvent("com.android.chrome", "com.google.android.apps.chrome.document.IncognitoDocumentActivity", 2, 1437165631873l));
    eventSequence.add(new AppUsageEvent("com.android.chrome", "com.google.android.apps.chrome.document.CipherKeyActivity", 1, 1437165631917l));
    eventSequence.add(new AppUsageEvent("com.android.chrome", "com.google.android.apps.chrome.document.CipherKeyActivity", 2, 1437165631942l));
    eventSequence.add(new AppUsageEvent("com.android.chrome", "com.google.android.apps.chrome.document.IncognitoDocumentActivity", 1, 1437165631966l));
  }

  public void addGmailOpen(List<AppUsageEvent> eventSequence) {
    eventSequence.add(new AppUsageEvent("com.google.android.gm", "com.google.android.gm.ConversationListActivityGmail", 1, 1437165911761l));
  }


  public void addSession(List<AppUsageEvent> eventSequence) {
    // 1 open chrome from home screen
    addSession_OpenChrome(eventSequence);

    // 1.1 repeat - notice same timestamps
//    07-17 13:47:58.212: I/Paco(16011): ==================================
//    07-17 13:47:58.213: I/Paco(16011): eventSequence.add(new AppUsageEvent("com.google.android.googlequicksearchbox", "com.google.android.launcher.GEL", 2, 1437166074978l));
//    07-17 13:47:58.213: I/Paco(16011): eventSequence.add(new AppUsageEvent("com.android.chrome", "com.google.android.apps.chrome.Main", 1, 1437166074997l));
//    07-17 13:47:58.213: I/Paco(16011): eventSequence.add(new AppUsageEvent("com.android.chrome", "com.google.android.apps.chrome.Main", 2, 1437166075015l));
//    07-17 13:47:58.213: I/Paco(16011): eventSequence.add(new AppUsageEvent("com.android.chrome", "org.chromium.chrome.browser.firstrun.FirstRunActivityStaging", 1, 1437166075037l));
//    07-17 13:47:58.213: I/Paco(16011): eventSequence.add(new AppUsageEvent("com.android.chrome", "org.chromium.chrome.browser.firstrun.FirstRunActivityStaging", 2, 1437166075102l));
//    07-17 13:47:58.213: I/Paco(16011): eventSequence.add(new AppUsageEvent("com.android.chrome", "com.google.android.apps.chrome.Main", 1, 1437166075149l));
//    07-17 13:47:58.213: I/Paco(16011): eventSequence.add(new AppUsageEvent("com.android.chrome", "com.google.android.apps.chrome.Main", 2, 1437166075166l));
//    07-17 13:47:58.213: I/Paco(16011): eventSequence.add(new AppUsageEvent("com.android.chrome", "com.google.android.apps.chrome.document.DocumentActivity", 1, 1437166075178l));

    // 2 close chrome
//    07-17 13:48:07.562: I/Paco(16011): ==================================
    addSession_closeChrome(eventSequence);

    // 2.1 repeat
//    07-17 13:48:08.580: I/Paco(16011): ==================================
//    07-17 13:48:08.581: I/Paco(16011): eventSequence.add(new AppUsageEvent("com.android.chrome", "com.google.android.apps.chrome.document.DocumentActivity", 2, 1437166085288l));
//    07-17 13:48:08.581: I/Paco(16011): eventSequence.add(new AppUsageEvent("com.google.android.googlequicksearchbox", "com.google.android.launcher.GEL", 1, 1437166085298l));

    // 3 open contacts/dialer
    //07-17 13:48:10.644: I/Paco(16011): ==================================
    addSessionOpenContacts(eventSequence);

    // 3.1 repeat
//    07-17 13:48:11.664: I/Paco(16011): ==================================
//    07-17 13:48:11.665: I/Paco(16011): eventSequence.add(new AppUsageEvent("com.google.android.googlequicksearchbox", "com.google.android.launcher.GEL", 2, 1437166088294l));
//    07-17 13:48:11.665: I/Paco(16011): eventSequence.add(new AppUsageEvent("com.google.android.dialer", "com.google.android.dialer.extensions.GoogleDialtactsActivity", 1, 1437166088310l));

    // 4 open recents from contacts/dialer
//    07-17 13:48:14.748: I/Paco(16011): ==================================
    addSessionOpenRecents(eventSequence);

    // 4.1 repeat
//    07-17 13:48:15.788: I/Paco(16011): ==================================
//    07-17 13:48:15.790: I/Paco(16011): eventSequence.add(new AppUsageEvent("com.google.android.dialer", "com.google.android.dialer.extensions.GoogleDialtactsActivity", 2, 1437166092267l));
//    07-17 13:48:15.790: I/Paco(16011): eventSequence.add(new AppUsageEvent("com.android.systemui", "com.android.systemui.recents.RecentsActivity", 1, 1437166092272l));

    // 5 pick chrome from recents
//    07-17 13:48:16.828: I/Paco(16011): ==================================
    addSession_OpenChromeFromRecents(eventSequence);

    // 5.1 repeat
//    07-17 13:48:17.880: I/Paco(16011): ==================================
//    07-17 13:48:17.881: I/Paco(16011): eventSequence.add(new AppUsageEvent("com.android.systemui", "com.android.systemui.recents.RecentsActivity", 2, 1437166095045l));
//    07-17 13:48:17.881: I/Paco(16011): eventSequence.add(new AppUsageEvent("com.android.chrome", "com.google.android.apps.chrome.document.DocumentActivity", 1, 1437166095055l));

    // 5.2 three peat
//    07-17 13:48:18.920: I/Paco(16011): ==================================
//    07-17 13:48:18.921: I/Paco(16011): eventSequence.add(new AppUsageEvent("com.android.systemui", "com.android.systemui.recents.RecentsActivity", 2, 1437166095045l));
//    07-17 13:48:18.921: I/Paco(16011): eventSequence.add(new AppUsageEvent("com.android.chrome", "com.google.android.apps.chrome.document.DocumentActivity", 1, 1437166095055l));

    // 6 chrome to home
//    07-17 13:48:27.270: I/Paco(16011): ==================================
    addSession_closeChromeAgain(eventSequence);

    // 6.1 repeat
//    07-17 13:48:28.278: I/Paco(16011): ==================================
//    07-17 13:48:28.279: I/Paco(16011): eventSequence.add(new AppUsageEvent("com.android.chrome", "com.google.android.apps.chrome.document.DocumentActivity", 2, 1437166104649l));
//    07-17 13:48:28.279: I/Paco(16011): eventSequence.add(new AppUsageEvent("com.google.android.googlequicksearchbox", "com.google.android.launcher.GEL", 1, 1437166104674l));

  }

  public void addSession_closeChromeAgain(List<AppUsageEvent> eventSequence) {
    eventSequence.add(new AppUsageEvent("com.android.chrome", "com.google.android.apps.chrome.document.DocumentActivity", 2, 1437166104649l));
    eventSequence.add(new AppUsageEvent("com.google.android.googlequicksearchbox", "com.google.android.launcher.GEL", 1, 1437166104674l));
  }

  public void addSession_OpenChromeFromRecents(List<AppUsageEvent> eventSequence) {
    eventSequence.add(new AppUsageEvent("com.android.systemui", "com.android.systemui.recents.RecentsActivity", 2, 1437166095045l));
    eventSequence.add(new AppUsageEvent("com.android.chrome", "com.google.android.apps.chrome.document.DocumentActivity", 1, 1437166095055l));
  }

  public void addSessionOpenRecents(List<AppUsageEvent> eventSequence) {
    eventSequence.add(new AppUsageEvent("com.google.android.dialer", "com.google.android.dialer.extensions.GoogleDialtactsActivity", 2, 1437166092267l));
    eventSequence.add(new AppUsageEvent("com.android.systemui", "com.android.systemui.recents.RecentsActivity", 1, 1437166092272l));
  }

  public void addSessionOpenContacts(List<AppUsageEvent> eventSequence) {
    eventSequence.add(new AppUsageEvent("com.google.android.googlequicksearchbox", "com.google.android.launcher.GEL", 2, 1437166088294l));
    eventSequence.add(new AppUsageEvent("com.google.android.dialer", "com.google.android.dialer.extensions.GoogleDialtactsActivity", 1, 1437166088310l));
  }

  public void addSession_closeChrome(List<AppUsageEvent> eventSequence) {
    eventSequence.add(new AppUsageEvent("com.android.chrome", "com.google.android.apps.chrome.document.DocumentActivity", 2, 1437166085288l));
    eventSequence.add(new AppUsageEvent("com.google.android.googlequicksearchbox", "com.google.android.launcher.GEL", 1, 1437166085298l));
  }

  public void addSession_OpenChrome(List<AppUsageEvent> eventSequence) {
    eventSequence.add(new AppUsageEvent("com.google.android.googlequicksearchbox", "com.google.android.launcher.GEL", 2, 1437166074978l));
    eventSequence.add(new AppUsageEvent("com.android.chrome", "com.google.android.apps.chrome.Main", 1, 1437166074997l));
    eventSequence.add(new AppUsageEvent("com.android.chrome", "com.google.android.apps.chrome.Main", 2, 1437166075015l));
    eventSequence.add(new AppUsageEvent("com.android.chrome", "org.chromium.chrome.browser.firstrun.FirstRunActivityStaging", 1, 1437166075037l));
    eventSequence.add(new AppUsageEvent("com.android.chrome", "org.chromium.chrome.browser.firstrun.FirstRunActivityStaging", 2, 1437166075102l));
    eventSequence.add(new AppUsageEvent("com.android.chrome", "com.google.android.apps.chrome.Main", 1, 1437166075149l));
    eventSequence.add(new AppUsageEvent("com.android.chrome", "com.google.android.apps.chrome.Main", 2, 1437166075166l));
    eventSequence.add(new AppUsageEvent("com.android.chrome", "com.google.android.apps.chrome.document.DocumentActivity", 1, 1437166075178l));
  }

}
