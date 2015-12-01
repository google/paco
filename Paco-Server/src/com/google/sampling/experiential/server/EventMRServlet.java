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
package com.google.sampling.experiential.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;

import com.google.appengine.api.users.User;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.PhotoBlob;
import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.model2.JsonConverter;

/**
 * Servlet that answers queries for Events.
 *
 * @author Bob Evans
 *
 */
public class EventMRServlet extends HttpServlet {

  public static final Logger log = Logger.getLogger(EventMRServlet.class.getName());
  private String defaultAdmin = "bobevans@google.com";
  private List<String> adminUsers = Lists.newArrayList(defaultAdmin);

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    setCharacterEncoding(req, resp);
    User user = AuthUtil.getWhoFromLogin();
    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      String anonStr = req.getParameter("anon");
      boolean anon = false;
      if (anonStr != null) {
        anon = Boolean.parseBoolean(anonStr);
      }
      String includePhotosParam = req.getParameter("includePhotos");
      boolean includePhotos = false;
      if (includePhotosParam != null) {
        includePhotos = Boolean.parseBoolean(includePhotosParam);
      }
      boolean cmdline = req.getParameter("cmdline") != null;
      String cursor = req.getParameter("cursor");
      String limitStr = req.getParameter("limit");
      int limit = 0;
      if (!Strings.isNullOrEmpty(limitStr)) {
          try {
            limit = Integer.parseInt(limitStr);
          } catch (NumberFormatException e) {
            e.printStackTrace();
          }
      }

      dumpEventsCSVMR(resp,req);
//      if (req.getParameter("mapping") != null) {
//        dumpUserIdMapping(req, resp, limit, cursor);
//      } else if (req.getParameter("json") != null) {
//        resp.setContentType("application/json;charset=UTF-8");
//        dumpEventsJson(resp, req, anon, includePhotos, limit, cursor, cmdline);
//      } else if (req.getParameter("photozip") != null) {
//        dumpPhotosZip(resp, req, anon, limit, cursor, cmdline);
//      } else if (req.getParameter("csv") != null) {
//        dumpEventsCSV(resp, req, anon, limit, cursor, cmdline);
//      } else {
//        dumpEventsHtml(resp, req, anon, limit, cursor, cmdline);
//      }
    }
  }

  private void dumpEventsCSVMR(HttpServletResponse resp, HttpServletRequest req) throws IOException {
    List<com.google.sampling.experiential.server.Query> query = new QueryParser().parse(stripQuotes(HttpUtil.getParam(req, "q")));
    EventQueryResultPair eventQueryPair = getEventsWithQuery(req, query);
    List<Event> events = eventQueryPair.getEvents();
    EventRetriever.sortEvents(events);

    String jsonOutput = jsonifyEvents(eventQueryPair, false, TimeUtil.getTimeZoneForClient(req).getID(), false);
    resp.getWriter().println(jsonOutput);
  }

  private String jsonifyEvents(EventQueryResultPair eventQueryPair, boolean anon, String timezoneId, boolean includePhotos) {
    ObjectMapper mapper = JsonConverter.getObjectMapper();

    try {
      List<EventDAO> eventDAOs = Lists.newArrayList();
      for (Event event : eventQueryPair.getEvents()) {
        String userId = event.getWho();
        if (anon) {
          userId = Event.getAnonymousId(userId);
        }
        DateTime responseDateTime = event.getResponseTimeWithTimeZone(timezoneId);
        Date responseTime = null;
        if (responseDateTime != null) {
          responseTime = responseDateTime.toGregorianCalendar().getTime();
        }
        DateTime scheduledDateTime = event.getScheduledTimeWithTimeZone(timezoneId);
        Date scheduledTime = null;
        if (scheduledDateTime != null) {
          scheduledTime = scheduledDateTime.toDate();
        }
        final Map<String, String> whatMap = event.getWhatMap();
        List<PhotoBlob> photos = event.getBlobs();
        String[] photoBlobs = null;
        if (includePhotos && photos != null && photos.size() > 0) {

          photoBlobs = new String[photos.size()];

          Map<String, PhotoBlob> photoByNames = Maps.newConcurrentMap();
          for (PhotoBlob photoBlob : photos) {
            photoByNames.put(photoBlob.getName(), photoBlob);
          }
          for(String key : whatMap.keySet()) {
            String value = null;
            if (photoByNames.containsKey(key)) {
              byte[] photoData = photoByNames.get(key).getValue();
              if (photoData != null && photoData.length > 0) {
                String photoString = new String(Base64.encodeBase64(photoData));
                if (!photoString.equals("==")) {
                  value = photoString;
                } else {
                  value = "";
                }
              } else {
                value = "";
              }
              whatMap.put(key, value);
            }
          }
        }

        eventDAOs.add(new EventDAO(userId,
                                   event.getWhen(),
                                   event.getExperimentName(),
                                   event.getLat(), event.getLon(),
                                   event.getAppId(),
                                   event.getPacoVersion(),
                                   whatMap,
                                   event.isShared(),
                                   responseTime,
                                   scheduledTime,
                                   null,
                                   Long.parseLong(event.getExperimentId()),
                                   event.getExperimentVersion(),
                                   event.getTimeZone(),
                                   event.getExperimentGroupName(),
                                   event.getActionTriggerId(),
                                   event.getActionTriggerSpecId(),
                                   event.getActionId()));
      }
      EventDAOQueryResultPair eventDaoQueryResultPair = new EventDAOQueryResultPair(eventDAOs, eventQueryPair.getCursor());
      return mapper.writeValueAsString(eventDaoQueryResultPair);
    } catch (JsonGenerationException e) {
      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return "Error could not retrieve events as json";
  }

  private String stripQuotes(String parameter) {
    if (parameter == null) {
      return null;
    }
    if (parameter.startsWith("'") || parameter.startsWith("\"")) {
      parameter = parameter.substring(1);
    }
    if (parameter.endsWith("'") || parameter.endsWith("\"")) {
      parameter = parameter.substring(0, parameter.length() - 1);
    }
    return parameter;
  }

  private EventQueryResultPair getEventsWithQuery(HttpServletRequest req,
                                         List<com.google.sampling.experiential.server.Query> queries) {
    User whoFromLogin = AuthUtil.getWhoFromLogin();
    //return EventRetriever.getInstance().getEventsUntilExhausted(queries, whoFromLogin.getEmail().toLowerCase(), TimeUtil.getTimeZoneForClient(req));
    return null;
  }

  private void setCharacterEncoding(HttpServletRequest req, HttpServletResponse resp)
      throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }
}
