package com.google.sampling.experiential.datastore;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.server.AuthUtil;
import com.google.sampling.experiential.server.DAOConverterOld;
import com.pacoapp.paco.shared.model.ExperimentDAO;
import com.pacoapp.paco.shared.model.SignalScheduleDAO;
import com.pacoapp.paco.shared.model.SignalingMechanismDAO;
import com.pacoapp.paco.shared.model.TriggerDAO;

public class ExperimentVersionEntity {
  private static final String DEFINITION_COLUMN = "definition";

  private static final String VERSION_COLUMN = "version";

  private static final String JDO_EXPERIMENT_ID_COLUMN = "jdoExperimentId";

  private static final String TITLE_COLUMN = "title";

  public static final Logger log = Logger.getLogger(ExperimentVersionEntity.class.getName());

  public static String EXPERIMENT_VERSION_KIND = "experiment_version";

//  public static Key saveExperimentVersionAsEntity(ExperimentDAO experiment) {
//    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
//    //Key key = KeyFactory.createKey(EXPERIMENT_KIND, experiment.getTitle());
//
//    if (experiment.getId() == null) {
//      log.severe("Experiment must have an id to be versioned in history table.");
//    }
//    Entity entity = new Entity(EXPERIMENT_VERSION_KIND);
//    entity.setProperty(TITLE_COLUMN, experiment.getTitle());
//    entity.setProperty(JDO_EXPERIMENT_ID_COLUMN, experiment.getId());
//    entity.setProperty(VERSION_COLUMN, experiment.getVersion());
//    Text experimentJson = new Text(JsonConverter.jsonify(experiment));
//    entity.setUnindexedProperty(DEFINITION_COLUMN, experimentJson);
//    Key key = ds.put(entity);
//    return key;
//  }

  public static List<Experiment> getExperiments() {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(EXPERIMENT_VERSION_KIND);
    QueryResultIterable<Entity> result = ds.prepare(query).asQueryResultIterable();
    List<Experiment> experiments = Lists.newArrayList();
    for (Entity entity : result) {
      ExperimentDAO experimentDAO = oldDAOfromSingleEntityJson((String)entity.getProperty(DEFINITION_COLUMN));

      Experiment experiment = new Experiment();
      DAOConverterOld.fromExperimentDAO(experimentDAO, experiment, AuthUtil.getWhoFromLogin());
      experiments.add(experiment);
    }
    return experiments;
  }

  public static ExperimentDAO oldDAOfromSingleEntityJson(String experimentJson) {
    ObjectMapper mapper1 = new ObjectMapper();
    mapper1.configure(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper1.getDeserializationConfig().addMixInAnnotations(SignalingMechanismDAO.class, SignalingMechanismDAOMixIn.class);
    ObjectMapper mapper = mapper1;
    try {
      ExperimentDAO experiment = mapper.readValue(experimentJson, new TypeReference<ExperimentDAO>() {});
      return experiment;
    } catch (JsonParseException e) {
      log.severe("Could not parse json. " + e.getMessage());
    } catch (JsonMappingException e) {
      log.severe("Could not parse json. " + e.getMessage());
    } catch (IOException e) {
      log.severe("Could not parse json. " + e.getMessage());
    }
    return null;
  }


  @JsonTypeInfo(
                use = JsonTypeInfo.Id.NAME,
                include = JsonTypeInfo.As.PROPERTY,
                property = "type")
            @JsonSubTypes({
                @Type(value = SignalScheduleDAO.class, name = "signalSchedule"),
                @Type(value = TriggerDAO.class, name = "trigger") })
  private class SignalingMechanismDAOMixIn
  {
    // Nothing to be done here. This class exists for the sake of its annotations.
  }


  public static Experiment getExperimentVersion(Long jdoExperimentId, Integer version) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(EXPERIMENT_VERSION_KIND);
    query.addFilter(JDO_EXPERIMENT_ID_COLUMN, FilterOperator.EQUAL, jdoExperimentId);
    query.addFilter(VERSION_COLUMN, FilterOperator.EQUAL, version);
    Entity result = ds.prepare(query).asSingleEntity();
    if (result == null) {
      return null;
    }
    ExperimentDAO experimentDAO = oldDAOfromSingleEntityJson((String)result.getProperty(DEFINITION_COLUMN));
    Experiment experiment = new Experiment();
    DAOConverterOld.fromExperimentDAO(experimentDAO, experiment, AuthUtil.getWhoFromLogin());
    return experiment;
  }
}
