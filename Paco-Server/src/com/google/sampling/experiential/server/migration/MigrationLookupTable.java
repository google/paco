package com.google.sampling.experiential.server.migration;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gwt.gen2.logging.shared.Log;
import com.google.gwt.thirdparty.guava.common.base.Strings;
import com.google.sampling.experiential.server.migration.jobs.ExperimentJDOToDatastoreMigration;
import com.google.sampling.experiential.server.migration.jobs.FeedbackTypeRepairMigration;
import com.google.sampling.experiential.server.migration.jobs.TestJDODSCompat;

public class MigrationLookupTable {

  private static Map<String, Class> migrations = Maps.newHashMap();
  static {
    migrations.put("96", ExperimentJDOToDatastoreMigration.class);
    migrations.put("97", FeedbackTypeRepairMigration.class);
    migrations.put("965", TestJDODSCompat.class);
  }
  public static MigrationJob getMigrationByName(String name) {
    if (Strings.isNullOrEmpty(name)) {
      Log.info("Could not run migration - no jobName specified");
      return null;
    }
    Class migrationClass = migrations.get(name);
    if (migrationClass != null) {
      try {
        return (MigrationJob) migrationClass.newInstance();
      } catch (InstantiationException e) {
        Log.severe("Could not instantiate migration named: " + name);
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        Log.severe("Did not have access to instantiate migration named: " + name);
        e.printStackTrace();
      }
    } else {
      Log.info("Migration name " + name + " does not exist in map");
    }
    return null;
  }


}
