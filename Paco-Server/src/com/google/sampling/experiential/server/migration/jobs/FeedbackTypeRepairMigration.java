package com.google.sampling.experiential.server.migration.jobs;

import java.util.List;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.sampling.experiential.server.AuthUtil;
import com.google.sampling.experiential.server.ExceptionUtil;
import com.google.sampling.experiential.server.ExperimentService;
import com.google.sampling.experiential.server.ExperimentServiceFactory;
import com.google.sampling.experiential.server.migration.MigrationJob;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.ExperimentQueryResult;

public class FeedbackTypeRepairMigration implements MigrationJob {
  public static final Logger log = Logger.getLogger(FeedbackTypeRepairMigration.class.getName());
  public static boolean repairFeedbacktypeAssignment(ExperimentDAO experiment) {
    List<ExperimentGroup> groups = experiment.getGroups();
    boolean madeChange = false;
    for (ExperimentGroup experimentGroup : groups) {
      final Integer feedbackTypeOnGroup = experimentGroup.getFeedbackType();
      if (feedbackTypeOnGroup != null) {
        if (experimentGroup.getFeedback().getType() == null ||
                experimentGroup.getFeedback().getType() != feedbackTypeOnGroup) {
          experimentGroup.getFeedback().setType(feedbackTypeOnGroup);
          madeChange = true;
        }
      }
    }
    return madeChange;
  }

  @Override
  public boolean doMigration(String optionalcursor, DateTime startTime, DateTime endTime) {
    // load experiments
    boolean success = false;
    try {
      ExperimentService es = ExperimentServiceFactory.getExperimentService();
      String cursor = null;
      ExperimentQueryResult result = es.getAllExperiments(cursor);
      List<ExperimentDAO> experiments = result.getExperiments();
      for (ExperimentDAO experimentDAO : experiments) {
        if (repairFeedbacktypeAssignment(experimentDAO)) {
          es.saveExperiment(experimentDAO, AuthUtil.getWhoFromLogin().getEmail().toLowerCase(), null, true, true);
        }
      }
      success = true;
    } catch (Exception e) { 
      log.warning(ExceptionUtil.getStackTraceAsString(e));
      success = false;
    }
    return success;
  }

}
