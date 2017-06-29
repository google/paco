package com.google.sampling.experiential.server.migration;

import java.util.Map;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.server.migration.jobs.ExperimentCompleteStatusSP;
import com.google.sampling.experiential.server.migration.jobs.ExperimentQuickStatusSP;

import com.google.sampling.experiential.server.migration.jobs.StoredProcImpl;

public class StoredProcLookupTable {
  private static final Logger log = Logger.getLogger(StoredProcLookupTable.class.getName());
  private static Map<String, Class> storedProcs = Maps.newHashMap();
  static {
    storedProcs.put("95", ExperimentCompleteStatusSP.class);
    storedProcs.put("96", ExperimentQuickStatusSP.class);
  }
  
  public static StoredProc getStoredProcByName(String name) {
    if (Strings.isNullOrEmpty(name)) {
      log.info("Could not run stored proc - no Name specified");
      return null;
    }
    Class storedProcClass = storedProcs.get(name);
    if (storedProcClass != null) {
      try {
        return (StoredProc) storedProcClass.newInstance();
      } catch (InstantiationException e) {
        log.severe("Could not instantiate stored proc named: " + name);
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        log.severe("Did not have access to instantiate stored proc named: " + name);
        e.printStackTrace();
      }
    } else {
      log.info("Stored Proc name " + name + " does not exist in map");
      return new StoredProcImpl();
    }
    return null;
  }
}
