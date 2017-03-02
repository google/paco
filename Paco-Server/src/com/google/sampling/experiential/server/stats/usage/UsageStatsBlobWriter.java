package com.google.sampling.experiential.server.stats.usage;

import java.io.IOException;
import java.util.logging.Logger;

public class UsageStatsBlobWriter {

  private static final Logger log = Logger.getLogger(UsageStatsBlobWriter.class.getName());


  public UsageStatsBlobWriter() {
  }

  public String writeStatsAsJson(String jobId, String timeZone, String requestorEmail) {
    log.info("writing usage stats report");
//    UsageStatsBackfillJob job = new UsageStatsBackfillJob();
//    UsageStatsCreatorStatsJob job = new UsageStatsCreatorStatsJob();
    UsageStatsCronJob job = new UsageStatsCronJob();
    try {
      job.run();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return "no job id reported for this type of job";
  }

}
