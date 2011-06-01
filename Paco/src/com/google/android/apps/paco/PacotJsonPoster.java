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


import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class PacotJsonPoster {


  private Context context;

  public PacotJsonPoster(Context context) {
    this.context = context;
  }

  String saveForm(String formJson) {
    StringBuilder jsonBuf = new StringBuilder();
    jsonBuf.append("{ appId : ");
    jsonBuf.append(PacoConstants.APP_ID);
    jsonBuf.append(", who: \"");
    jsonBuf.append("unused");
    jsonBuf.append("\", ");
    jsonBuf.append("when : \"");
    jsonBuf.append(getDateString());
    jsonBuf.append("\", ");
    jsonBuf.append(getLocationJson());
    jsonBuf.append("what : { ");
    jsonBuf.append(formJson);
    jsonBuf.append("} }");
    return jsonBuf.toString();
  }

  private String getLocationJson() {
    Double lat = null;
    Double lon = null;
    Location location = getCurrentLocation();
    if (location != null) {
      lat = location.getLatitude();
      lon = location.getLongitude();
    }
    if (lat != null && lon != null) {
      return "where : { lat : \"" + lat + "\", lon : \"" + lon + "\" }, ";
    }
    return "";
  }

  public Location getCurrentLocation() {
    LocationManager locationManager =
        (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    if (locationManager == null) {
      return null;
    }
    final long maxAgeMilliSeconds = 1000 * 60 * 1; // 1 minute
    final long maxAgeNetworkMilliSeconds = 1000 * 60 * 10; // 10 minutes
    final long now = System.currentTimeMillis();
    Location loc = locationManager.getLastKnownLocation("gps");
    if (loc == null || loc.getTime() < now - maxAgeMilliSeconds) {
      // We don't have a recent GPS fix, just use cell towers if available
      loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
      if (loc == null || loc.getTime() < now - maxAgeNetworkMilliSeconds) {
        // We don't have a recent cell tower location, let the user know:
        // Toast.makeText(this, "Location is stale. No towers around.",
        // Toast.LENGTH_LONG).show();
        Log.i("paco_android", "Location is stale. No towers around.");
        return loc;
      } else {
        Log.i("paco_android", "Using cell towers and WIFI, which are less accurate.");
      }
    }
    return loc;
  }

  private String getDateString() {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd:HH:mm:ssZ");
    return df.format(Calendar.getInstance().getTime());
  }

  public static void addKeyValue(String key, String value, StringBuilder buf) {
    if (!isEmpty(value)) {
      buf.append(", " + key + " : \"");
      buf.append(value);
      buf.append("\"");
    }
  }

  public static boolean isEmpty(String name) {
    return name == null || name.length() == 0;
  }



}
