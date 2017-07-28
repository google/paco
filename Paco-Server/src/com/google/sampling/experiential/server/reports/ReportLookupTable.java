package com.google.sampling.experiential.server.reports;

import java.util.Map;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.server.reports.jobs.CompleteStatusReportJob;
import com.google.sampling.experiential.server.reports.jobs.ExperimentCompleteStatusStoredProcedure;
import com.google.sampling.experiential.server.reports.jobs.ExperimentQuickStatusStoredProcedure;
import com.google.sampling.experiential.server.reports.jobs.QuickStatusReportJob;

public class ReportLookupTable {
  private static final Logger log = Logger.getLogger(ReportLookupTable.class.getName());

  private static Map<String, Class> reports = Maps.newHashMap();
  static {
    reports.put("1", ExperimentCompleteStatusStoredProcedure.class);
    reports.put("2", ExperimentQuickStatusStoredProcedure.class);
    reports.put("3", CompleteStatusReportJob.class);
    reports.put("4", QuickStatusReportJob.class);
  }
  public static ReportJob getReportBackendName(String id) {
    if (Strings.isNullOrEmpty(id)) {
      log.info("Could not run report - no jobName specified");
      return null;
    }
    Class reportClass = reports.get(id);
    if (reportClass != null) {
      try {
        return (ReportJob) reportClass.newInstance();
      } catch (InstantiationException e) {
        log.severe("Could not instantiate report named: " + id);
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        log.severe("Did not have access to instantiate report named: " + id);
        e.printStackTrace();
      }
    } else {
      log.info("Report name " + id + " does not exist in map");
    }
    return null;
  }
}
