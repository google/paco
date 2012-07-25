/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.sampling.experiential.server;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.joda.time.DateTimeZone;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.model.PhotoBlob;
import com.google.sampling.experiential.shared.Event;

/**
 * Retrieve Event objects from the datastore.
 *
 * @author Bob Evans
 *
 */
public class EventRetriever {

  private static EventRetriever instance;
  private static final Logger log = Logger.getLogger(EventRetriever.class.getName());

  private EventRetriever() {}

  public static synchronized EventRetriever getInstance() {
    if (instance == null) {
      instance = new EventRetriever();
    }
    return instance;
  }

  public void postEvent(String who,
      String lat,
      String lon,
      Date whenDate,
      String appId,
      String pacoVersion,
      Map<String, String> what,
      boolean shared,
      String experimentId,
      String experimentName,
      Date responseTime,
      Date scheduledTime,
      List<PhotoBlob> blobs) {
    long t1 = System.currentTimeMillis();

    List<String> photoBlobs = Lists.newArrayList();

    if (blobs != null) {
      for (PhotoBlob photoBlob : blobs) {
        photoBlobs.add(DAO.getInstance().createPhotoBlob(photoBlob).toString());
      }
    }


    Event event = new Event(who,
        lat,
        lon,
        whenDate,
        appId,
        pacoVersion,
        what,
        shared,
        experimentId,
        experimentName,
        responseTime,
        scheduledTime,
        photoBlobs);

    DAO.getInstance().createEvent(event);

    long t2 = System.currentTimeMillis();
    log.info("POST Event time: " + (t2 - t1));
  }

  @SuppressWarnings("unchecked")
  public List<Event> getEvents(String loggedInUser) {
    return DAO.getInstance().getSubjectEvents(loggedInUser);
  }

  public List<Event> getEvents(String loggedInUser,
      List<com.google.sampling.experiential.server.Query> queryFilters, DateTimeZone clientTimeZone) {
    // FIXME: Implement logic here.
    System.out.println("queryFilters = " + queryFilters.toString());
    return null;
  }

}
