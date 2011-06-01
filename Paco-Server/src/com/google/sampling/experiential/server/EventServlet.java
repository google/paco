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


import au.com.bytecode.opencsv.CSVWriter;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.model.Input;
import com.google.sampling.experiential.model.PhotoBlob;
import com.google.sampling.experiential.model.What;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.InputDAO;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that answers queries for Events.
 * 
 * @author Bob Evans
 *
 */
public class EventServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(EventServlet.class.getName());
  static final String DEV_HOST = "localhost:8080";
  private DateTimeFormatter jodaFormatter = DateTimeFormat.forPattern("yyyyMMdd:HH:mm:ssZ");
  private String defaultAdmin = "bobevans@google.com";
  private List<String> adminUsers =
      Lists.newArrayList(defaultAdmin);


  private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // TODO(bobevans): Add security check
    String isGetMethodPosting = req.getParameter("autopost");
    if (isGetMethodPosting != null && !isGetMethodPosting.isEmpty()) {
      postEventFromGet(req, resp);
    } else if (req.getParameter("json") != null) {
      dumpEventsJson(resp, req);
    } else if (req.getParameter("csv") != null) {
      dumpEventsCSV(resp, req);
    } else {
      showEvents(req, resp);
    }
  }

  public static DateTimeZone getTimeZoneForClient(HttpServletRequest req) {
    String tzStr = req.getParameter("tz");
    if (tzStr != null && !tzStr.isEmpty()) {
      DateTimeZone jodaTimeZone = DateTimeZone.forID(tzStr);
      return jodaTimeZone;
    } else {
      Locale clientLocale = req.getLocale();
      Calendar calendar = Calendar.getInstance(clientLocale);
      TimeZone clientTimeZone = calendar.getTimeZone();
      DateTimeZone jodaTimeZone = DateTimeZone.forTimeZone(clientTimeZone);
      return jodaTimeZone;
    }
  }

  private void doGeistNowQueryHtml(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    // TODO(bobevans):this is duplicate code. refactor
    long start = System.currentTimeMillis();
    DateTimeZone jodaTimeZone = getTimeZoneForClient(req);
    DateTime todayTime = new DateTime(jodaTimeZone);
    DateTimeFormatter df = DateTimeFormat.forPattern("yyyyMMdd");

    String today = df.print(todayTime);
    String sevenDaysAgo = df.print(todayTime.minusDays(6));
    List<com.google.sampling.experiential.server.Query> query =
        new QueryParser().parse("survey=geistnow:date_range=" + sevenDaysAgo + "-" + today);
    List<Event> events =
        EventRetriever.getInstance().getEvents(query, defaultAdmin, jodaTimeZone);
    sortEvents(events);
    String[] charts = buildChartUrls(events, jodaTimeZone);

    String passionChartUrl, productivityChartUrl, processChartUrl, energyChartUrl, barrierChartUrl, 
      workteamChartUrl, stakeholdersChartUrl;
    productivityChartUrl = charts[0];
    passionChartUrl = charts[1];
    processChartUrl = charts[2];
    energyChartUrl = charts[3];
    workteamChartUrl = charts[4];
    stakeholdersChartUrl = charts[5];

    String april1 = df.print(todayTime.minusDays(30));
    List<com.google.sampling.experiential.server.Query> obstaclesBarriersQuery =
        new QueryParser().parse("survey=geistnow:date_range=" + april1 + "-" + today);
    List<Event> aprilToNowEvents =
        EventRetriever.getInstance().getEvents(obstaclesBarriersQuery, defaultAdmin,
            jodaTimeZone);
    sortEvents(aprilToNowEvents);
    barrierChartUrl = buildBarrierChartUrl(aprilToNowEvents);
    List<String> obstacles = buildObstaclesTopList(aprilToNowEvents);
    List<String> moods = buildMoodsTopList(aprilToNowEvents);

    StringBuilder out = new StringBuilder();
    out.append("<html><title>GeistNow Results</title><body><h1>GeistNow Results</h1>");

    out.append("<img src=\"");
    out.append(productivityChartUrl);
    out.append("\">,<br/>");

    out.append("<img src=\"");
    out.append(passionChartUrl);
    out.append("\">,<br/>");

    out.append("<img src=\"");
    out.append(processChartUrl);
    out.append("\">,<br/>");

    out.append("<img src=\"");
    out.append(energyChartUrl);
    out.append("\">,<br/>");

    out.append("<img src=\"");
    out.append(barrierChartUrl);
    out.append("\">,<br/>");

    out.append("<img src=\"");
    out.append(workteamChartUrl);
    out.append("\">,<br/>");

    out.append("<img src=\"");
    out.append(stakeholdersChartUrl);
    out.append("\">,<br/>");

    out.append("<br/><hr><br/><h3>Moods</h3>");
    out.append(Joiner.on("<br/>").join(moods));
    out.append("<hr>");

    out.append("<h3>Obstacles</h3>");
    out.append(Joiner.on("<br/>").join(obstacles));
    out.append("</body></html>");

    resp.getWriter().println(out.toString());
    log.info("doGeistNow HTML time: " + (System.currentTimeMillis() - start));
  }

  static class ObstacleRank implements Comparable<ObstacleRank> {
    public int count = 1;
    public String obstacle;

    public ObstacleRank(String obstacle) {
      this.obstacle = obstacle;
    }

    @Override
    public int compareTo(ObstacleRank o) {
      return ((Integer) o.count).compareTo(count);
    }
  }

  private List<String> buildObstaclesTopList(List<Event> events) {
    Map<String, ObstacleRank> obstacles = Maps.newHashMap();
    for (Event event : events) {
      String whatByKey = event.getWhatByKey("problem");
      if (whatByKey == null || whatByKey.length() == 0) {
        continue;
      }
      ObstacleRank rank = obstacles.get(whatByKey);
      if (rank == null) {
        rank = new ObstacleRank(whatByKey);
        obstacles.put(whatByKey, rank);
      } else {
        rank.count++;
      }

    }

    List<ObstacleRank> values = Lists.newArrayList(obstacles.values());
    Collections.sort(values);
    ArrayList<String> jsonObstacles = Lists.newArrayList();
    for (ObstacleRank obstacle : values) {
      jsonObstacles.add(quote(obstacle.obstacle));
    }
    return jsonObstacles; // .subList(0, Math.min(30,
                          // obstacles.keySet().size()));
  }

  private List<String> buildMoodsTopList(List<Event> events) {
    Map<String, ObstacleRank> obstacles = Maps.newHashMap();
    for (Event event : events) {
      String whatByKey = event.getWhatByKey("mood");
      if (whatByKey == null || whatByKey.length() == 0) {
        continue;
      }
      ObstacleRank rank = obstacles.get(whatByKey);
      if (rank == null) {
        rank = new ObstacleRank(whatByKey);
        obstacles.put(whatByKey, rank);
      } else {
        rank.count++;
      }

    }

    List<ObstacleRank> values = Lists.newArrayList(obstacles.values());
    Collections.sort(values);
    ArrayList<String> jsonObstacles = Lists.newArrayList();
    for (ObstacleRank obstacle : values) {
      jsonObstacles.add(quote(obstacle.obstacle));
    }
    return jsonObstacles; // .subList(0, Math.min(30,
                          // obstacles.keySet().size()));
  }

  private String quote(String whatByKey) {
    return "\"" + whatByKey + "\"";
  }

  private String buildBarrierChartUrl(List<Event> events) {
    int[] barrierCountsGoogler = new int[4];
    int[] barrierCountsSubject = new int[4];

    String who = getWhoFromLogin().getEmail();

    for (Event event : events) {
      String questionSet = event.getWhatByKey("questionSet");
      if (questionSet == null || (!questionSet.equals("2"))) {
        continue;
      }

      String reportTimeString = event.getWhatByKey("reportTime");
      if (reportTimeString == null) {
        continue;
      }

      String barrier = event.getWhatByKey("barrier");
      int barrierIndex = -1;
      if (barrier != null) {
        barrier = barrier.substring(0, 5);
        if (barrier.equals("Physi")) {
          barrierIndex = 0;
        } else if (barrier.equals("Emoti")) {
          barrierIndex = 1;
        } else if (barrier.equals("Focus")) {
          barrierIndex = 2;
        } else if (barrier.equals("Align")) {
          barrierIndex = 3;
        }
      }
      if (who.equals(event.getWho())) {
        if (barrierIndex != -1) {
          barrierCountsSubject[barrierIndex] += 1;
        }
      }
      if (barrierIndex != -1) {
        barrierCountsGoogler[barrierIndex] += 1;
      }
    }

    // by category barrier instead of by day of the week in last 7 days
    int[] avgBarriersGoogler = makePercentages(barrierCountsGoogler);
    int[] avgBarriersSubject = makePercentages(barrierCountsSubject);
    return buildBarChartUrlForBarriers(avgBarriersGoogler, avgBarriersSubject,
        "Barriers%20%28last%2030%20days%29");

  }

  private String[] buildChartUrls(List<Event> events, DateTimeZone clientTimeZone) {
    int durationOfQueryInDays = 7;
    int[] productivityCountsGoogler = new int[durationOfQueryInDays];
    int[] productivityCountsSubject = new int[durationOfQueryInDays];
    int[] prodCntG = new int[durationOfQueryInDays];
    int[] prodCntS = new int[durationOfQueryInDays];

    int[] passionCountsGoogler = new int[durationOfQueryInDays];
    int[] passionCountsSubject = new int[durationOfQueryInDays];
    int[] passCntG = new int[durationOfQueryInDays];
    int[] passCntS = new int[durationOfQueryInDays];

    int[] processCountsGoogler = new int[durationOfQueryInDays];
    int[] processCountsSubject = new int[durationOfQueryInDays];
    int[] processCntG = new int[durationOfQueryInDays];
    int[] processCntS = new int[durationOfQueryInDays];

    int[] energyCountsGoogler = new int[durationOfQueryInDays];
    int[] energyCountsSubject = new int[durationOfQueryInDays];
    int[] energyCntG = new int[durationOfQueryInDays];
    int[] energyCntS = new int[durationOfQueryInDays];

    // int[] barrierCountsGoogler = new int[4];
    // int[] barrierCountsSubject = new int[4];

    int[] workteamCountsGoogler = new int[durationOfQueryInDays];
    int[] workteamCountsSubject = new int[durationOfQueryInDays];
    int[] workteamCntG = new int[durationOfQueryInDays];
    int[] workteamCntS = new int[durationOfQueryInDays];

    int[] stakeholdersCountsGoogler = new int[durationOfQueryInDays];
    int[] stakeholdersCountsSubject = new int[durationOfQueryInDays];
    int[] stakeholdersCntG = new int[durationOfQueryInDays];
    int[] stakeholdersCntS = new int[durationOfQueryInDays];



    String who = getWhoFromLogin().getEmail();
    DateTime today = new DateTime(clientTimeZone);
    DateMidnight todayMidnight = today.toDateMidnight();
    for (Event event : events) {
      String reportTimeString = event.getWhatByKey("reportTime");
      if (reportTimeString == null) {
        continue;
      }
      DateTime reportTime;
      try {
        reportTime = jodaFormatter.withZone(clientTimeZone).parseDateTime(reportTimeString);
      } catch (IllegalArgumentException e) {
        continue;
      }

      int daysBetween = new Period(reportTime.toDateMidnight(), todayMidnight).getDays();
      int arrayIndexForDay = durationOfQueryInDays - daysBetween - 1;

      String questionSet = event.getWhatByKey("questionSet");
      if (questionSet == null) {
        questionSet = "1";
      }
      if (questionSet.equals("1")) {
        String passionStr = event.getWhatByKey("passion");
        if (passionStr == null) {
          continue;
        }
        Integer passion = Integer.parseInt(passionStr);

        String productivityStr = event.getWhatByKey("productivity");
        if (productivityStr == null) {
          continue;
        }
        int productivity = Integer.parseInt(productivityStr);

        // index the personal values
        if (who.equals(event.getWho())) {
          productivityCountsSubject[arrayIndexForDay] += productivity;
          prodCntS[arrayIndexForDay] += 1;
          passionCountsSubject[arrayIndexForDay] += passion;
          passCntS[arrayIndexForDay] += 1;
        }
        // compute the global values
        productivityCountsGoogler[arrayIndexForDay] += productivity;
        prodCntG[arrayIndexForDay] += 1;
        passionCountsGoogler[arrayIndexForDay] += passion;
        passCntG[arrayIndexForDay] += 1;

      } else if (questionSet.equals("2")) {
        String processStr = event.getWhatByKey("progress");
        if (processStr == null) {
          continue;
        }
        int process = Integer.parseInt(processStr);

        String energyStr = event.getWhatByKey("energized");
        if (energyStr == null) {
          continue;
        }
        int energy = Integer.parseInt(energyStr);

        // String barrier = event.getWhatByKey("barrier");
        // int barrierIndex = -1;
        // if (barrier != null) {
        // barrier = barrier.substring(0,5);
        // if (barrier.equals("Physi")) {
        // barrierIndex = 0;
        // } else if (barrier.equals("Emoti")) {
        // barrierIndex = 1;
        // } else if (barrier.equals("Focus")) {
        // barrierIndex = 2;
        // } else if (barrier.equals("Align")) {
        // barrierIndex = 3;
        // }
        // }

        // index the personal values
        if (who.equals(event.getWho())) {
          processCountsSubject[arrayIndexForDay] += process;
          processCntS[arrayIndexForDay] += 1;
          energyCountsSubject[arrayIndexForDay] += energy;
          energyCntS[arrayIndexForDay] += 1;
          // if (barrierIndex != - 1) {
          // barrierCountsSubject[barrierIndex] += 1;
          // }
        }
        // compute the global values
        processCountsGoogler[arrayIndexForDay] += process;
        processCntG[arrayIndexForDay] += 1;
        energyCountsGoogler[arrayIndexForDay] += energy;
        energyCntG[arrayIndexForDay] += 1;
        // if (barrierIndex != - 1) {
        // barrierCountsGoogler[barrierIndex] += 1;
        // }
      } else if (questionSet.equals("3")) {
        String workteamStr = event.getWhatByKey("in_zone");
        if (workteamStr == null) {
          continue;
        }
        int workteam = Integer.parseInt(workteamStr);

        String stakeholderStr = event.getWhatByKey("stakeholder_satisfaction");
        if (stakeholderStr == null) {
          continue;
        }
        int stakeholder = Integer.parseInt(stakeholderStr);

        // index the personal values
        if (who.equals(event.getWho())) {
          workteamCountsSubject[arrayIndexForDay] += workteam;
          workteamCntS[arrayIndexForDay] += 1;
          stakeholdersCountsSubject[arrayIndexForDay] += stakeholder;
          stakeholdersCntS[arrayIndexForDay] += 1;
        }
        // compute the global values
        workteamCountsGoogler[arrayIndexForDay] += workteam;
        workteamCntG[arrayIndexForDay] += 1;
        stakeholdersCountsGoogler[arrayIndexForDay] += stakeholder;
        stakeholdersCntG[arrayIndexForDay] += 1;
      }



    }

    int[] avgProductivityGoogler =
        avgZeros(scale(createAverages(productivityCountsGoogler, prodCntG)));
    int[] avgProductivitySubject =
        avgZeros(scale(createAverages(productivityCountsSubject, prodCntS)));

    int[] avgPassionGoogler = avgZeros(scale(createAverages(passionCountsGoogler, passCntG)));
    int[] avgPassionSubject = avgZeros(scale(createAverages(passionCountsSubject, passCntS)));

    int[] avgProcessGoogler = avgZeros(scale(createAverages(processCountsGoogler, processCntG)));
    int[] avgProcessSubject = avgZeros(scale(createAverages(processCountsSubject, processCntS)));

    int[] avgEnergyGoogler = avgZeros(scale(createAverages(energyCountsGoogler, energyCntG)));
    int[] avgEnergySubject = avgZeros(scale(createAverages(energyCountsSubject, energyCntS)));

    // by category barrier instead of by day of the week in last 7 days
    // int[] avgBarriersGoogler = makePercentages(barrierCountsGoogler);
    // int[] avgBarriersSubject = makePercentages(barrierCountsSubject);

    int[] avgWorkteamGoogler = avgZeros(scale(createAverages(workteamCountsGoogler, workteamCntG)));
    int[] avgWorkteamSubject = avgZeros(scale(createAverages(workteamCountsSubject, workteamCntS)));

    int[] avgStakeholderGoogler =
        avgZeros(scale(createAverages(stakeholdersCountsGoogler, stakeholdersCntG)));
    int[] avgStakeholderSubject =
        avgZeros(scale(createAverages(stakeholdersCountsSubject, stakeholdersCntS)));


    String prodChartUrl =
        buildChartUrl(avgProductivityGoogler, avgProductivitySubject, "Productivity");
    String passChartUrl = buildChartUrl(avgPassionGoogler, avgPassionSubject, "Passion");
    String processChartUrl = buildChartUrl(avgProcessGoogler, avgProcessSubject, "Progress");
    String energyChartUrl = buildChartUrl(avgEnergyGoogler, avgEnergySubject, "Energy%20Level");
    // String barrierChartUrl = buildBarChartUrlForBarriers(avgBarriersGoogler,
    // avgBarriersSubject, "Barriers%20%28since%20inception%29");
    String workteamChartUrl =
        buildChartUrl(avgWorkteamGoogler, avgWorkteamSubject, "Workteam%20Zone");
    String stakeholderChartUrl =
        buildChartUrl(avgStakeholderGoogler, avgStakeholderSubject, "Stakeholder%20Belief");
    return new String[] {prodChartUrl, 
        passChartUrl, 
        processChartUrl, 
        energyChartUrl, 
        /* barrierChartUrl, */
        workteamChartUrl, 
        stakeholderChartUrl};
  }

  private int[] makePercentages(int[] barrierCounts) {
    int[] percentagesByBarrier = {0, 0, 0, 0};
    int totalBarriers = 0;
    for (int i = 0; i < barrierCounts.length; i++) {
      totalBarriers += barrierCounts[i];
    }
    if (totalBarriers > 0) {
      for (int i = 0; i < percentagesByBarrier.length; i++) {
        int barrierCount = barrierCounts[i];
        if (barrierCount > 0) {
          float ratio = (float) barrierCount / (float) totalBarriers;
          percentagesByBarrier[i] = (int) (ratio * 100);
        }
      }
    }
    return percentagesByBarrier;
  }

  private int[] avgZeros(int[] scale) {
    int last = 0;
    for (int i = 0; i < scale.length; i++) {
      if (scale[i] == 0 && i != scale.length - 1) {
        scale[i] = last;
      }
      last = scale[i];
    }
    return scale;
  }

  private String buildChartUrl(int[] avgForGoogler, int[] avgForSubject, String title) {
    StringBuilder buf =
        new StringBuilder("http://chart.apis.google.com/chart?cht=lc");
    buf.append("&chs=370x165&chls=6|3");
    buf.append("&chd=t:");
    buf.append(joinInts(avgForSubject));
    buf.append("%7C");
    buf.append(joinInts(avgForGoogler));
    String spacer = "%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20" +
      "%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20" +
      "%20%20%20%20%20";
    String youSpacer = "%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20" +
      "%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20.";
    buf.append("&chxs=0,666666,14|1,666666,14");
    buf.append("&chxt=x,y");
    buf.append("&chxr=0,-6,0|1,0,5");
    buf.append("&chxl=0:|-6d|-5d|-4d|-3d|-2d|-1d|today");
    buf.append("&chtt=Last%20Seven%20Days%20");
    buf.append(title);
    buf.append("&chts=333333,18");
    buf.append("&chco=4d89f9,ffb642");
    buf.append("&chdl=You|Google");
    buf.append("&chdlp=r");
    buf.append("&chco=4d89f9,c6d9fd");
    buf.append("&chbh=r,0.5,1.5");
    return buf.toString();
  }

  private String buildBarChartUrlForBarriers(int[] avgForGoogler, 
      int[] avgForSubject, 
      String title) {
    StringBuilder buf = new StringBuilder("http://chart.apis.google.com/chart?cht=bhg");
    buf.append("&chs=370x165");
    buf.append("&chd=t:");
    buf.append(joinInts(avgForSubject));
    buf.append("%7C");
    buf.append(joinInts(avgForGoogler));
    buf.append("&chxt=x,y");
    buf.append("&chxl=0:|0|100%|1:|Alignment|Focus|Emotional|Physical");
    buf.append("&chxs=0N*px,333333,14|1,333333,16");
    buf.append("&chtt=");
    buf.append(title);
    buf.append("&chts=333333,18");
    buf.append("&chdl=You|Google");
    buf.append("&chco=4d89f9,c6d9fd");
    buf.append("&chbh=a,1,6");
    return buf.toString();
  }


  private int[] scale(float[] averages) {
    int[] scaledAvgs = new int[averages.length];
    for (int i = 0; i < averages.length; i++) {
      scaledAvgs[i] = (int) (averages[i] * 20.0);
    }
    return scaledAvgs;
  }

  private String joinInts(int[] avgProductivitySubject) {
    StringBuffer buf = new StringBuffer();
    boolean first = true;
    for (int i = 0; i < avgProductivitySubject.length; i++) {
      if (first) {
        first = false;
      } else {
        buf.append(",");
      }
      buf.append(avgProductivitySubject[i]);
    }
    return buf.toString();
  }

  private float[] createAverages(int[] totalsPerBin, int[] countPerBin) {
    int bins2 = totalsPerBin.length;
    float[] avgPerBin = new float[bins2];
    for (int i = 0; i < bins2; i++) {
      int cnt = countPerBin[i];
      if (cnt == 0) {
        avgPerBin[i] = 0;
      } else {
        avgPerBin[i] = (float) totalsPerBin[i] / (float) cnt;
      }
    }
    return avgPerBin;
  }


  private void postEventFromGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd:HH:mm:ssZ");
    
    String who = getWho(req);
    String lat = getParam(req, "lat");
    String lon = getParam(req, "lon");
    String whenString = getParam(req, "when");
    String appId = getParam(req, "appId");
    String pacoVersion = getParam(req, "pacoVersion");
    String sharedParam = getParam(req, "shared");
    String experimentId = getParam(req, "experimentId");
    String experimentName = getParam(req, "experimentName");
    Date responseTime = createDateFromString(df, getParam(req, "responseTime"));
    Date scheduledTime = createDateFromString(df, getParam(req, "scheduledTime"));
    boolean shared = sharedParam == null ? false : Boolean.getBoolean(sharedParam);

    Date whenDate = createDateFromString(df, whenString);
    Set<What> what = Sets.newHashSet();
    Enumeration names = req.getParameterNames();
    while (names != null && names.hasMoreElements()) {
      String name = (String) names.nextElement();
      if (name.startsWith("what.")) {
        String key = name.substring(5);
        String value = req.getParameter(name);
        what.add(new What(key, value));
      }
    }
    EventRetriever.getInstance().postEvent(who, lat, lon, whenDate, appId, pacoVersion, what,
        shared, experimentId, experimentName, responseTime, scheduledTime, null);
    resp.getWriter().println("<img src=\"/images/paco_sil.png\">" +
        "Paco says, 'Thank you for reviewing!'<br/>" +
        "<a href=\"http://quantifiedself.appspot.com/\">Home</a>");
  }

  private Date createDateFromString(SimpleDateFormat df, String whenString) {
    Date whenDate = null;
    if (whenString != null && !whenString.isEmpty()) {
      
      try {
        whenDate = df.parse(whenString);
      } catch (ParseException e) {
        whenDate = Calendar.getInstance().getTime();
      }
    } else {
      whenDate = Calendar.getInstance().getTime();
    }
    return whenDate;
  }

  /**
   * For posting purposes, in corp, you are the who param.
   * 
   * @param req
   * @return
   */
  private String getWho(HttpServletRequest req) {
    User loggedInUser = getWhoFromLogin();
    String loggedInWho = null;
    if (loggedInUser != null) {
      loggedInWho = loggedInUser.getEmail();
    }

    if (isDevInstance(req)) {
      String whoParam = getWhoFromParam(req);
      if (whoParam != null && !whoParam.isEmpty()) {
        return whoParam;
      } else if (loggedInWho != null && !loggedInWho.isEmpty()) {
        return loggedInWho;
      } else {
        throw new IllegalArgumentException("Must be logged in, or 'who' param must be supplied.");
      }
    } else {
      return loggedInWho;
    }
  }

  private String getWhoFromParam(HttpServletRequest req) {
    String who = getParam(req, "who");

    return who;
  }

  private boolean isDevInstance(HttpServletRequest req) {
    return DEV_HOST.equals(req.getHeader("Host"));
  }

  private User getWhoFromLogin() {
    UserService userService = UserServiceFactory.getUserService();
    return userService.getCurrentUser();
  }

  private String getParam(HttpServletRequest req, String whoParam) {
    try {
      String parameter = req.getParameter(whoParam);
      if (parameter == null || parameter.isEmpty()) {
        return null;
      }
      return URLDecoder.decode(parameter, "UTF-8");
    } catch (UnsupportedEncodingException e1) {
      throw new IllegalArgumentException("Unspported encoding");
    }
  }

  private void dumpEventsJson(HttpServletResponse resp, HttpServletRequest req) throws IOException {
    List<com.google.sampling.experiential.server.Query> query =
        new QueryParser().parse(stripQuotes(req.getParameter("q")));
    List<Event> events = getEventsWithQuery(req, query);
    sortEvents(events);
    String jsonOutput = jsonifyEvents(events);
    resp.getWriter().println(jsonOutput);
  }

  private String jsonifyEvents(List<Event> events) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);
    try {
      List<EventDAO> eventDAOs = Lists.newArrayList();
      for (Event event : events) {
        eventDAOs.add(new EventDAO(event.getWho(), event.getWhen(), event.getExperimentName(),
            event.getLat(), event.getLon(), event.getAppId(), event.getPacoVersion(),
            event.getWhatMap(),event.isShared(), event.getResponseTime(), event.getScheduledTime(),
            null));
      }
      return mapper.writeValueAsString(eventDAOs);      
    } catch (JsonGenerationException e) {
      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } 
    return "Error could not retrieve events as json";
  }
  
//  private String jsonifyEventsDead(List<Event> events) {
//    StringBuilder out = new StringBuilder();
//    out.append("{ \"results\" : [");
//    boolean first = true;
//    for (Event event : events) {
//      if (first) {
//        first = false;
//      } else {
//        out.append(", ");
//      }
//      out.append(event.toJson());
//    }
//    out.append("] }");
//    return out.toString();
//  }

  private void dumpEventsCSV(HttpServletResponse resp, HttpServletRequest req) throws IOException {
    List<com.google.sampling.experiential.server.Query> query =
        new QueryParser().parse(stripQuotes(req.getParameter("q")));

    String loggedInuser = getWhoFromLogin().getEmail();
    if (loggedInuser != null && adminUsers.contains(loggedInuser)) {
      loggedInuser = defaultAdmin;
    }
    List<Event> events =
        EventRetriever.getInstance().getEvents(query, loggedInuser, getTimeZoneForClient(req));
    sortEvents(events);

    List<String[]> eventsCSV = Lists.newArrayList();
    // find the biggest collection of what keys in this list (presumably
    // homogeneous),
    // use that as the csv headers.
    int whatKeyCount = 0;

    Set<String> foundColumnNames = Sets.newHashSet();
    for (Event event : events) {
      Map<String, String> whatMap = event.getWhatMap();
      foundColumnNames.addAll(whatMap.keySet());
    }
    List<String> columns = Lists.newArrayList();
    columns.addAll(foundColumnNames);
    Collections.sort(columns);
    for (Event event : events) {
      eventsCSV.add(event.toCSV(columns));
    }
    // add back in the standard pacot event columns
    columns.add(0, "who");
    columns.add(1, "when");
    columns.add(2, "lat");
    columns.add(3, "lon");
    columns.add(4, "appId");
    columns.add(5, "pacoVersion");
    columns.add(6, "experimentName");
    columns.add(7, "experimentId");
    columns.add(8, "scheduledTime");
    columns.add(9, "responseTime");

    resp.setContentType("text/csv");
    CSVWriter csvWriter = null;
    try {
      csvWriter = new CSVWriter(resp.getWriter());
      String[] columnsArray = columns.toArray(new String[0]);
      csvWriter.writeNext(columnsArray);
      for (String[] eventCSV : eventsCSV) {
        csvWriter.writeNext(eventCSV);
      }
      csvWriter.flush();
    } finally {
      if (csvWriter != null) {
        csvWriter.close();
      }
    }
  }

  private void showEvents(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    List<com.google.sampling.experiential.server.Query> query =
        new QueryParser().parse(stripQuotes(req.getParameter("q")));
    List<Event> greetings = getEventsWithQuery(req, query);
    sortEvents(greetings);
    printEvents(resp, greetings);
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

  private List<Event> getEventsWithQuery(HttpServletRequest req,
      List<com.google.sampling.experiential.server.Query> queries) {
    User whoFromLogin = getWhoFromLogin();
    if (!isDevInstance(req) && whoFromLogin == null) {
      throw new IllegalArgumentException("Must be logged in to retrieve data.");
    }
    String who = null;
    if (whoFromLogin != null) {
      who = whoFromLogin.getEmail();
    }
    return EventRetriever.getInstance().getEvents(queries, who, getTimeZoneForClient(req));
  }

  private void printEvents(HttpServletResponse resp, List<Event> greetings) throws IOException {
    long t1 = System.currentTimeMillis();
    long eventTime = 0;
    long whatTime = 0;
    if (greetings.isEmpty()) {
      resp.getWriter().println("Nothing to see here.");
    } else {
      StringBuilder out = new StringBuilder();
      out.append("<html><head><title>Current Ratings</title></head><body>");
      out.append("<h1>Results</h1>");
      out.append("<table border=1>");
      out.append("<tr><th>When</th><th>Who</th><th>Where</th><th>Experiment Name</th>"
          + "<th>What</th></tr>");
      for (Event eventRating : greetings) {
        long e1 = System.currentTimeMillis();
        out.append("<tr>");
        out.append("<td>").append(eventRating.getWhen().toString()).append("</td>");
        out.append("<td>").append(eventRating.getWho()).append("</td>");
        out.append("<td>").append(eventRating.getLat()).append(", ").append(eventRating.getLon())
            .append("</td>");
        out.append("<td>").append(eventRating.getExperimentName()).append("</td>");
        out.append("<td>");
        boolean first = true;
        eventTime += System.currentTimeMillis() - e1;
        long what1 = System.currentTimeMillis();
        // for (What what : eventRating.getWhat()) {
        Map<String, String> whatMap = eventRating.getWhatMap();
        if (whatMap.keySet() != null) {
          for (String key : whatMap.keySet()) {
            if (first) {
              first = false;
            } else {
              out.append(", ");
            }
            String value = whatMap.get(key);
            if (value == null) {
              value = "";
            }
            if (value.indexOf(" ") != -1) {
              value = "\"" + value + "\"";
            }
            out.append(key).append(" = ").append(value);
          }
        }
        whatTime += System.currentTimeMillis() - what1;
        out.append("</td>");
        out.append("<tr>");
      }
      long t2 = System.currentTimeMillis();
      log.info("EventServlet printEvents total: " + (t2 - t1));
      log.info("Event time: " + eventTime);
      log.info("what time: " + whatTime);
      out.append("</table></body></html>");
      resp.getWriter().println(out.toString());
    }
  }

  private List<String> getFilters(HttpServletRequest req) {
    // TODO (bobevans): this is beyond stupid.
    List<String> filters = Lists.newArrayList();
    String what = req.getParameter("what");
    if (what != null) {
      filters.add("what=" + what);
    }
    String whatValue = req.getParameter("what.value");
    if (whatValue != null) {
      filters.add("what.value=" + whatValue);
    }
    return filters;
  }

  private void sortEvents(List<Event> greetings) {
    Comparator<Event> dateComparator = new Comparator<Event>() {
      @Override
      public int compare(Event o1, Event o2) {
        Date when1 = o1.getWhen();
        Date when2 = o2.getWhen();
        if (when1 == null || when2 == null) {
          return 0;
        } else if (when1.after(when2)) {
          return -1;
        } else if (when2.after(when1)) {
          return 1;
        }
        return 0;
      }
    };
    Collections.sort(greetings, dateComparator);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    setCharacterEncoding(req, resp);
    // TODO(bobevans): Add security check
    String postBodyString = org.apache.commons.io.IOUtils.toString(req.getInputStream());
    if (postBodyString.equals("")) {
      resp.getWriter().write("Empty Post body");
    } else {
      log.info(postBodyString);
      JSONObject currentEvent = null;
      try {
        boolean isDevInstance = isDevInstance(req);
        if (postBodyString.startsWith("[")) {
          JSONArray posts = new JSONArray(postBodyString);
          for (int i = 0; i < posts.length(); i++) {
            currentEvent = posts.getJSONObject(i);
            postEvent(isDevInstance, currentEvent);
          }
        } else {
          currentEvent = new JSONObject(postBodyString);
          postEvent(isDevInstance, currentEvent);
        }
        resp.getWriter().write("Success");
      } catch (JSONException e) {
        e.printStackTrace();
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.getWriter().write(
            "Paco says: Invalid JSON Input: " + postBodyString + "\nError: " + e.getMessage());
      } catch (ParseException e) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.getWriter().write(
            "Paco says: Invalid Date in an Event Input: " + postBodyString + "\nError: "
                + e.getMessage());
      }  catch (Exception t) {
       log.log(Level.SEVERE, "Caught throwable in doPost!", t);
       resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
       resp.getWriter().write("Paco says: Something generic went wrong in an Event Input: "
       + postBodyString + "\nError: " + t.getMessage());
       }
    }
  }

  private void postEvent(boolean isDevInstance, JSONObject eventJson) throws JSONException,
      ParseException {
    String msg;
    User loggedInWho = getWhoFromLogin();

    if (loggedInWho == null) {
      throw new IllegalArgumentException("Must be logged in!");
    }
    String who = loggedInWho.getEmail();
    String whoFromPost = null;
    if (eventJson.has("who")) {
      whoFromPost = eventJson.getString("who");
    }
    if (isDevInstance && whoFromPost != null) {
      who = whoFromPost;
    }
    String lat = null;
    String lon = null;
    JSONObject where = null;
    if (eventJson.has("where")) {
      where = eventJson.getJSONObject("where");
      lat = where.getString("lat");
      lon = where.getString("lon");
    }

    String appId = "unspecified";
    if (eventJson.has("appId")) {
     appId = eventJson.getString("appId");
    }
    String pacoVersion = null;
    if (eventJson.has("pacoVersion")) {
      pacoVersion = eventJson.getString("pacoVersion");
    }
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd:HH:mm:ssZ");
    Date whenDate = null;
    if (eventJson.has("when")) {
      String when = eventJson.getString("when");
      
      whenDate = df.parse(when);
    } else {
      whenDate = new Date();
    }

    boolean shared = false;
    if (eventJson.has("shared")) {
      shared = eventJson.getBoolean("shared");
    }

    Experiment experiment = null;
    String experimentId = null;
    String experimentName = null;
    Date responseTime = null;    
    Date scheduledTime = null;
    
    if (eventJson.has("experimentId")) {
      experimentId = eventJson.getString("experimentId"); 
    }
    if (eventJson.has("experimentName")) {
      experimentName = eventJson.getString("experimentName"); 
    }
    
    PersistenceManager pm = null;

    if (experimentId != null) {
        pm = PMF.get().getPersistenceManager();
        javax.jdo.Query q = pm.newQuery(Experiment.class);
        q.setFilter("id == idParam");
        q.declareParameters("Long idParam");
        List<Experiment> experiments = (List<Experiment>)q.execute(Long.valueOf(experimentId));
        if (experiments.size() > 0) {
          experiment = experiments.get(0);
        }
    }
    
    Set<What> whats = Sets.newHashSet();
    List<PhotoBlob> blobs = Lists.newArrayList();
    if (eventJson.has("what")) {
      JSONObject what = eventJson.getJSONObject("what");
      for (Iterator iterator = what.keys(); iterator.hasNext();) {
        String whatKey = (String) iterator.next();
        String whatValue = what.getString(whatKey);
        whats.add(new What(whatKey, whatValue));
      }
    } else if (eventJson.has("responses")) {
      
      JSONArray responses = eventJson.getJSONArray("responses");
      log.info("There are " + responses.length() + " response objects");
      for (int i=0; i < responses.length(); i++) {
        JSONObject response = responses.getJSONObject(i);
        String inputId = response.getString("inputId");
        String name = response.getString("name");
        Input input = null;
        if (experiment != null) {
          input = experiment.getInputWithId(Long.valueOf(inputId));
        }
        String answer = response.getString("answer");
        if (name == null || name.isEmpty()) {
          name = "unnamed_"+i;
          
          whats.add(new What(name+"_inputId", inputId));
        }
        if (input != null && input.getResponseType() != null && 
            input.getResponseType().equals(InputDAO.PHOTO)) {
          PhotoBlob photoBlob = new PhotoBlob(name, Base64.decodeBase64(answer.getBytes()));
          blobs.add(photoBlob);
          answer = "blob";          
        }
        whats.add(new What(name, answer));
        
      }
    }
  
    if (eventJson.has("responseTime")) {
      
      String responseTimeStr = eventJson.getString("responseTime");
      if (!responseTimeStr.equals("null") && !responseTimeStr.isEmpty()) {
        responseTime = df.parse(responseTimeStr); 
      }
    }
    if (eventJson.has("scheduledTime")) {
      String timeStr = eventJson.getString("scheduledTime");
      if (!timeStr.equals("null") && !timeStr.isEmpty()) {       
        scheduledTime = df.parse(timeStr);
      }
    }
    
    log.info("Sanity check: who = " + who + 
        ", when = " + (new SimpleDateFormat("yyyyMMdd:HH:mm:ssZ")).format(whenDate) + 
        ", appId = "+appId +", what length = " + whats.size());
    
    if (pm != null) {
      pm.close();
    }
    EventRetriever.getInstance().postEvent(who, lat, lon, whenDate, appId, pacoVersion, whats,
        shared, experimentId, experimentName, responseTime, scheduledTime, blobs);
  }



  private void setCharacterEncoding(HttpServletRequest req, HttpServletResponse resp)
      throws UnsupportedEncodingException {
    req.setCharacterEncoding(Charsets.UTF_8.name());
    resp.setCharacterEncoding(Charsets.UTF_8.name());
  }


}
