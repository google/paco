package com.google.sampling.experiential.client;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;

public class TimeUtil {

  public static final String ZONE_FORMAT = "Z";

  public static String getTimezone() {
    return DateTimeFormat.getFormat(TimeUtil.ZONE_FORMAT).format(new Date());
  }

}
