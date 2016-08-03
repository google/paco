package com.pacoapp.paco.js.bridge;

import java.io.IOException;
import java.io.Serializable;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.webkit.JavascriptInterface;

import com.google.common.base.Strings;
import com.pacoapp.paco.shared.model2.JsonConverter;

public class JavascriptLocationManager {

  static class LocationJson implements Serializable{

    public static LocationJson createWithLocation(Location lastKnownLocation) {
      String provider = lastKnownLocation.getProvider();
      double latitude = lastKnownLocation.getLatitude();
      double longitude = lastKnownLocation.getLongitude();
      float accuracy = lastKnownLocation.getAccuracy();
      long time = lastKnownLocation.getTime();
      return new LocationJson(latitude, longitude, time, provider, accuracy);
    }

    private double latitude;
    private double longitude;
    private long time;
    private String provider;
    private float accuracy;

    public LocationJson() {

    }

    public LocationJson(double latitude, double longitude) {
      this.latitude = latitude;
      this.longitude = longitude;
    }

    public LocationJson(double latitude, double longitude, long time, String provider, float accuracy) {
      this(latitude, longitude);
      this.time = time;
      this.provider = provider;
      this.accuracy = accuracy;
    }

    public double getLatitude() {
      return latitude;
    }

    public void setLatitude(double latitude) {
      this.latitude = latitude;
    }

    public double getLongitude() {
      return longitude;
    }

    public void setLongitude(double longitude) {
      this.longitude = longitude;
    }

    public long getTime() {
      return time;
    }

    public void setTime(long time) {
      this.time = time;
    }

    public String getProvider() {
      return provider;
    }

    public void setProvider(String provider) {
      this.provider = provider;
    }

    public float getAccuracy() {
      return accuracy;
    }

    public void setAccuracy(float accuracy) {
      this.accuracy = accuracy;
    }



  }

  static class Distance implements Serializable{
    private String error;
    private float distance;

    Distance(String error) {
      this.error = error;
    }

    Distance(float distance) {
      this.distance = distance;
    }

    public String getError() {
      return error;
    }

    public void setError(String error) {
      this.error = error;
    }

    public float getDistance() {
      return distance;
    }

    public void setDistance(float distance) {
      this.distance = distance;
    }


  }

  private Context context;
  private LocationManager locationManager;

  public JavascriptLocationManager(Context context) {
    this.context = context;
    locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
  }

  /**
   * Get last known location
   */
  @JavascriptInterface
  public String getLastKnownLocation() {
    Location lastKnownLocation = getLastKnownLocationFromLocManager();
    LocationJson jsonLocation = LocationJson.createWithLocation(lastKnownLocation);
    if (lastKnownLocation != null) {
      return toJson(jsonLocation);
    }
    return null;
  }

  private Location getLastKnownLocationFromLocManager() {
    String locationProvider = LocationManager.NETWORK_PROVIDER;
    Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
    return lastKnownLocation;
  }

  /**
   * takes an string of a json object with format { "latitude" : 1, "longitude" : 2 }"
   * @param targetLocationString
   * @return
   */
  @JavascriptInterface
  public String getDistanceFrom(String targetLocationString) {
    if (Strings.isNullOrEmpty(targetLocationString)) {
      return null;
    }
    ObjectMapper mapper = JsonConverter.getObjectMapper();
    try {
      LocationJson targetLocationJson = mapper.readValue(targetLocationString, LocationJson.class);

      if (targetLocationJson != null) {
        Location targetLocationAndroid = convertToAndroidLocation(targetLocationJson);
        Location currentLocation = getLastKnownLocationFromLocManager();
        if (currentLocation != null) {
          final Distance distance = new Distance(currentLocation.distanceTo(targetLocationAndroid));
          return mapper.writeValueAsString(distance);
        }
      }
    } catch (JsonParseException e) {
      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      return mapper.writeValueAsString(new Distance("Unknown distance"));
    } catch (JsonGenerationException e) {
      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return "{ \"error\" : \"could not complete distance call\"}";
  }

  private Location convertToAndroidLocation(LocationJson targetLocationJson) {
    Location targetLocationAndroid = new Location(LocationManager.NETWORK_PROVIDER);
    targetLocationAndroid.setLatitude(targetLocationJson.latitude);
    targetLocationAndroid.setLongitude(targetLocationJson.longitude);
    return targetLocationAndroid;
  }

  public String toJson(LocationJson jsonLocation) {
    ObjectMapper mapper = JsonConverter.getObjectMapper();
    String json = null;
    try {
      json = mapper.writeValueAsString(jsonLocation);
    } catch (JsonGenerationException e) {
      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return json;
  }

}
