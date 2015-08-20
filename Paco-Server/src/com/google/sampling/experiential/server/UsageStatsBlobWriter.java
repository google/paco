package com.google.sampling.experiential.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.Channels;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringEscapeUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.shared.TimeUtil;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentQueryResult;
import com.pacoapp.paco.shared.model2.JsonConverter;
import com.pacoapp.paco.shared.scheduling.ActionScheduleGenerator;
import com.pacoapp.paco.shared.util.ExperimentHelper;

public class UsageStatsBlobWriter {

  private static final Logger log = Logger.getLogger(UsageStatsBlobWriter.class.getName());
  private DateTimeFormatter jodaFormatter = DateTimeFormat.forPattern(TimeUtil.DATETIME_FORMAT).withOffsetParsed();


  public UsageStatsBlobWriter() {
  }

  public String writeStatsAsJson(String jobId, String timeZone, String requestorEmail, String adminDomainFilter)
          throws IOException {
    log.info("writing usage stats as json");

    ExperimentQueryResult experiments = ExperimentServiceFactory.getExperimentService().getAllExperiments(null);
    String eventPage = runStats(experiments, timeZone, requestorEmail, adminDomainFilter);
    BlobKey blobKey = writeBlobUsingNewApi(jobId, timeZone, eventPage);
    return blobKey.getKeyString();
  }

  private String runStats(ExperimentQueryResult experimentQueryResults, String timeZone, String requestorEmail, String adminDomainFilter) {
    List<ExperimentDAO> experimentList = experimentQueryResults.getExperiments();
    UsageStat usageStats = new UsageStat(DateTime.now());
    if (experimentList == null) {
      return "";
    }
    usageStats.setTotalExperimentCount(experimentList.size());

    getAdminDomainExperimentCount(experimentList, usageStats, adminDomainFilter);


    return jsonifyEvents(usageStats);
  }

  private void getAdminDomainExperimentCount(List<ExperimentDAO> experimentList, UsageStat usageStats, String adminDomainFilter) {
    List<ExperimentDAO> domainExperiments = Lists.newArrayList();
    for (ExperimentDAO experimentDAO : experimentList) {
      List<String> admins = experimentDAO.getAdmins();
      for (String admin : admins) {
        if (admin.indexOf("@" + adminDomainFilter) != -1) {
          domainExperiments.add(experimentDAO);
          break;
        }
      }

    }
    usageStats.setDomainExperimentCount(domainExperiments.size());

    final DateTime nowDateTime = DateTime.now();

    int future = 0;
    int past = 0;
    int present = 0;
    int ongoing = 0;
    List<Integer> lastModifiedAge = Lists.newArrayList();
    int published = 0 ;
    List<Integer> publishedUserCounts = Lists.newArrayList();
    for (ExperimentDAO experimentDAO : domainExperiments) {
      if (!ExperimentHelper.isAnyGroupOngoingDuration(experimentDAO)) {
        DateMidnight startDate = ActionScheduleGenerator.getEarliestStartDate(experimentDAO);
        DateTime endDate = ActionScheduleGenerator.getLastEndTime(experimentDAO);
        if (startDate != null && startDate.isAfter(nowDateTime)) {
          future++;
        } else if (endDate != null && endDate.isBefore(nowDateTime)) {
          past++;
        } else if (endDate != null && startDate != null && startDate.isBefore(nowDateTime) && endDate.isAfter(nowDateTime)) {
          present++;
        }
      } else {
        ongoing++;
//        String modifiedDate = experimentDAO.getModifyDate();
//        if (modifiedDate != null) {
//          DateTime modifiedDateTime = com.pacoapp.paco.shared.util.TimeUtil.unformatDate(modifiedDate);
//          Days ageDays = Days.daysBetween(modifiedDateTime, nowDateTime);
//          int dayCount = ageDays.getDays();
//          lastModifiedAge.add(dayCount);
//        }
      }
      if (experimentDAO.getPublished()) {
        published++;
      }
      List<String> publishedUsers = experimentDAO.getPublishedUsers();
      if (publishedUsers != null) {
        publishedUserCounts.add(publishedUsers.size());
      } else {
        publishedUserCounts.add(0);
      }
    }

    usageStats.setDomainFutureExperimentCount(future);
    usageStats.setDomainPastExperimentCount(past);
    usageStats.setDomainPresentExperimentCount(present);
    usageStats.setDomainOngoingExperimentCount(ongoing);
    usageStats.setDomainPublishedExperimentCount(published);
    usageStats.setDomainPublishedUserCounts(publishedUserCounts);
//    usageStats.setDomainExperimentModifiedAges(lastModifiedAge);
  }

  private BlobKey writeBlobUsingNewApi(String jobId, String timeZone,
                                       String eventPage) throws IOException,
                                       FileNotFoundException {

    GcsService gcsService = GcsServiceFactory.createGcsService();
    String BUCKETNAME = "reportbucket";
    String FILENAME = jobId;
    GcsFilename filename = new GcsFilename(BUCKETNAME, FILENAME);
    GcsFileOptions options = new GcsFileOptions.Builder().mimeType("application/json").acl("project-private")
                                                         .addUserMetadata("jobId", jobId).build();

    GcsOutputChannel writeChannel = gcsService.createOrReplace(filename, options);
    PrintWriter writer = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));
    writer.println(eventPage);
    writer.flush();

    writeChannel.waitForOutstandingWrites();

    //writeChannel.write(ByteBuffer.wrap("And miles to go before I sleep.".getBytes("UTF8")));

    writeChannel.close();
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    BlobKey blobKey = blobstoreService.createGsBlobKey(
        "/gs/" + BUCKETNAME + "/" + FILENAME);
    return blobKey;
  }

  private String jsonifyEvents(UsageStat usageStats) {
    ObjectMapper mapper = JsonConverter.getObjectMapper();

    try {
      return mapper.writeValueAsString(usageStats);
    } catch (JsonGenerationException e) {
      e.printStackTrace();
      log.severe(e.getMessage());
    } catch (JsonMappingException e) {
      e.printStackTrace();
      log.severe(e.getMessage());
    } catch (IOException e) {
      e.printStackTrace();
      log.severe(e.getMessage());
    }
    return "Error could not retrieve events as json";
  }

  private String escapeText(String experimentTitle) {
    return StringEscapeUtils.escapeHtml4(experimentTitle);
  }


}
