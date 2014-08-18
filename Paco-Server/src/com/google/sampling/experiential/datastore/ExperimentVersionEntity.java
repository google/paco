package com.google.sampling.experiential.datastore;

import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
import com.google.common.collect.Lists;

import com.google.paco.shared.model.ExperimentDAO;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.server.DAOConverter;

public class ExperimentVersionEntity {
  private static final String DEFINITION_COLUMN = "definition";

  private static final String VERSION_COLUMN = "version";

  private static final String JDO_EXPERIMENT_ID_COLUMN = "jdoExperimentId";

  private static final String TITLE_COLUMN = "title";

  public static final Logger log = Logger.getLogger(ExperimentVersionEntity.class.getName());

  public static String EXPERIMENT_VERSION_KIND = "experiment_version";
  
  public static Key saveExperimentAsEntity(Experiment experiment) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    //Key key = KeyFactory.createKey(EXPERIMENT_KIND, experiment.getTitle());
    
    if (experiment.getId() == null) {
      log.severe("Experiment must have an id to be versioned in history table.");
    } 
    Entity entity = new Entity(EXPERIMENT_VERSION_KIND);
    entity.setProperty(TITLE_COLUMN, experiment.getTitle());
    entity.setProperty(JDO_EXPERIMENT_ID_COLUMN, experiment.getId());
    entity.setProperty(VERSION_COLUMN, experiment.getVersion());
    Text experimentJson = new Text(JsonConverter.jsonify(DAOConverter.createDAO(experiment)));
    entity.setUnindexedProperty(DEFINITION_COLUMN, experimentJson);
    Key key = ds.put(entity);
    return key;    
  }
  
  public static List<Experiment> getExperiments(User loggedInUser) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(EXPERIMENT_VERSION_KIND);
    QueryResultIterable<Entity> result = ds.prepare(query).asQueryResultIterable();
    List<Experiment> experiments = Lists.newArrayList();
    for (Entity entity : result) {
      ExperimentDAO experimentDAO = JsonConverter.fromSingleEntityJson((String)entity.getProperty(DEFINITION_COLUMN));
      
      Experiment experiment = new Experiment();
      DAOConverter.fromExperimentDAO(experimentDAO, experiment, loggedInUser);
      experiments.add(experiment);
    }
    return experiments;
  }
  
  public static Experiment getExperimentVersion(User loggedInUser, Long jdoExperimentId, Integer version) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(EXPERIMENT_VERSION_KIND);
    query.addFilter(JDO_EXPERIMENT_ID_COLUMN, FilterOperator.EQUAL, jdoExperimentId);
    query.addFilter(VERSION_COLUMN, FilterOperator.EQUAL, version);
    Entity result = ds.prepare(query).asSingleEntity();
    if (result == null) {
      return null;
    }
    ExperimentDAO experimentDAO = JsonConverter.fromSingleEntityJson((String)result.getProperty(DEFINITION_COLUMN));
    Experiment experiment = new Experiment();
    DAOConverter.fromExperimentDAO(experimentDAO, experiment, loggedInUser);
    
    return experiment;
  }
}
