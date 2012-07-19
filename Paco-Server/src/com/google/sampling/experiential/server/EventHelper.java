// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import com.google.sampling.experiential.shared.Event;
import com.google.sampling.experiential.shared.TimeUtil;

import org.apache.commons.codec.binary.Hex;
import org.mortbay.log.Log;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class EventHelper {
  public static final String SALT = "zyzzyfoo";

  public static String[] toCSV(Event event, List<String> columnNames, boolean anon) {
    java.text.SimpleDateFormat simpleDateFormat =
      new java.text.SimpleDateFormat(TimeUtil.DATETIME_FORMAT);

    int csvIndex = 0;
    String[] parts = new String[10 + columnNames.size()];
    if (anon) {
      parts[csvIndex++] = EventHelper.getAnonymousId(event.getWho() + SALT);
    } else {
      parts[csvIndex++] = event.getWho();
    }
    parts[csvIndex++] = simpleDateFormat.format(event.getWhen());
    parts[csvIndex++] = event.getLat();
    parts[csvIndex++] = event.getLon();
    parts[csvIndex++] = event.getAppId();
    parts[csvIndex++] = event.getPacoVersion();
    parts[csvIndex++] = event.getExperimentId();
    parts[csvIndex++] = event.getExperimentName();
    parts[csvIndex++] = event.getResponseTime() != null ? simpleDateFormat.format(event.getResponseTime()) : null;
    parts[csvIndex++] = event.getScheduledTime() != null ? simpleDateFormat.format(event.getScheduledTime()) : null;
    Map<String, String> whatMap = event.getWhatMap();
    for (String key : columnNames) {
      String value = whatMap.get(key);
      parts[csvIndex++] = value;
    }
    return parts;
  }

  public static String getAnonymousId(String who) {
    MessageDigest messageDigest = null;
    try {
      messageDigest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      Log.info("Could not get MD5 algorithm");
      return null;
    }
    messageDigest.reset();
    messageDigest.update(who.getBytes(Charset.forName("UTF8")));
    byte[] resultByte = messageDigest.digest();
    return new String(Hex.encodeHex(resultByte));
  }
}
