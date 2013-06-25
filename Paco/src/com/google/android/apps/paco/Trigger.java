package com.google.android.apps.paco;

import java.util.HashMap;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;

public class Trigger extends SignalingMechanism implements Parcelable{

  public static final int HANGUP = 1;
  public static final int USER_PRESENT = 2;
  public static final int PACO_ACTION_EVENT = 3;
  
  public static final Map<Integer, String> EVENT_NAMES;
  static {
    EVENT_NAMES = new HashMap<Integer, String>();
    EVENT_NAMES.put(HANGUP, "Phone Hangup");
    EVENT_NAMES.put(USER_PRESENT, "User Present");
    EVENT_NAMES.put(PACO_ACTION_EVENT, "Paco Action");
  }
  
  
  private int eventCode;
  private long delay;
  private String sourceIdentifier;

  public int getEventCode() {
    return eventCode;
  }

  public void setEventCode(int code) {
    this.eventCode = code;
  }

  public Trigger(int eventCode) {
    this.eventCode = eventCode;
  }
  
  public Trigger() {
  }

  public boolean match(int event, String sourceIdentifier) {
    return event == eventCode && (eventCode != PACO_ACTION_EVENT || sourceIdentifier.equals(this.sourceIdentifier));
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(eventCode);
    dest.writeString(sourceIdentifier);
    dest.writeLong(delay);
    dest.writeInt(timeout);
  }

  public static class Creator implements Parcelable.Creator<Trigger> {

    public Trigger createFromParcel(Parcel source) {
      Trigger trigger = new Trigger();
      trigger.eventCode = source.readInt();
      trigger.sourceIdentifier = source.readString();
      trigger.delay = source.readLong();
      trigger.timeout = source.readInt();
      return trigger;
    }

    public Trigger[] newArray(int size) {
      return new Trigger[size];
    }
  }
  
  public static final Creator CREATOR = new Creator();
  

  public long getDelay() {
    return delay;
  }

  public static String getNameForCode(int code2) {
    return EVENT_NAMES.get(code2);
  }

  @Override
  public String toString() {
    return "Trigger: event: " + Trigger.getNameForCode(this.eventCode) + ", delay = " + Long.toString(delay);
  }

  public String getSourceIdentifier() {
    return sourceIdentifier;
  }

  public void setSourceIdentifier(String sourceIdentifier) {
    this.sourceIdentifier = sourceIdentifier;
  }
  
  

}
