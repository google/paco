package com.google.sampling.experiential.server.hub;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import com.google.sampling.experiential.server.KeyServlet;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.server.ExperimentServiceFactory;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentQueryResult;
import com.pacoapp.paco.shared.model2.Pair;
import com.google.sampling.experiential.server.stats.participation.ParticipationStatsService;


/**
 * This class is called by a scheduled Task weekly and updates the stats used in the
 * Experiment Hub
 *
 *
 *
 */
public class HubStatsCronJob {

    private static final Logger log = Logger.getLogger(HubStatsCronJob.class.getName());
    private String adminDomainSystemSetting;

    public static String PUBLIC_EXPERIMENT_KIND = "public_experiment";
    private static final String STATS_PARTICIPANTS_PROPERTY ="stats_participants";

    private static HubStatsCronJob instance;

    public static HubStatsCronJob getInstance() {
        if (instance == null) {
            instance = new HubStatsCronJob();
        }
        return instance;
    }

    public HubStatsCronJob() {
    }

    public void run() throws IOException {
        log.info("writing hub stats");

        // Update participant count for each public experiment
        ExperimentQueryResult experimentsQueryResults = ExperimentServiceFactory.getExperimentService().getAllExperiments(null);
        List<ExperimentDAO> experimentList = experimentsQueryResults.getExperiments();

        if (experimentList != null) {
            DateTime dateTime = DateTime.now();
            List<Pair<Long, Integer>> participantsByExperiment = Lists.newArrayList();

            ParticipationStatsService ps = new ParticipationStatsService();

            for (ExperimentDAO e : experimentList) {
                if (e.getPublished()) {
                    participantsByExperiment.add(
                            new Pair<Long, Integer>(e.getId(), ps.getTotalByParticipant(e.getId()).size() )
                    );
                }
            }

            updateDatastore(participantsByExperiment);
        }
    }

    private void updateDatastore(List<Pair<Long, Integer>> participantsByExperiment){
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        TransactionOptions options = TransactionOptions.Builder.withXG(true);

        final int bucketSize = 5;
        int count = 0;
        Transaction tx = tx = ds.beginTransaction(options);
        try {
            for (Pair<Long, Integer> curExp : participantsByExperiment) {
                count++;
                Key key = KeyFactory.createKey(PUBLIC_EXPERIMENT_KIND, curExp.first);
                Entity entity = ds.get(key);
                entity.setProperty(STATS_PARTICIPANTS_PROPERTY, curExp.second.longValue());
                ds.put(entity);
                if(count % bucketSize == 0){
                    tx.commit();
                    tx = ds.beginTransaction(options);
                }
            }
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
        }
    }

}
