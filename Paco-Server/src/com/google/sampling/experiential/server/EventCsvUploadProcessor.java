package com.google.sampling.experiential.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

import au.com.bytecode.opencsv.CSVReader;

import com.google.appengine.api.users.User;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.paco.shared.Outcome;
import com.google.paco.shared.model.InputDAO;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.model.Input;
import com.google.sampling.experiential.model.PhotoBlob;
import com.google.sampling.experiential.model.What;
import com.google.sampling.experiential.shared.TimeUtil;

public class EventCsvUploadProcessor {
  
  private static final Logger log = Logger.getLogger(EventCsvUploadProcessor.class.getName());

  void processCsvUpload(User loggedInWho, FileItemIterator iterator, PrintWriter out) {
    String fileName = null;
    try {

      while (iterator.hasNext()) {
        FileItemStream item = iterator.next();
        InputStream in = null;
        try {
          in = item.openStream();

          if (item.isFormField()) {
            //out.println("Got a form field: " + item.getFieldName());
          } else {
//            String fieldName = item.getFieldName();
            fileName = item.getName();
//            String contentType = item.getContentType();
//
//            out.println("--------------");
//            out.println("fileName = " + fileName);
//            out.println("field name = " + fieldName);
//            out.println("contentType = " + contentType);

            String fileContents = null;
            fileContents = IOUtils.toString(in);
//            out.println("length: " + fileContents.length());
//            out.println(fileContents);
            saveCSV(fileContents, loggedInWho);
          }
        } catch (ParseException e) {
          log.info("Parse Exception: " + e.getMessage());
          out.println("Could not parse your csv upload: " + e.getMessage());
        } finally {
          in.close();
        }
      }
    } catch (SizeLimitExceededException e) {
      log.info("SizeLimitExceededException for file: " + fileName + e.getMessage());
      out.println("You exceeded the maximum size (" + e.getPermittedSize() + ") of the file (" + e.getActualSize()
                  + ")");
      return;
    } catch (IOException e) {
      log.severe("IOException: " + e.getMessage());
      out.println("Error in receiving file." + fileName);
    } catch (FileUploadException e) {
      log.severe("FileUploadException: " + e.getMessage());
      out.println("Error in receiving file." + fileName);
    }
  }

  private void saveCSV(String fileContents, User loggedInWho) throws ParseException {
    CSVReader reader = new CSVReader(new BufferedReader(new StringReader("yourfile.csv")));
    List<String[]> rows;
    try {
      rows = reader.readAll();
    } catch (IOException e) {
      throw new IllegalArgumentException("Error reading CSV. Check your file if this is incorrect.");
    }
    if (rows == null || rows.size() == 0) {
      log.info("No rows in uploaded CSV");
      throw new IllegalArgumentException("No rows in uploaded CSV. Check your file if this is incorrect.");
    }
    String[] header = rows.get(0);
    for (int i = 1; i < rows.size(); i++) {
      postEventFromRowAsHash(convertToHashMap(header, rows.get(i)), i, loggedInWho);
    }

  }

  private HashMap<String, String> convertToHashMap(String[] header, String[] strings) throws ParseException {
    HashMap<String, String> map = new HashMap<String, String>();
    for (int i = 0; i < header.length; i++) {
      String currentHeader = header[i];
      String currentValue = strings[i];
      map.put(currentHeader, currentValue);
    }
    return map;
  }

  public void postEventFromRowAsHash(HashMap<String, String> rowData, long eventId, User loggedInWho) 
      throws ParseException {

    if (loggedInWho == null) {
      throw new IllegalArgumentException("Must be logged in!");
    }
    String who = loggedInWho.getEmail();
    String whoFromPost = null;
    if (rowData.containsKey("who")) {
      whoFromPost = rowData.get("who");
      rowData.remove("who");
    }
    Outcome outcome = new Outcome(eventId);
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
    Date whenDate = new Date();

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
    Integer experimentVersion = null;
    if (rowData.containsKey("experimentVersion")) {
      String experimentVersionStr = rowData.get("experimentVersion");
      rowData.remove("experimentVersion");
      if (!Strings.isNullOrEmpty(experimentVersionStr)) {
        try {
          experimentVersion = Integer.parseInt(experimentVersionStr);
        } catch (NumberFormatException nfe) {          
        }
      }
    }
    Experiment experiment = ExperimentRetriever.getInstance().getExperiment(experimentId);

    if (experiment == null) {
      throw new IllegalArgumentException("Must post to an existing experiment!");
    }

    if (!experiment.isWhoAllowedToPostToExperiment(who)) {
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
        if (input != null && input.getResponseType() != null && input.getResponseType().equals(InputDAO.PHOTO)) {
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

    log.info("Sanity check: who = " + who + ", when = "
             + (new SimpleDateFormat(TimeUtil.DATETIME_FORMAT)).format(whenDate) + ", appId = " + appId
             + ", what length = " + whats.size());

    EventRetriever.getInstance().postEvent(who, null, null, whenDate, appId, pacoVersion, whats, false, experimentId,
                                           experimentName, experimentVersion, responseTime, scheduledTime, blobs, null);

  }
  
  private Date parseDate(SimpleDateFormat df, SimpleDateFormat oldDf, String when) throws ParseException {
    return df.parse(when);
  }

}
