package com.google.sampling.experiential.server;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.appengine.api.users.User;
import com.google.paco.shared.model2.ExperimentDAO;
import com.google.paco.shared.model2.ExperimentQueryResult;
import com.google.paco.shared.model2.ValidationMessage;


/**
 * How objects get access to the retrieve and persist Experiments in the datastore.
 *
 * Note, we can return json from get calls, but, usually it will be easier to save
 * with ExperimentDAO objects as they give us easy access to update permissions tables
 * and any other tables we might need for storing more-easily queryable information.
 *
 */
public interface ExperimentService {

  // retrieving experiments
  // Note: DAOs are here for internal use and for the GWT client. Remote clients will use the AsJson api.
  ExperimentDAO getExperiment(Long id);
  String getExperimentAsJson(Long id);
  List<ExperimentDAO> getExperimentsById(List<Long> experimentIds, String email, DateTimeZone timezone);
  List<String> getExperimentsByIdAsJson(List<Long> experimentIds, String email, DateTimeZone timezone);

  // saving experiments
  List<ValidationMessage> saveExperiment(ExperimentDAO experimentDAO, User userFromLogin, DateTimeZone timezone);

  //delete experiments
  Boolean deleteExperiment(ExperimentDAO experimentDAO, String loggedInUserEmail);
  Boolean deleteExperiment(Long experimentId, String loggedInUserEmail);

  // Specific queries for clients
  // Note currently ExperimentQueryResult returns a cursor and a set of DAOs.
  // We should provide an ExperimentQueryResult that sends Json straight through without the unnecessary (db json) -> local DAO -> json translation steps
  ExperimentQueryResult getAllJoinableExperiments(String lowerCase, DateTimeZone timeZoneForClient, Integer limit,
                                                  String cursor);

  ExperimentQueryResult getMyJoinableExperiments(String lowerCase, DateTimeZone timeZoneForClient, Integer limit,
                                                 String cursor);

  ExperimentQueryResult getUsersAdministeredExperiments(String email, DateTimeZone timezone, Integer limit,
                                                        String cursor);

  ExperimentQueryResult getExperimentsPublishedPublicly(DateTimeZone timezone, Integer limit, String cursor);


  // referred experiments are used for linked end of day studies
  ExperimentDAO getReferredExperiment(long referredId);
  void setReferredExperiment(Long referringExperimentId, Long referencedExperimentId);



  // TODO maybe this should be in a cross-platform Utility class?
  boolean isOver(ExperimentDAO experiment, DateTime today);


}
