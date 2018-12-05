package com.google.sampling.experiential.server.migration.jobs;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.common.base.Strings;
import com.google.sampling.experiential.datastore.ExperimentJsonEntityManager;
import com.google.sampling.experiential.server.ExperimentService;
import com.google.sampling.experiential.server.ExperimentServiceFactory;
import com.google.sampling.experiential.server.migration.MigrationJob;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentQueryResult;
import com.pacoapp.paco.shared.model2.JsonConverter;
import com.pacoapp.paco.shared.util.TimeUtil;

public class ExperimentTitleLowercaseMigrationJob implements MigrationJob {

  public static final Logger log = Logger.getLogger(ExperimentTitleLowercaseMigrationJob.class.getName());


  public boolean doMigrationPublicExperiments() {
    final ExperimentService experimentService = ExperimentServiceFactory.getExperimentService();

    ExperimentQueryResult experimentsQueryResults = experimentService.getAllExperiments(null);
    List<ExperimentDAO> experimentList = experimentsQueryResults.getExperiments();

    log.fine("Retrieved " + experimentList.size() + "experiments");

    if (experimentList == null || experimentList.isEmpty()) {
      return true;
    }

    ObjectMapper mapper = JsonConverter.getObjectMapper();

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Transaction tx = null; // not currently used. TODO clean up tx.
    int completed = 0;
    for (ExperimentDAO e : experimentList) {
      try {
        Long modifiedDateAsMillis;
        if (!Strings.isNullOrEmpty(e.getModifyDate())) {
          try {
            DateTime dateTime = TimeUtil.parseDateWithoutZone(e.getModifyDate());
            modifiedDateAsMillis = dateTime.getMillis();
          } catch (Exception e1) {
            modifiedDateAsMillis = new DateTime().getMillis();
          }
        } else {
          modifiedDateAsMillis = new DateTime().getMillis();
          e.setModifyDate(TimeUtil.formatDate(modifiedDateAsMillis));
        }
        ExperimentJsonEntityManager.saveExperiment(ds, tx, mapper.writeValueAsString(e), e.getId(), e.getTitle(), e.getVersion(), modifiedDateAsMillis, e.getAdmins());
        completed++;
      } catch (JsonGenerationException e1) {
        log.severe("JsonGenerationException: " + e1.getMessage() + ". Current Entity = " + e.getTitle() + ", " + e.getId() + ". Completed = " + completed);
      } catch (JsonMappingException e1) {
        e1.printStackTrace();
        log.severe("JsonMappingException: " + e1.getMessage() + ". Current Entity = " + e.getTitle() + ", " + e.getId() + ". Completed = " + completed);
      } catch (IOException e1) {
        log.severe("IOException: " + e1.getMessage() + ". Current Entity = " + e.getTitle() + ", " + e.getId() + ". Completed = " + completed);
      }
    }
    log.fine("Done with migration. Modified entity count: " + completed);
    return true;
  }

  @Override
  public boolean doMigration(String cursor, DateTime startTime, DateTime endTime) {
    return doMigrationPublicExperiments();
  }

}
