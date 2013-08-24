/*
* Copyright 2011 Google Inc. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance  with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package com.google.android.apps.paco;


import java.util.Date;

import org.joda.time.DateTime;

import android.accounts.Account;
import android.content.Context;
import android.content.SharedPreferences;

import com.pacoapp.paco.R;

/**
 * A place for storing various preferences of the user.
 * Backed by Android shared preferences currently.
 *
 *
 */
public class UserPreferences {

  private static final String PHOTO_ADDRESS = "PHOTO_ADDRESS";

  private static final String LAST_PHOTO_ADDRESS = "LAST_PHOTO_ADDRESS";

  public static final String SIGNALLING_PREFERENCES = "PREFS_PAGING_HOURS";

//  public static final String START_HOUR_PREFERENCE_KEY = "start_hour";
//  public static final String END_HOUR_PREFERENCE_KEY = "end_hour";
//
//  public static final int DEFAULT_START_HOUR = 8;
//  public static final int DEFAULT_END_HOUR = 22;

  private static final String SERVER_ADDRESS = "server_address";

  private static final String SERVER_ADDRESS_KEY = "server_address";


  private static final String APP_PREFERENCES = "app_prefs";

  public static final String FIND_EXPERIMENTS = "FIND_EXPERIMENTS";

  private static final String FIND_MY_EXPERIMENTS = "FIND_MY_EXPERIMENTS";

  public static final String JOINED_EXPERIMENTS = "JOINED_EXPERIMENTS";

  private static final int FIND_LIST_REFRESH_TIMEOUT = 599990; //10 millis less than 5 min

  private static final String FIND_LAST_LIST_REFRESH_PREFERENCE_KEY = "list_refresh";

  private static final String FIND_MY_LAST_LIST_REFRESH_PREFERENCE_KEY = "my_list_refresh";

  private static final int JOIN_LIST_REFRESH_TIMEOUT = 86399990; //10 millis less than 24 hrs

  private static final String JOIN_LAST_LIST_REFRESH_PREFERENCE_KEY = "join_list_refresh";

  private static final String NEXT_SERVER_COMM_REFRESH_PREFERENCE_KEY = "next_server_communication_refresh";

  private static final String SELECTED_ACCOUNT_KEY = "selected_account";

  private static final String SELECTED_ACCOUNT_PREF = "selected_account_pref";

  private static final String RINGTONE_PREF_KEY = "ringtone_pref";

  private static final String RINGTONE_KEY = "ringtone_key";

  private static final String RINGTONE_INSTALLED_KEY = "paco_bark_ringtone_installed";


  private SharedPreferences signallingPrefs;
  private Context context;

  private SharedPreferences appPrefs;

  public static final String COOKIE_PREFERENCE_KEY = "http-cookies";

  public static final String PREFERENCE_KEY = "url-content-manager";

  private static final String EXPERIMENT_TRIGGERED_KEY = null;



  public UserPreferences(Context context) {
    this.context = context;
  }

  SharedPreferences getSignallingPrefs() {
    if (signallingPrefs == null) {
      signallingPrefs = context.getSharedPreferences(SIGNALLING_PREFERENCES, Context.MODE_PRIVATE);
    }
    return signallingPrefs;
  }

//  public int getStartHour() {
//    return getSignallingPrefs().getInt(START_HOUR_PREFERENCE_KEY,
//        DEFAULT_START_HOUR);
//  }

//  public int getEndHour() {
//    return getSignallingPrefs().getInt(END_HOUR_PREFERENCE_KEY,
//        DEFAULT_END_HOUR);
//  }

  public void setServerAddress(String text) {
    getServerAddressPref().edit().putString(SERVER_ADDRESS_KEY, text).commit();
  }

  private SharedPreferences getServerAddressPref() {
    SharedPreferences pref = getPhotoAddressPref();
    return pref;
  }

  public String getServerAddress() {
    return getServerAddressPref().getString(SERVER_ADDRESS_KEY, (String)context.getText(R.string.server));
  }

  SharedPreferences getAppPrefs() {
    if (appPrefs == null) {
      appPrefs = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }
    return appPrefs;
  }

  public boolean isAvailableExperimentsListStale() {
    return isExperimentListStale(FIND_EXPERIMENTS);
  }

  public boolean isMyExperimentsListStale() {
    return isExperimentListStale(FIND_MY_EXPERIMENTS);
  }


  public boolean isJoinedExperimentsListStale() {
    return isExperimentListStale(JOINED_EXPERIMENTS);
  }

  private boolean isExperimentListStale(String refreshType) {
    if (refreshType.equals(FIND_EXPERIMENTS)) {
      return (new Date().getTime() - getAppPrefs().getLong(FIND_LAST_LIST_REFRESH_PREFERENCE_KEY,
          0l)) >= FIND_LIST_REFRESH_TIMEOUT;
    } else if (refreshType.equals(FIND_MY_EXPERIMENTS)) {
      return (new Date().getTime() - getAppPrefs().getLong(FIND_MY_LAST_LIST_REFRESH_PREFERENCE_KEY,
                                                           0l)) >= FIND_LIST_REFRESH_TIMEOUT;
    } else {
      return (new Date().getTime() - getAppPrefs().getLong(JOIN_LAST_LIST_REFRESH_PREFERENCE_KEY,
          0l)) >= JOIN_LIST_REFRESH_TIMEOUT;
    }
  }

  public void setMyExperimentListRefreshTime(Long updateTime) {
    setExperimentListRefreshTime(updateTime, FIND_MY_EXPERIMENTS);
  }

  public void setAvailableExperimentListRefreshTime(Long updateTime) {
    setExperimentListRefreshTime(updateTime, FIND_EXPERIMENTS);
  }

  public void setJoinedExperimentListRefreshTime(Long updateTime) {
    setExperimentListRefreshTime(updateTime, JOINED_EXPERIMENTS);
  }

  private void setExperimentListRefreshTime(Long updateTime, String refreshType) {
    if (refreshType.equals(FIND_EXPERIMENTS)) {
      getAppPrefs().edit().putLong(FIND_LAST_LIST_REFRESH_PREFERENCE_KEY, updateTime).commit();
    } else if (refreshType.equals(FIND_MY_EXPERIMENTS))  {
      getAppPrefs().edit().putLong(FIND_MY_LAST_LIST_REFRESH_PREFERENCE_KEY, updateTime).commit();
    } else {
      getAppPrefs().edit().putLong(JOIN_LAST_LIST_REFRESH_PREFERENCE_KEY, updateTime).commit();
    }
  }

  public DateTime getMyExperimentListRefreshTime() {
    return getExperimentListRefreshTime(FIND_MY_EXPERIMENTS);
  }

  public DateTime getAvailableExperimentListRefreshTime() {
    return getExperimentListRefreshTime(FIND_EXPERIMENTS);
  }

  public DateTime getJoinedExperimentListRefreshTime() {
    return getExperimentListRefreshTime(JOINED_EXPERIMENTS);
  }

  private DateTime getExperimentListRefreshTime(String refreshType) {
    Long lastRefresh;
    if (refreshType.equals(FIND_EXPERIMENTS)) {
      lastRefresh = getAppPrefs().getLong(FIND_LAST_LIST_REFRESH_PREFERENCE_KEY, -1);
    } else if (refreshType.equals(FIND_MY_EXPERIMENTS)) {
      lastRefresh = getAppPrefs().getLong(FIND_MY_LAST_LIST_REFRESH_PREFERENCE_KEY, -1);
    } else {
      lastRefresh = getAppPrefs().getLong(JOIN_LAST_LIST_REFRESH_PREFERENCE_KEY, -1);
    }

    if (lastRefresh.equals(Long.valueOf(-1))) {
      return null;
    }
    return new DateTime(lastRefresh);
  }

  public void setPhotoAddress(String absolutePath) {
    SharedPreferences pref = getPhotoAddressPref();
    pref.edit().putString(LAST_PHOTO_ADDRESS, absolutePath).commit();
  }

  public void clearPhotoAddress() {
    SharedPreferences pref = getPhotoAddressPref();
    pref.edit().clear().commit();
  }

  private SharedPreferences getPhotoAddressPref() {
    SharedPreferences pref = context.getSharedPreferences(PHOTO_ADDRESS, Context.MODE_PRIVATE);
    return pref;
  }

  public String getPhotoAddress() {
    SharedPreferences pref = getPhotoAddressPref();
    return pref.getString(LAST_PHOTO_ADDRESS, null);
  }

  public long getNextServerCommunicationServiceAlarmTime() {
    return getAppPrefs().getLong(NEXT_SERVER_COMM_REFRESH_PREFERENCE_KEY, new DateTime().minusHours(12).getMillis());
  }

  public void setNextServerCommunicationServiceAlarmTime(Long updateTime) {
    getAppPrefs().edit().putLong(NEXT_SERVER_COMM_REFRESH_PREFERENCE_KEY, updateTime).commit();
  }

  public void saveSelectedAccount(String name) {
    SharedPreferences prefs = context.getSharedPreferences(SELECTED_ACCOUNT_PREF, Context.MODE_PRIVATE);
    prefs.edit().putString(SELECTED_ACCOUNT_KEY, name).commit();
    deleteAccountCookie();
  }

  private void deleteAccountCookie() {
    SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
    preferences.edit().remove(COOKIE_PREFERENCE_KEY).commit();
  }

  public void saveSelectedAccount(Account account) {
    saveSelectedAccount(account.name);
  }

  public String getSelectedAccount() {
    SharedPreferences prefs = context.getSharedPreferences(SELECTED_ACCOUNT_PREF, Context.MODE_PRIVATE);
    return prefs.getString(SELECTED_ACCOUNT_KEY, null);
  }

  public void setRingtone(String ringtoneUri) {
    getAppPrefs().edit().putString(RINGTONE_KEY, ringtoneUri).commit();
  }

  public String getRingtone() {
    return getAppPrefs().getString(RINGTONE_KEY, null);
  }

  public boolean clearRingtone() {
    return getAppPrefs().edit().clear().commit();
  }

  public boolean hasInstalledPacoBarkRingtone() {
    return getAppPrefs().getBoolean(RINGTONE_INSTALLED_KEY, false);
  }

  public void setPacoBarkRingtoneInstalled() {
    getAppPrefs().edit().putBoolean(RINGTONE_INSTALLED_KEY, true).commit();
  }

  public DateTime getRecentlyTriggeredTime(long experimentId) {
    String storedTime = getAppPrefs().getString(EXPERIMENT_TRIGGERED_KEY + "_" + experimentId, null);
    if (storedTime == null) {
      return null;
    }
    return TimeUtil.parseDateTime(storedTime);
  }

  public void setRecentlyTriggeredTime(long experimentId, DateTime time) {
    getAppPrefs().edit().putString(EXPERIMENT_TRIGGERED_KEY + "_" + experimentId, TimeUtil.formatDateTime(time)).commit();
  }

}

