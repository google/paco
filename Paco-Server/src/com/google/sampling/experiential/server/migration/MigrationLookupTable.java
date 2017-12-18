package com.google.sampling.experiential.server.migration;

import java.util.Map;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.server.migration.jobs.AnonymizeParticipantsJob;
import com.google.sampling.experiential.server.migration.jobs.CSInsertEventsJob;
import com.google.sampling.experiential.server.migration.jobs.CSInsertOutputsJob;
import com.google.sampling.experiential.server.migration.jobs.CatchUpDateRangeJob;
import com.google.sampling.experiential.server.migration.jobs.CatchUpDateRangeJobOldVersion;
import com.google.sampling.experiential.server.migration.jobs.ConvertEventV4ToV5Job;
import com.google.sampling.experiential.server.migration.jobs.EventStatsCounterMigrationJob;
import com.google.sampling.experiential.server.migration.jobs.ExperimentHubMigrationJob;
import com.google.sampling.experiential.server.migration.jobs.ExperimentJDOToDatastoreMigration;
import com.google.sampling.experiential.server.migration.jobs.ExperimentTitleLowercaseMigrationJob;
import com.google.sampling.experiential.server.migration.jobs.FeedbackTypeRepairMigration;
import com.google.sampling.experiential.server.migration.jobs.TestJDODSCompat;
import com.google.sampling.experiential.server.stats.usage.UsageStatsBackfillJob;

public class MigrationLookupTable {
  private static final Logger log = Logger.getLogger(MigrationLookupTable.class.getName());


  private static Map<String, Class> migrations = Maps.newHashMap();
  static {
    migrations.put("95", ExperimentHubMigrationJob.class);
    migrations.put("96", ExperimentJDOToDatastoreMigration.class);
    migrations.put("97", FeedbackTypeRepairMigration.class);
    migrations.put("965", TestJDODSCompat.class);
    migrations.put("98", EventStatsCounterMigrationJob.class);
    migrations.put("99", UsageStatsBackfillJob.class);
    migrations.put("19", CSInsertEventsJob.class);
    migrations.put("20", CSInsertOutputsJob.class);
    migrations.put("21", CatchUpDateRangeJob.class);
    migrations.put("22", ConvertEventV4ToV5Job.class);
    migrations.put("23", AnonymizeParticipantsJob.class);
    // Catchup job old version, persists experiment info for an event in events table 
    migrations.put("24", CatchUpDateRangeJobOldVersion.class);
    migrations.put("100", ExperimentTitleLowercaseMigrationJob.class);
  }
  public static MigrationJob getMigrationByName(String name) {
    if (Strings.isNullOrEmpty(name)) {
      log.info("Could not run migration - no jobName specified");
      return null;
    }
    Class migrationClass = migrations.get(name);
    if (migrationClass != null) {
      try {
        return (MigrationJob) migrationClass.newInstance();
      } catch (InstantiationException e) {
        log.severe("Could not instantiate migration named: " + name);
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        log.severe("Did not have access to instantiate migration named: " + name);
        e.printStackTrace();
      }
    } else {
      log.info("Migration name " + name + " does not exist in map");
    }
    return null;
  }


}
