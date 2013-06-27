package com.google.android.apps.paco;

import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import android.content.Context;
import android.util.Log;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.corp.productivity.specialprojects.android.comm.Response;
import com.google.corp.productivity.specialprojects.android.comm.UrlContentManager;

public class DownloadHelper {

  public static final int INVALID_DATA_ERROR = 1003;
  public static final int SERVER_ERROR = 1004;
  public static final int NO_NETWORK_CONNECTION = 1005;
  
  public static final int ENABLED_NETWORK = 1;
  
  private Context context;
  private ExperimentProviderUtil experimentProviderUtil;
  private UrlContentManager manager;
  private UserPreferences userPrefs;
  private List<Experiment> experimentsList;
  public static final String EXECUTION_ERROR = "execution_error";
  public static final String SERVER_COMMUNICATION_ERROR = "server_communication_error";
  public static final String CONTENT_ERROR = "content_error";
  public static final String RETRIEVAL_ERROR = "retrieval_error";
  public static final String SUCCESS = "success";

  public DownloadHelper(Context context, ExperimentProviderUtil experimentProviderUtil, 
                        UserPreferences userPrefs) {
    this.context = context;
    this.experimentProviderUtil = experimentProviderUtil;
    this.manager = new UrlContentManager(context);
    this.userPrefs = userPrefs;
  }

  public String updateAvailableExperiments() {
    try {
      String contentAsString = makeExperimentRequest("short");

      if (contentAsString == null) {
        return DownloadHelper.RETRIEVAL_ERROR;
      }

      try {
        experimentProviderUtil.saveExperimentsToDisk(contentAsString);
      } catch (JsonParseException e) {
        Log.e(PacoConstants.TAG, "Could not parse text: " + contentAsString + ", " + e.getMessage());
        return DownloadHelper.CONTENT_ERROR;
      } catch (JsonMappingException e) {
        Log.e(PacoConstants.TAG, "Could not map json: " + contentAsString + ", " + e.getMessage());
        return DownloadHelper.CONTENT_ERROR;
      } catch (UnsupportedCharsetException e) {
        Log.e(PacoConstants.TAG, "UnsupportedCharset. json: " + contentAsString + ", " + e.getMessage());
        return DownloadHelper.CONTENT_ERROR;
      } catch (IOException e) {
        Log.e(PacoConstants.TAG, "IOException. json: " + contentAsString + ", " + e.getMessage());
        return DownloadHelper.CONTENT_ERROR;
      }
      userPrefs.setAvailableExperimentListRefreshTime(new Date().getTime());
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

  public String updateRunningExperiments(List<Experiment> experiments, Boolean isAllRunningUpdate) {
    try {
      String pathSuffix = getExperimentIdList(experiments);
      String contentAsString = makeExperimentRequest("id=" + pathSuffix);

      if (contentAsString == null) {
        return DownloadHelper.RETRIEVAL_ERROR;
      }

      try {
        experimentsList = ExperimentProviderUtil.getExperimentsFromJson(contentAsString);
        if (isAllRunningUpdate) {
          experimentProviderUtil.updateExistingExperiments(experimentsList);
        }
      } catch (JsonParseException e) {
        Log.e(PacoConstants.TAG, "Could not parse text: " + contentAsString + ", " + e.getMessage());
        return DownloadHelper.CONTENT_ERROR;
      } catch (JsonMappingException e) {
        Log.e(PacoConstants.TAG, "Could not map json: " + contentAsString + ", " + e.getMessage());
        return DownloadHelper.CONTENT_ERROR;
      } catch (UnsupportedCharsetException e) {
        Log.e(PacoConstants.TAG, "UnsupportedCharset. json: " + contentAsString + ", " + e.getMessage());
        return DownloadHelper.CONTENT_ERROR;
      } catch (IOException e) {
        Log.e(PacoConstants.TAG, "IOException. json: " + contentAsString + ", " + e.getMessage());
        return DownloadHelper.CONTENT_ERROR;
      }
      if (isAllRunningUpdate) {
        userPrefs.setJoinedExperimentListRefreshTime(new Date().getTime());
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

  private String makeExperimentRequest(String flag) throws Exception {
    String serverAddress = userPrefs.getServerAddress();
    String path = "/experiments?" + flag;
    Response response = manager.createRequest().setUrl(ServerAddressBuilder.createServerUrl(serverAddress, path))
        .addHeader("http.useragent", "Android")
        .addHeader("paco.version", AndroidUtils.getAppVersion(context)).execute();
    return response.getContentAsString();
  }
  
  public List<Experiment> getExperiments() {
    return experimentsList;
  }

  private String getExperimentIdList(List<Experiment> experiments) {
    List<Long> experimentIds = Lists.transform(experiments, new Function<Experiment, Long>() {
      public Long apply(Experiment experiment) {
        return experiment.getServerId();
      }
    });
    String list = Joiner.on(",").join(experimentIds);
    return list;
  }

}
