package com.pacoapp.paco.net;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.pacoapp.paco.UserPreferences;
import com.pacoapp.paco.os.AndroidUtils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.Bundle;
import android.util.Pair;

public class PacoService {

  private static Logger Log = LoggerFactory.getLogger(PacoService.class);

  private Context context;
  private UserPreferences userPrefs;
  private int statusCode = 200;

  public PacoService(Context context) {
    this.context = context;
    userPrefs = new UserPreferences(context);
  }

  public String post(String url, String body, List<Pair<String, String>> headers) {
    return service("POST", url, body, headers);
  }

  public String get(String url, List<Pair<String, String>> headers) {
    return service("GET", url, "", headers);
  }

  String service(String httpMethod, String url, String body, List<Pair<String, String>> headers) {
    if (headers == null) {
      headers  = Lists.newArrayList();
    }
    addStandardHeaders(headers);
    TokenFuture future = TokenFuture.run(context, userPrefs.getSelectedAccount());
    try {
      String token = future.get();
      if (future.isCancelled()) {
        setStatus(500); // TODO clean this up to propagate the correct errors
        return null;
      }
      addAccessTokenBearerHeader(token, headers);
      return makeCall(httpMethod, url, body, headers);
    } catch (InterruptedException e) {
      Log.error("InterruptedException", e);
    } catch (ExecutionException e) {
      Log.error("ExecutionException", e);
      e.printStackTrace();
    }
    setStatus(500);
    return null;

  }

  public String makeCall(String httpMethod, String url, String body, List<Pair<String, String>> headers) {
    Log.info("Making the call to " + url + " accessToken = " + userPrefs.getAccessToken() +"\nBody: \n" + body);

    HttpsURLConnection urlConnection = null;
    try {
      URL u = new URL(url);
      urlConnection = (HttpsURLConnection) u.openConnection();
      urlConnection.setChunkedStreamingMode(0);
      urlConnection.setRequestMethod(httpMethod);

      for (Pair<String, String> header : headers) {
        urlConnection.addRequestProperty(header.first, header.second);
      }

      if ("POST".equals(httpMethod)) {
        urlConnection.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream(), AbstractAuthTokenTask.UTF_8);
        writer.write(body);
        writer.flush();
      }

      InputStreamReader reader = new InputStreamReader(urlConnection.getInputStream(), AbstractAuthTokenTask.UTF_8);
      String result = read(reader);
      //Log.d(PacoConstants.TAG, "RESULT = " + result);
      return result;
    } catch (Exception e) {
      Log.warn("Exception: " + e);
      setStatus(500);
    } finally {
      urlConnection.disconnect();
    }
    return null;
  }

  public void addStandardHeaders(List<Pair<String, String>> headers) {
    headers.add(new Pair<String, String>("http.useragent", "Android"));
    headers.add(new Pair<String, String>("paco.version", AndroidUtils.getAppVersion(context)));
    headers.add(new Pair<String, String>("pacoProtocol", "4"));
  }

//    @SuppressWarnings("deprecation")
//    private AccountManagerFuture<Bundle> getAuthAccessToken(final String accountName, final List<Pair<String, String>> headers) {
//      AccountManager accountManager = AccountManager.get(context);
//      Account[] accounts = accountManager.getAccountsByType("com.google");
//      Account account = null;
//      for (Account currentAccount : accounts) {
//        if (currentAccount.name.equals(accountName)) {
//          account = currentAccount;
//          break;
//        }
//      }
//
//      String accessToken = userPrefs.getAccessToken();
//      if (accessToken != null) {
//        Log.i(PacoConstants.TAG, "Invalidating previous OAuth2 access token: " + accessToken);
//        accountManager.invalidateAuthToken(account.type, accessToken);
//        userPrefs.setAccessToken(null);
//      }
//
//      String authTokenType = PacoService.AUTH_TOKEN_TYPE_USERINFO_EMAIL;
//
//      Log.i(PacoConstants.TAG, "Get access token for " + accountName + " using authTokenType " + authTokenType);
//      return accountManager.getAuthToken(account, authTokenType, true,
//          new AccountManagerCallback<Bundle>() {
//            @Override
//            public void run(AccountManagerFuture<Bundle> future) {
//              try {
//                String accessToken = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
//                Log.i(PacoConstants.TAG, "Got OAuth2 access token: " + accessToken);
//                addAccessTokenBearerHeader(accessToken, headers);
//              } catch (OperationCanceledException e) {
//                Log.e(PacoConstants.TAG, "The user has denied you access to the API");
//              } catch (Exception e) {
//                Log.e(PacoConstants.TAG, e.getMessage());
//                Log.w("Exception: ", e);
//              }
//            }
//
//          }, null);
//
//  }

  public void addAccessTokenBearerHeader(String accessToken, final List<Pair<String, String>> headers) {
    userPrefs.setAccessToken(accessToken);
    headers.add(new Pair<String, String>("Authorization", "Bearer " + accessToken));
  }

  static String read(InputStreamReader reader) throws IOException {
    char[] chars = new char[1024];
    StringBuffer buf = new StringBuffer();
    int len;
    while ((len = reader.read(chars)) != -1) {
      buf.append(chars, 0, len);
    }
    return buf.toString();
  }


  public static class TokenFuture implements Future<String> {
    private volatile String result;
    private volatile boolean cancelled = false;
    private CountDownLatch countDownLatch;
    private Context context;
    private String accountName;
    private UserPreferences userPrefs;

    public TokenFuture(Context context, String accountName) {
      this.context = context;
      this.userPrefs = new UserPreferences(context);
      this.accountName = accountName;
      countDownLatch = new CountDownLatch(1);
    }

    public static TokenFuture run(Context context, String accountName) {
      final TokenFuture tf = new TokenFuture(context, accountName);

      new Thread(new Runnable() {
        @Override
        public void run() {
          tf.getAuthAccessToken();
        }
      }).start();
      return tf;
    }

    @SuppressWarnings("deprecation")
    protected void getAuthAccessToken() {
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType("com.google");
        Account account = null;
        for (Account currentAccount : accounts) {
          if (currentAccount.name.equals(accountName)) {
            account = currentAccount;
            break;
          }
        }

        String accessToken = userPrefs.getAccessToken();
        if (accessToken != null) {
          Log.info("Invalidating previous OAuth2 access token: " + accessToken);
          accountManager.invalidateAuthToken(account.type, accessToken);
          userPrefs.setAccessToken(null);
        }

        String authTokenType = AbstractAuthTokenTask.AUTH_TOKEN_TYPE_USERINFO_EMAIL;

        Log.info("Get access token for " + accountName + " using authTokenType " + authTokenType);
        accountManager.getAuthToken(account, authTokenType, true,
            new AccountManagerCallback<Bundle>() {
              @Override
              public void run(AccountManagerFuture<Bundle> future) {
                try {
                  String accessToken = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
                  onResult(accessToken);
                  Log.info("Got OAuth2 access token: " + accessToken);
                } catch (OperationCanceledException e) {
                  Log.error("TokenError: The user has denied you access to the API");
                  cancel(false);
                } catch (Exception e) {
                  Log.error("TokenError: " + e.getMessage());
                  Log.error("Exception: ", e);
                  cancel(false);
                }
              }

            }, null);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      if (isDone()) {
        return false;
      } else {
        countDownLatch.countDown();
        cancelled = true;
        return !isDone();
      }
    }

    @Override
    public boolean isCancelled() {
      return cancelled;
    }

    @Override
    public boolean isDone() {
      return countDownLatch.getCount() == 0;
    }

    @Override
    public String get() throws InterruptedException, ExecutionException {
      countDownLatch.await();
      return result;
    }

    @Override
    public String get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      countDownLatch.await(timeout, unit);
      return result;
    }

    public void onResult(final String result) {
      this.result = result;
      countDownLatch.countDown();
    }

  }

  public void setStatus(int code) {
    this.statusCode = code;
  }

  public int getStatusCode() {
    return statusCode;
  }


}
