package com.google.android.apps.paco;

import java.util.List;

import android.content.Context;
import android.util.Log;

import com.google.common.base.Joiner;
import com.google.corp.productivity.specialprojects.android.comm.Request;
import com.google.corp.productivity.specialprojects.android.comm.Response;
import com.google.corp.productivity.specialprojects.android.comm.UrlContentManager;

public class DownloadHelper {

  public static final int INVALID_DATA_ERROR = 1003;
  public static final int SERVER_ERROR = 1004;
  public static final int NO_NETWORK_CONNECTION = 1005;
  
  public static final int ENABLED_NETWORK = 1;
  
  private Context context;
  private UrlContentManager manager;
  private UserPreferences userPrefs;
  private String contentAsString;
  private Request request;
  public static final String EXECUTION_ERROR = "execution_error";
  public static final String SERVER_COMMUNICATION_ERROR = "server_communication_error";
  public static final String CONTENT_ERROR = "content_error";
  public static final String RETRIEVAL_ERROR = "retrieval_error";
  public static final String SUCCESS = "success";

  public DownloadHelper(Context context, UserPreferences userPrefs) {
    this.context = context;
    this.manager = new UrlContentManager(context);
    this.userPrefs = userPrefs;
  }

  public String downloadAvailableExperiments() {
    try {
      contentAsString = makeAvailableExperimentsRequest();
      if (contentAsString == null) {
        return DownloadHelper.RETRIEVAL_ERROR;
      }
      return DownloadHelper.SUCCESS;
    } catch (Exception e) {
      Log.e(PacoConstants.TAG, "Exception. Unable to update available experiments, " + e.getMessage());
      return DownloadHelper.SERVER_COMMUNICATION_ERROR;
    } finally {
      if (manager != null) {
        manager.cleanUp();
      }
    }
  }
  
  // Visible for testing
  public String makeAvailableExperimentsRequest() throws Exception {
    return makeExperimentRequest("short");
  }

  public String downloadRunningExperiments(List<Long> experimentIds) {
    try {
      contentAsString = makeRunningExperimentsRequest(experimentIds);
      if (contentAsString == null) {
        return DownloadHelper.RETRIEVAL_ERROR;
      }
      return DownloadHelper.SUCCESS; 
    } catch (Exception e) {
      Log.e(PacoConstants.TAG, "Exception. Unable to update running experiments, " + e.getMessage());
      return DownloadHelper.SERVER_COMMUNICATION_ERROR;
    } finally {
      if (manager != null) {
        manager.cleanUp();
      }
    }
  }
  
  // Visible for testing
  public String makeRunningExperimentsRequest(List<Long> experimentIds) throws Exception {
    String experimentIdSuffix = formatExperimentIdList(experimentIds);
    return makeExperimentRequest("id=" + experimentIdSuffix);
  }

  private String makeExperimentRequest(String flag) throws Exception {
    String serverAddress = userPrefs.getServerAddress();
    String path = "/experiments?" + flag;
    request = manager.createRequest();
    Response response = request.setUrl(ServerAddressBuilder.createServerUrl(serverAddress, path))
        .addHeader("http.useragent", "Android")
        .addHeader("paco.version", AndroidUtils.getAppVersion(context)).execute();
    return response.getContentAsString();
  }

  private String formatExperimentIdList(List<Long> experimentIds) {
    String list = Joiner.on(",").join(experimentIds);
    return list;
  }
  
  public String getContentAsString() {
    return contentAsString;
  }
  
  public Request getRequest() {
    return request;
  }

}
