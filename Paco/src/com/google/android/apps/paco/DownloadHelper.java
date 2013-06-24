package com.google.android.apps.paco;

import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import android.content.Context;
import android.util.Log;

import com.google.corp.productivity.specialprojects.android.comm.Response;
import com.google.corp.productivity.specialprojects.android.comm.UrlContentManager;

public class DownloadHelper {

  private Context context;
  private ExperimentProviderUtil experimentProviderUtil;
  private UrlContentManager manager;
  private UserPreferences userPrefs;
  private List<Experiment> experimentsList;

  public DownloadHelper(Context context, ExperimentProviderUtil experimentProviderUtil, 
                        UrlContentManager manager, UserPreferences userPrefs) {
    this.context = context;
    this.experimentProviderUtil = experimentProviderUtil;
    this.manager = manager;
    this.userPrefs = userPrefs;
  }

  public void updateFindExperiments() throws Exception {
    String contentAsString = makeExperimentRequest("short");
    if (contentAsString != null) {
      try {
        experimentProviderUtil.saveExperimentsToDisk(contentAsString);
      } catch (JsonParseException e) {
        Log.e(PacoConstants.TAG, "Could not parse text: " + contentAsString + ", " + e.getMessage());
      } catch (JsonMappingException e) {
        Log.e(PacoConstants.TAG, "Could not map json: " + contentAsString + ", " + e.getMessage());
      } catch (UnsupportedCharsetException e) {
        Log.e(PacoConstants.TAG, "UnsupportedCharset. json: " + contentAsString + ", " + e.getMessage());
      } catch (IOException e) {
        Log.e(PacoConstants.TAG, "IOException. json: " + contentAsString + ", " + e.getMessage());
      }
    }
    userPrefs.setExperimentListRefreshTime(new Date().getTime(), UserPreferences.FIND_EXPERIMENTS);
  }

  public String updateRunningExperiments(List<Experiment> experiments, Boolean isFullJoinedRefresh) throws Exception {
    String pathSuffix = getExperimentIdList(experiments);
    String contentAsString = makeExperimentRequest("id=" + pathSuffix);
    
    if (contentAsString == null) {
      return DownloadStatusConstants.RETRIEVAL_ERROR;
    }
    
    try {
      experimentsList = ExperimentProviderUtil.getExperimentsFromJson(contentAsString);
      if (isFullJoinedRefresh) {
        experimentProviderUtil.updateExistingExperiments(experimentsList);
      }
    } catch (JsonParseException e) {
      Log.e(PacoConstants.TAG, "Could not parse text: " + contentAsString + ", " + e.getMessage());
      return DownloadStatusConstants.CONTENT_ERROR;
    } catch (JsonMappingException e) {
      Log.e(PacoConstants.TAG, "Could not map json: " + contentAsString + ", " + e.getMessage());
      return DownloadStatusConstants.CONTENT_ERROR;
    } catch (UnsupportedCharsetException e) {
      Log.e(PacoConstants.TAG, "UnsupportedCharset. json: " + contentAsString + ", " + e.getMessage());
      return DownloadStatusConstants.CONTENT_ERROR;
    } catch (IOException e) {
      Log.e(PacoConstants.TAG, "IOException. json: " + contentAsString + ", " + e.getMessage());
      return DownloadStatusConstants.CONTENT_ERROR;
    }
    
    if (isFullJoinedRefresh) {
      userPrefs.setExperimentListRefreshTime(new Date().getTime(), UserPreferences.JOINED_EXPERIMENTS);
    }
    
    return DownloadStatusConstants.SUCCESS; 
  }

  private String makeExperimentRequest(String flag) throws Exception {
    manager = new UrlContentManager(context);
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

  // TODO: concatenation is slow. Does Java have a nice version of reduce?
  private String getExperimentIdList(List<Experiment> experiments) {
    String suffix = "";
    for (Experiment experiment : experiments) {
      suffix += experiment.getServerId().toString();
      suffix += ",";
    }
    return suffix;
  }

}
