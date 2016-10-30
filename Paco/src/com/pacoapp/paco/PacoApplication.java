package com.pacoapp.paco;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Application;
import android.app.PendingIntent;
import android.content.res.Configuration;
import im.delight.android.languages.Language;

public class PacoApplication extends Application {

//  private static final String WALKING_FENCE = "walkingFence";

  private static Logger Log = LoggerFactory.getLogger(PacoApplication.class);

  public static final String CUSTOM_LANGUAGE_KEY = "customLanguageKey";


//  private GoogleApiClient client;
//  private MyFenceReceiver myFenceReceiver;
//  private final String FENCE_RECEIVER_ACTION = "com.pacoapp.paco" + "FENCE_RECEIVER_ACTION";

  protected PendingIntent pendingIntent;

  @Override
  public void onCreate() {
    super.onCreate();
    DateTime.now(); // load this early to try to circumvent joda bug
    Language.setFromPreference(this, CUSTOM_LANGUAGE_KEY);


    //initAwarenessApiTest();

  }

//  private void initAwarenessApiTest() {
//    //    myFenceReceiver = new MyFenceReceiver();
//    //    registerReceiver(myFenceReceiver, new IntentFilter(FENCE_RECEIVER_ACTION));
//
//        client = new GoogleApiClient.Builder(this)
//                .addApi(Awareness.API)
//                //.enableAutoManage(this, 1, null)
//                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
//                  @Override
//                  public void onConnected(@Nullable Bundle bundle) {
//                      // Set up the PendingIntent that will be fired when the fence is triggered.
//                      Intent intent = new Intent(FENCE_RECEIVER_ACTION);
//                      pendingIntent =
//                              PendingIntent.getBroadcast(PacoApplication.this, 0, intent, 0);
//
//                      // The broadcast receiver that will receive intents when a fence is triggered.
//                      myFenceReceiver = new MyFenceReceiver();
//                      registerReceiver(myFenceReceiver, new IntentFilter(FENCE_RECEIVER_ACTION));
//                      AwarenessFence walkingFence = DetectedActivityFence.during(DetectedActivityFence.WALKING);
//                      registerFence(WALKING_FENCE, walkingFence);
//                  }
//
//                  @Override
//                  public void onConnectionSuspended(int i) {
//                    unregisterFence(WALKING_FENCE);
//                    Log.info("Connection to awareness suspended");
//                  }
//              }).build();
//        client.connect();
//  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

    Language.setFromPreference(this, CUSTOM_LANGUAGE_KEY);
  }


//  protected void registerFence(final String fenceKey, final AwarenessFence fence) {
//    PendingIntent mPendingIntent = null;
//    Awareness.FenceApi.updateFences(client,
//                                    new FenceUpdateRequest.Builder().addFence(fenceKey, fence, mPendingIntent).build())
//                      .setResultCallback(new ResultCallback<Status>() {
//                        @Override
//                        public void onResult(@NonNull Status status) {
//                          if (status.isSuccess()) {
//                            Log.info("Fence was successfully registered.");
//                            // queryFence(fenceKey);
//                          } else {
//                            Log.error("Fence could not be registered: " + status);
//                          }
//                        }
//                      });
//  }
//
//  protected void unregisterFence(final String fenceKey) {
//    Awareness.FenceApi.updateFences(client,
//                                    new FenceUpdateRequest.Builder().removeFence(fenceKey).build())
//                      .setResultCallback(new ResultCallback<Status>() {
//
//                        public void onSuccess(@NonNull Status status) {
//                          Log.info("Fence " + fenceKey + " successfully removed.");
//                        }
//
//
//                        public void onFailure(@NonNull Status status) {
//                          Log.info("Fence " + fenceKey + " could NOT be removed.");
//                        }
//
//
//                        @Override
//                        public void onResult(Status arg0) {
//                          // TODO Auto-generated method stub
//
//                        }
//                      });
//  }
//
//  public class MyFenceReceiver extends BroadcastReceiver {
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        FenceState fenceState = FenceState.extract(intent);
//
//        if (TextUtils.equals(fenceState.getFenceKey(), WALKING_FENCE)) {
//            switch(fenceState.getCurrentState()) {
//                case FenceState.TRUE:
//                    Log.info("Walking.");
//                    break;
//                case FenceState.FALSE:
//                    Log.info("Not Walking.");
//                    break;
//                case FenceState.UNKNOWN:
//                    Log.info("unknown state.");
//                    break;
//            }
//        }
//    }
//}
//
//  protected void queryFence(final String fenceKey) {
////    Awareness.FenceApi.queryFences(client, FenceQueryRequest.forFences(Arrays.asList(fenceKey)))
////                      .setResultCallback(new ResultCallback<FenceQueryResult>() {
////                        @Override
////                        public void onResult(@NonNull FenceQueryResult fenceQueryResult) {
////                          if (!fenceQueryResult.getStatus().isSuccess()) {
////                            Log.e(TAG, "Could not query fence: " + fenceKey);
////                            return;
////                          }
////                          FenceStateMap map = fenceQueryResult.getFenceStateMap();
////                          for (String fenceKey : map.getFenceKeys()) {
////                            FenceState fenceState = map.getFenceState(fenceKey);
////                            Log.info("Fence " + fenceKey + ": " + fenceState.getCurrentState() + ", was="
////                                     + fenceState.getPreviousState() + ", lastUpdateTime="
////                                     + DATE_FORMAT.format(new Date(fenceState.getLastFenceUpdateTimeMillis())));
////                          }
////                        }
////                      });
//  }
}
