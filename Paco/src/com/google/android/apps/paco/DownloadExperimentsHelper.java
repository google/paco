package com.google.android.apps.paco;

import java.net.URLEncoder;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.pacoapp.paco.UserPreferences;
import com.pacoapp.paco.net.NetworkUtil;
import com.pacoapp.paco.net.PacoService;
import com.pacoapp.paco.net.ServerAddressBuilder;

import android.content.Context;

public class DownloadExperimentsHelper {

  private static Logger Log = LoggerFactory.getLogger(DownloadExperimentsHelper.class);
  private Context context;
  private UserPreferences userPrefs;
  private String contentAsString;
  private String cursor;
  private Integer limit;

  public DownloadExperimentsHelper(Context context, UserPreferences userPrefs, Integer limit, String cursor) {
    this.context = context;
    this.userPrefs = userPrefs;
    this.cursor = cursor;
    this.limit = limit;
  }

  public String downloadMyExperiments() {
    try {
      contentAsString = makeMyExperimentsRequest();
      if (contentAsString == null) {
        return NetworkUtil.RETRIEVAL_ERROR;
      }
      return NetworkUtil.SUCCESS;
    } catch (Exception e) {
      Log.error("Exception. Unable to update my experiments, " + e.getMessage());
      return NetworkUtil.SERVER_COMMUNICATION_ERROR;
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
        return NetworkUtil.RETRIEVAL_ERROR;
      }
      return NetworkUtil.SUCCESS;
    } catch (Exception e) {
      Log.error("Exception. Unable to update available experiments, " + e.getMessage());
      return NetworkUtil.SERVER_COMMUNICATION_ERROR;
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
        return NetworkUtil.RETRIEVAL_ERROR;
      }
      return NetworkUtil.SUCCESS;
    } catch (Exception e) {
      Log.error("Exception. Unable to update running experiments, " + e.getMessage());
      return NetworkUtil.SERVER_COMMUNICATION_ERROR;
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
