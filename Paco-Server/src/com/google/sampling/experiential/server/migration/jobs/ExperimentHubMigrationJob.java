package com.google.sampling.experiential.server.migration.jobs;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.DateFormat;
import java.util.List;
import java.util.logging.Logger;

import com.google.sampling.experiential.server.ExperimentServiceFactory;
import com.pacoapp.paco.shared.model2.*;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.server.migration.MigrationJob;
import com.google.sampling.experiential.shared.TimeUtil;
import com.google.sampling.experiential.server.hub.HubStatsCronJob;

public class ExperimentHubMigrationJob implements MigrationJob {

    public static final Logger log = Logger.getLogger(EventStatsCounterMigrationJob.class.getName());

    public static String PUBLIC_EXPERIMENT_KIND = "public_experiment";
    private static final String MODIFY_DATE_PROPERTY ="modify_date";

    public boolean doMigrationPublicExperiments(){
        //1. Loop through public experiments and fill in "modify_date"

        ExperimentQueryResult experimentsQueryResults = ExperimentServiceFactory.getExperimentService().getAllExperiments(null);
        List<ExperimentDAO> experimentList = experimentsQueryResults.getExperiments();

        if (experimentList != null) {
            DateFormat df = new SimpleDateFormat(TimeUtil.DATE_FORMAT);
            List<Pair<Long, Date>> experimentsWithModifyDates = Lists.newArrayList();

            for (ExperimentDAO e : experimentList) {
                if (e.getPublished()) {
                    Date date;
                    try{
                        date = df.parse(e.getModifyDate());
                    }catch(ParseException ex){
                        log.info("Could not parse date for " + e.getId() + " " + ex.toString());
                        date = new Date(); //fallback to "now"
                    }

                    experimentsWithModifyDates.add(
                            new Pair<Long, Date>(e.getId(), date)
                    );
                }
            }

            updateDatastore(experimentsWithModifyDates);
        }

        //2. Run cron job to count participants in each experiment
        HubStatsCronJob cj = new HubStatsCronJob();
        try {
            cj.run();
        }catch(IOException e){
            log.warning("Exception occurred while running the HubStatsCronJob during Migration: " + e.toString());
        }

        return true;
    }

    private void updateDatastore(List<Pair<Long, Date>> experimentsWithModifyDates){
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        TransactionOptions options = TransactionOptions.Builder.withXG(true);

        final int bucketSize = 5;
        int count = 0;
        Transaction tx = tx = ds.beginTransaction(options);
        try {
            for (Pair<Long, Date> curExp : experimentsWithModifyDates) {
                count++;
                Key key = KeyFactory.createKey(PUBLIC_EXPERIMENT_KIND, curExp.first);
                Entity entity = ds.get(key);
                entity.setProperty(MODIFY_DATE_PROPERTY, curExp.second);
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

    @Override
    public boolean doMigration() {
        return doMigrationPublicExperiments();
    }

}
