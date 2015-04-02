package com.google.android.apps.paco;

import java.net.URLEncoder;
import java.util.List;

import org.joda.time.DateTime;

import android.content.Context;
import android.util.Log;

import com.google.android.apps.paco.utils.PacoService;
import com.google.common.base.Joiner;

public class DownloadHelper {

  public static final int INVALID_DATA_ERROR = 1003;
  public static final int SERVER_ERROR = 1004;
  public static final int NO_NETWORK_CONNECTION = 1005;

  public static final int ENABLED_NETWORK = 1;

  private Context context;
  private UserPreferences userPrefs;
  private String contentAsString;
  private String cursor;
  private Integer limit;
  public static final String EXECUTION_ERROR = "execution_error";
  public static final String SERVER_COMMUNICATION_ERROR = "server_communication_error";
  public static final String CONTENT_ERROR = "content_error";
  public static final String RETRIEVAL_ERROR = "retrieval_error";
  public static final String SUCCESS = "success";

  public DownloadHelper(Context context, UserPreferences userPrefs, Integer limit, String cursor) {
    this.context = context;
    this.userPrefs = userPrefs;
    this.cursor = cursor;
    this.limit = limit;
  }

  public String downloadMyExperiments() {
    try {
      contentAsString = makeMyExperimentsRequest();
      if (contentAsString == null) {
        return DownloadHelper.RETRIEVAL_ERROR;
      }
      return DownloadHelper.SUCCESS;
    } catch (Exception e) {
      Log.e(PacoConstants.TAG, "Exception. Unable to update my experiments, " + e.getMessage());
      return DownloadHelper.SERVER_COMMUNICATION_ERROR;
    }
  }

  // Visible for testing
  public String makeMyExperimentsRequest() throws Exception {
    return makeExperimentRequest("mine");
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
    }
  }

  // Visible for testing
  public String makeAvailableExperimentsRequest() throws Exception {
    return makeExperimentRequest("public");
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
    if (cursor != null) {
      path += "&cursor="+cursor;
    }
    if (limit != null) {
      path += "&limit=" + limit;
    }
    String timezoneId = new DateTime().getZone().getID();
    path += "&tz=" + URLEncoder.encode(timezoneId, "UTF-8");
    final String completeUrl = ServerAddressBuilder.createServerUrl(serverAddress, path);
    String result = new PacoService(context).get(completeUrl, null);
    return result;
  }

  private String formatExperimentIdList(List<Long> experimentIds) {
    String list = Joiner.on(",").join(experimentIds);
    return list;
  }

  public String getContentAsString() {
    return contentAsString;
  }


}
