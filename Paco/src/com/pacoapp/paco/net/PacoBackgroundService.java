package com.pacoapp.paco.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.common.collect.Lists;
import com.pacoapp.paco.os.AndroidUtils;

import android.util.Pair;

public class PacoBackgroundService extends GetInBackground {

  private static Logger Log = LoggerFactory.getLogger(PacoBackgroundService.class);

  public static final String POST = "POST";
  public  static final String GET = "GET";
  private static final int MAX_ATTEMPTS = 2;

  private String url;
  private String httpMethod;
  private String body;
  private int attempts;

  public PacoBackgroundService(NetworkClient client, String url, String body) {
    super(client);
    this.url = url;
    this.httpMethod = POST;
    this.body = body;
  }

  public PacoBackgroundService(NetworkClient client, String url) {
    super(client);
    this.url = url;
    this.httpMethod = GET;
  }

  @Override
  protected void doRequest() throws IOException, JSONException {
    String token = fetchToken();
    if (token == null) {
      // error has already been handled in fetchToken()
      return;
    }
    userPrefs.setAccessToken(token);
    List<Pair<String,String>> headers = null; // TODO do we want to pass headers in?
    if (headers == null) {
      headers = Lists.newArrayList();
    }
    addStandardHeaders(headers);
    addAccessTokenBearerHeader(fetchToken(), headers);

    URL u = new URL(url);
    HttpURLConnection urlConnection = ServerAddressBuilder.getConnection(u);
    for (Pair<String, String> header : headers) {
      urlConnection.addRequestProperty(header.first, header.second);
    }

    if (POST.equals(httpMethod)) {
      urlConnection.setDoOutput(true);
      urlConnection.setRequestMethod(POST);
      OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream(), AbstractAuthTokenTask.UTF_8);
      writer.write(body);
      writer.flush();
    }

    int sc = 0;
    try {
      sc = urlConnection.getResponseCode();
    } catch (ConnectException e) {
      sc = 503;
    }
    if (sc == 200) {
      InputStream is = urlConnection.getInputStream();
      String result = readResponse(urlConnection.getInputStream());
      //Log.d(PacoConstants.TAG, "RESULT = " + result);
      is.close();
      networkClient.showAndFinish(result);
      return;
    } else if (sc == 401) {
        GoogleAuthUtil.invalidateToken(networkClient.getContext(), token);
        onError("Server auth error, please try again.", null);
        Log.info("Server auth error: " + readResponse(urlConnection.getErrorStream()));
        if (attempts < MAX_ATTEMPTS) {
          attempts++;
          Log.info("Attempt: " + attempts + " for url:  " + url);
          doRequest();
        }
        return;
    } else {
      onError("Server returned the following error code: " + sc, null);
      return;
    }

  }

  public void addStandardHeaders(List<Pair<String, String>> headers) {
    headers.add(new Pair<String, String>("http.useragent", "Android"));
    headers.add(new Pair<String, String>("paco.version", AndroidUtils.getAppVersion(networkClient.getContext())));
    headers.add(new Pair<String, String>("pacoProtocol", "4"));
  }

  public void addAccessTokenBearerHeader(String accessToken, final List<Pair<String, String>> headers) {
    headers.add(new Pair<String, String>("Authorization", "Bearer " + accessToken));
  }



}
