package com.pacoapp.paco.net;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.joda.time.DateTime;

import com.google.common.base.Joiner;
import com.pacoapp.paco.UserPreferences;

public class ExperimentUrlBuilder {

  public static String buildUrlForMyExperiments(UserPreferences userPrefs, String cursor, Integer downloadLimit) {
    return createExperimentsUrl(userPrefs, cursor, downloadLimit, "mine");
  }

  public static String buildUrlForPublicExperiments(UserPreferences userPrefs, String cursor, Integer limit) {
    return createExperimentsUrl(userPrefs, cursor, limit, "public");
  }

  public static String createExperimentsUrl(UserPreferences userPrefs, String cursor, Integer downloadLimit, String flag) {
    String serverAddress = userPrefs.getServerAddress();
    String path = "/experiments?" + flag;
    if (cursor != null) {
      path += "&cursor="+cursor;
    }
    if (downloadLimit != null) {
      path += "&limit=" + downloadLimit;
    }
    String timezoneId = new DateTime().getZone().getID();
    try {
      path += "&tz=" + URLEncoder.encode(timezoneId, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return ServerAddressBuilder.createServerUrl(serverAddress, path);
  }

  public static String buildUrlForFullExperiment(UserPreferences userPreferences, Long... experimentIds) {
    String experimentIdSuffix = formatExperimentIdList(experimentIds);
    return createExperimentsUrl(userPreferences, null, null, "id=" + experimentIdSuffix);
  }

  private static String formatExperimentIdList(Long... experimentIds) {
    return Joiner.on(",").join(experimentIds);
  }
}
