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


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Charsets;
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
import com.google.sampling.experiential.shared.TimeUtil;

/**
 * Servlet that answers queries for Events.
 * 
 * @author Bob Evans
 *
 */
public class EventServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(EventServlet.class.getName());
  private DateTimeFormatter jodaFormatter = DateTimeFormat.forPattern(TimeUtil.DATETIME_FORMAT);
  private String defaultAdmin = "bobevans@google.com";
  private List<String> adminUsers = Lists.newArrayList(defaultAdmin);
  private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    if (user == null) {
      resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
    } else {
      String anonStr = req.getParameter("anon");
      boolean anon = false;
      if (anonStr != null) {
        anon = Boolean.parseBoolean(anonStr);
      }
      if (req.getParameter("mapping") != null) {
        dumpUserIdMapping(req, resp);
      } else if (req.getParameter("json") != null) {
        resp.setContentType("application/json;charset=UTF-8");
        dumpEventsJson(resp, req, anon);
      } else if (req.getParameter("csv") != null) {
        resp.setContentType("text/csv;charset=UTF-8");
        dumpEventsCSV(resp, req, anon);
      } else {
        resp.setContentType("text/html;charset=UTF-8");
        showEvents(req, resp, anon);
      }
    }
  }

  private void dumpUserIdMapping(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    List<com.google.sampling.experiential.server.Query> query =
      new QueryParser().parse(stripQuotes(getParam(req, "q")));
    List<Event> events = getEventsWithQuery(req, query);
    sortEvents(events);
    Set<String> whos = new HashSet<String>();
    for (Event event : events) {
      whos.add(event.getWho());
    }
    StringBuilder mappingOutput = new StringBuilder();
    for (String who : whos) {
      mappingOutput.append(who);
      mappingOutput.append(",");
      mappingOutput.append(Event.getAnonymousId(who));
      mappingOutput.append("\n");
    }
    resp.setContentType("text/csv;charset=UTF-8");
    resp.getWriter().println(mappingOutput.toString());    
  }

  public static DateTimeZone getTimeZoneForClient(HttpServletRequest req) {
    String tzStr = getParam(req, "tz");
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

  private boolean isDevInstance(HttpServletRequest req) {
    return ExperimentServlet.isDevInstance(req);
  }

  private User getWhoFromLogin() {
    UserService userService = UserServiceFactory.getUserService();
    return userService.getCurrentUser();
  }

  private static String getParam(HttpServletRequest req, String paramName) {
    try {
      String parameter = req.getParameter(paramName);
      if (parameter == null || parameter.isEmpty()) {
        return null;
      }
      return URLDecoder.decode(parameter, "UTF-8");
    } catch (UnsupportedEncodingException e1) {
      throw new IllegalArgumentException("Unspported encoding");
    }
  }

  private void dumpEventsJson(HttpServletResponse resp, HttpServletRequest req, boolean anon) throws IOException {
    List<com.google.sampling.experiential.server.Query> query =
        new QueryParser().parse(stripQuotes(getParam(req, "q")));
    List<Event> events = getEventsWithQuery(req, query);
    sortEvents(events);
    String jsonOutput = jsonifyEvents(events, anon);
    resp.getWriter().println(jsonOutput);
  }

  private String jsonifyEvents(List<Event> events, boolean anon) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);
    try {
      List<EventDAO> eventDAOs = Lists.newArrayList();
      for (Event event : events) {
        String userId = event.getWho();
        if (anon) {
          userId = Event.getAnonymousId(userId);
        }
        eventDAOs.add(new EventDAO(userId, event.getWhen(), event.getExperimentName(),
            event.getLat(), event.getLon(), event.getAppId(), event.getPacoVersion(),
            event.getWhatMap(), event.isShared(), event.getResponseTime(), event.getScheduledTime(),
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
  
  private void dumpEventsCSV(HttpServletResponse resp, HttpServletRequest req, boolean anon) throws IOException {
    List<com.google.sampling.experiential.server.Query> query = new QueryParser().parse(stripQuotes(getParam(req, "q")));

    String loggedInuser = getWhoFromLogin().getEmail();
    if (loggedInuser != null && adminUsers.contains(loggedInuser)) {
      loggedInuser = defaultAdmin;
    }
    List<Event> events = EventRetriever.getInstance().getEvents(query, loggedInuser, getTimeZoneForClient(req));
    sortEvents(events);

    List<String[]> eventsCSV = Lists.newArrayList();

    Set<String> foundColumnNames = Sets.newHashSet();
    for (Event event : events) {
      Map<String, String> whatMap = event.getWhatMap();
      foundColumnNames.addAll(whatMap.keySet());
    }
    List<String> columns = Lists.newArrayList();
    columns.addAll(foundColumnNames);
    Collections.sort(columns);
    for (Event event : events) {
      eventsCSV.add(event.toCSV(columns, anon));
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
    columns.add(8, "responseTime");
    columns.add(9, "scheduledTime");
    
    resp.setContentType("text/csv;charset=UTF-8");
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

  private void showEvents(HttpServletRequest req, HttpServletResponse resp, boolean anon) throws IOException {
    List<com.google.sampling.experiential.server.Query> query = new QueryParser().parse(stripQuotes(getParam(req, "q")));
    List<Event> greetings = getEventsWithQuery(req, query);
    sortEvents(greetings);
    printEvents(resp, greetings, anon);
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

  private void printEvents(HttpServletResponse resp, List<Event> greetings, boolean anon) throws IOException {
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
      out.append("<tr><th>Experiment Name</th><th>Scheduled Time</th><th>Response Time</th><th>Who</th><th>Responses</th></tr>");
      for (Event eventRating : greetings) {
        long e1 = System.currentTimeMillis();
        out.append("<tr>");
        out.append("<td>").append(eventRating.getExperimentName()).append("</td>");
        out.append("<td>").append(jodaFormatter.print(new DateTime(eventRating.getScheduledTime()))).append("</td>");
        out.append("<td>").append(jodaFormatter.print(new DateTime(eventRating.getResponseTime()))).append("</td>");
        String who = eventRating.getWho();
        if (anon) {
          who = Event.getAnonymousId(who);
        }
        out.append("<td>").append(who).append("</td>");
        eventTime += System.currentTimeMillis() - e1;
        long what1 = System.currentTimeMillis();
        // we want to render photos as photos not as strings.
        // It would be better to do this by getting the experiment for the event and going through the inputs.
        // That was not done because there may be multiple experiments in the data returned for this interface and
        // that is work that is otherwise necessary for now. Go pretotyping!
        // TODO clean all the accesses of what could be tainted data.
        List<PhotoBlob> photos = eventRating.getBlobs();
        Map<String, PhotoBlob> photoByNames = Maps.newConcurrentMap();
        for (PhotoBlob photoBlob : photos) {
          photoByNames.put(photoBlob.getName(), photoBlob);
        }
        Map<String, String> whatMap = eventRating.getWhatMap();
        Set<String> keys = whatMap.keySet();
        if (keys != null) {
          ArrayList<String> keysAsList = Lists.newArrayList(keys);
          Collections.sort(keysAsList);
          Collections.reverse(keysAsList);
          for (String key : keysAsList) {
            String value = whatMap.get(key);
            if (value == null) {
              value = "";
            } else if (photoByNames.containsKey(key)) {
              byte[] photoData = photoByNames.get(key).getValue();
              if (photoData != null && photoData.length > 0) {
                String photoString = new String(Base64.encodeBase64(photoData));
                if (!photoString.equals("==")) { 
                  value = "<img height=\"375\" src=\"data:image/jpg;base64," 
                    + photoString  
                    + "\">";
                } else {
                  value = "";
                }
              } else {
                value = "";
              }
            } else if (value.indexOf(" ") != -1) {
              value = "\"" + value + "\"";
            }
            out.append("<td>");
            out.append(key).append(" = ").append(value);
            out.append("</td>");
          }
        }
        whatTime += System.currentTimeMillis() - what1;
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
    if (ServletFileUpload.isMultipartContent(req)) {
      processCsvUpload(req, resp);
    } else {
      processJsonUpload(req, resp);
    }
  }

	private void processCsvUpload(HttpServletRequest req,
			HttpServletResponse resp) {
		PrintWriter out = null;
		try {
			out = resp.getWriter();
		} catch (IOException e1) {
			log.log(Level.SEVERE, "Cannot get an output PrintWriter!");
		}
		try {
		  boolean isDevInstance = isDevInstance(req);
			ServletFileUpload fileUploadTool = new ServletFileUpload();
			fileUploadTool.setSizeMax(50000);
			resp.setContentType("text/html;charset=UTF-8");

			FileItemIterator iterator = fileUploadTool.getItemIterator(req);
			while (iterator.hasNext()) {
				FileItemStream item = iterator.next();
				InputStream in = null;
				try {
					in = item.openStream();

					if (item.isFormField()) {
						out.println("Got a form field: " + item.getFieldName());
					} else {
						String fieldName = item.getFieldName();
						String fileName = item.getName();
						String contentType = item.getContentType();

						out.println("--------------");
						out.println("fileName = " + fileName);
						out.println("field name = " + fieldName);
						out.println("contentType = " + contentType);

						String fileContents = null;
						fileContents = IOUtils.toString(in);
						out.println("length: " + fileContents.length());
						out.println(fileContents);
						saveCSV(fileContents, isDevInstance);
					}
				} catch (ParseException e) {
          log.info("Parse Exception: " + e.getMessage());
          out.println("Could not parse your csv upload: " + e.getMessage());
        } finally {
					in.close();
				}
			}
		} catch (SizeLimitExceededException e) {
		  log.info("SizeLimitExceededException: " + e.getMessage());
			out.println("You exceeded the maximum size ("+ e.getPermittedSize() + ") of the file ("
					+ e.getActualSize() + ")");
			return;
		} catch (IOException e) {
			log.severe("IOException: " + e.getMessage());
			out.println("Error in receiving file.");
		} catch (FileUploadException e) {
			log.severe("FileUploadException: " + e.getMessage());
			out.println("Error in receiving file.");
		}
	}

	private void saveCSV(String fileContents, boolean isDevInstance) throws ParseException, IOException {
    CSVReader reader = new CSVReader(new BufferedReader(new StringReader("yourfile.csv")));
    List<String[]> rows = reader.readAll();
    if (rows == null || rows.size() == 0) {
      log.info("No rows in uploaded CSV");
      throw new IOException("No rows in uploaded CSV. Check your file if this is incorrect.");
    }
    String[] header = rows.get(0);
    for (int i = 1; i < rows.size(); i++) {
      postEventFromRowAsHash(convertToHashMap(header, rows.get(i)), isDevInstance);
    }
      
	}

	private HashMap<String, String> convertToHashMap(String[] header, String[] strings) throws ParseException {
	  HashMap<String,String> map = new HashMap<String, String>();
	  for (int i = 0; i < header.length; i++) {
      String currentHeader = header[i];
      String currentValue = strings[i];
      map.put(currentHeader, currentValue);
    }
	  return map;
	}
	
	public void postEventFromRowAsHash(HashMap<String, String> rowData, boolean isDevInstance) throws ParseException {
    User loggedInWho = getWhoFromLogin();

    if (loggedInWho == null) {
      throw new IllegalArgumentException("Must be logged in!");
    }
    String who = loggedInWho.getEmail();
    String whoFromPost = null;
    if (rowData.containsKey("who")) {
      whoFromPost = rowData.get("who");
      rowData.remove("who");
    }
    if (isDevInstance && whoFromPost != null) {
      who = whoFromPost;
    }
    String lat = null;
    String lon = null;
    String where = null;
    if (rowData.containsKey("where")) {
      where = rowData.get("where");
      rowData.remove("where");
      lat = where.substring(0, where.indexOf(","));
      lon = where.substring(where.indexOf(",") + 1);
    }

    String appId = "from_csv";
    if (rowData.containsKey("appId")) {
     appId = rowData.get("appId");
     rowData.remove("appId");
    }
    String pacoVersion = null;
    if (rowData.containsKey("pacoVersion")) {
      pacoVersion = rowData.get("pacoVersion");
      rowData.remove("pacoVersion");
    }
    SimpleDateFormat df = new SimpleDateFormat(TimeUtil.DATETIME_FORMAT);
    SimpleDateFormat oldDf = new SimpleDateFormat(TimeUtil.DATETIME_FORMAT_OLD);
    Date whenDate = null;
    if (rowData.containsKey("when")) {
      String when = rowData.get("when");
      rowData.remove("when");
      whenDate = parseDate(df, oldDf, when);
    } else {
      whenDate = new Date();
    }

    boolean shared = false;

    String experimentId = null;
    String experimentName = null;
    Date responseTime = null;    
    Date scheduledTime = null;
    
    if (rowData.containsKey("experimentId")) {
      experimentId = rowData.get("experimentId"); 
      rowData.remove("experimentId");
    }
    if (rowData.containsKey("experimentName")) {
      experimentName = rowData.get("experimentName"); 
      rowData.remove("experimentName");
    }
    
    Experiment experiment = ExperimentRetriever.getExperiment(experimentId);
    
    if (experiment == null) {
      throw new IllegalArgumentException("Must post to an existing experiment!");
    }
    
    if (!ExperimentRetriever.isWhoAllowedToPostToExperiment(experiment, who)) {
      throw new IllegalArgumentException("This user is not allowed to post to this experiment");      
    }
    

    
    Set<What> whats = Sets.newHashSet();
    List<PhotoBlob> blobs = Lists.newArrayList();
    if (rowData.keySet().size() > 0) {      
      log.info("There are " + rowData.keySet().size() + " csv columns left");
      for (String name : rowData.keySet()) {
        String answer = rowData.get(name);
        Input input = null;
        if (experiment != null) {
          input = experiment.getInputWithName(name);
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
  
    if (rowData.containsKey("responseTime")) {      
      String responseTimeStr = rowData.get("responseTime");
      if (!responseTimeStr.equals("null") && !responseTimeStr.isEmpty()) {
        responseTime = parseDate(df, oldDf, responseTimeStr); 
      }
    }
    if (rowData.containsKey("scheduledTime")) {
      String timeStr = rowData.get("scheduledTime");
      if (!timeStr.equals("null") && !timeStr.isEmpty()) {       
        scheduledTime = parseDate(df, oldDf, timeStr);
      }
    }
    
    log.info("Sanity check: who = " + who + 
        ", when = " + (new SimpleDateFormat(TimeUtil.DATETIME_FORMAT)).format(whenDate) + 
        ", appId = "+appId +", what length = " + whats.size());
    
    EventRetriever.getInstance().postEvent(who, lat, lon, whenDate, appId, pacoVersion, whats,
        shared, experimentId, experimentName, responseTime, scheduledTime, blobs);

    
  }

  private void processJsonUpload(HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		String postBodyString = org.apache.commons.io.IOUtils.toString(req
				.getInputStream());
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
						"Paco says: Invalid JSON Input: " + postBodyString
								+ "\nError: " + e.getMessage());
			} catch (ParseException e) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				resp.getWriter()
						.write("Paco says: Invalid Date in an Event Input: "
								+ postBodyString + "\nError: " + e.getMessage());
			} catch (Exception t) {
				log.log(Level.SEVERE, "Caught throwable in doPost!", t);
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				resp.getWriter()
						.write("Paco says: Something generic went wrong in an Event Input: "
								+ postBodyString + "\nError: " + t.getMessage());
			}
		}
	}

  private void postEvent(boolean isDevInstance, JSONObject eventJson) throws JSONException,
      ParseException {
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
    SimpleDateFormat df = new SimpleDateFormat(TimeUtil.DATETIME_FORMAT);
    SimpleDateFormat oldDf = new SimpleDateFormat(TimeUtil.DATETIME_FORMAT_OLD);
    Date whenDate = null;
    if (eventJson.has("when")) {
      String when = eventJson.getString("when");
      whenDate = parseDate(df, oldDf, when);
    } else {
      whenDate = new Date();
    }

    boolean shared = false;
    if (eventJson.has("shared")) {
      shared = eventJson.getBoolean("shared");
    }

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
    
    Experiment experiment = ExperimentRetriever.getExperiment(experimentId);
    
    if (experiment == null) {
      throw new IllegalArgumentException("Must post to an existing experiment!");
    }
    
    if (!ExperimentRetriever.isWhoAllowedToPostToExperiment(experiment, who)) {
      throw new IllegalArgumentException("This user is not allowed to post to this experiment");      
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
        responseTime = parseDate(df, oldDf, responseTimeStr); 
      }
    }
    if (eventJson.has("scheduledTime")) {
      String timeStr = eventJson.getString("scheduledTime");
      if (!timeStr.equals("null") && !timeStr.isEmpty()) {       
        scheduledTime = parseDate(df, oldDf, timeStr);
      }
    }
    
    log.info("Sanity check: who = " + who + 
        ", when = " + (new SimpleDateFormat(TimeUtil.DATETIME_FORMAT)).format(whenDate) + 
        ", appId = "+appId +", what length = " + whats.size());

    EventRetriever.getInstance().postEvent(who, lat, lon, whenDate, appId, pacoVersion, whats,
        shared, experimentId, experimentName, responseTime, scheduledTime, blobs);
  }

  private Date parseDate(SimpleDateFormat df, SimpleDateFormat oldDf, String when) throws ParseException {
    Date dateString = null;
    try {
      dateString = df.parse(when);
    } catch (ParseException pe) {
      dateString = oldDf.parse(when); //TODO remove this once all the clients are updated.        
    }
    return dateString;
  }

  private void setCharacterEncoding(HttpServletRequest req, HttpServletResponse resp)
      throws UnsupportedEncodingException {
    req.setCharacterEncoding(Charsets.UTF_8.name());
    resp.setCharacterEncoding(Charsets.UTF_8.name());
  }


}
