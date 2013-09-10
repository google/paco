package com.google.sampling.experiential.datastore;

import java.util.List;

import junit.framework.TestCase;

import org.joda.time.DateTime;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.paco.shared.model.ExperimentDAO;

public class ExperimentEntityPersistenceTest extends TestCase {

  private static final String EXPERIMENT_TITLE = "ex1";
  private static final String ADMIN_WOMAN_EXAMPLE_COM = "admin_woman@example.com";
  private static final String ADMIN_MAN2_EXAMPLE_COM = "admin_man2@example.com";
  private static final String PUB_MAN_EXAMPLE_COM = "pub_man@example.com";
  private static final String ADMIN_MAN_EXAMPLE_COM = "admin_man@example.com";

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  public void setUp() {
    helper.setUp();
  }

  public void tearDown() {
    helper.tearDown();
  }

  public void testSaveExperimentAsEntityRetrieveByExperimentId() {
    String adminUser = ADMIN_MAN_EXAMPLE_COM;
    String[] admins = new String[] {adminUser};
    ExperimentDAO experiment = new ExperimentDAO();
    experiment.setTitle(EXPERIMENT_TITLE);
    experiment.setAdmins(admins);
    experiment.setCreator(adminUser);
    Key key = ExperimentEntityPersistence.saveExperiment(experiment, adminUser);

    ExperimentDAO retrievedExperiment = ExperimentEntityPersistence.getExperimentById(key.getId());
    assertNotNull(retrievedExperiment);
  }

  public void testSaveExperimentAsEntityRetrieveByAdmin() {
    String adminUser = ADMIN_MAN_EXAMPLE_COM;
    String[] admins = new String[] {adminUser};
    ExperimentDAO experiment = new ExperimentDAO();
    experiment.setTitle(EXPERIMENT_TITLE);
    experiment.setAdmins(admins);
    experiment.setCreator(adminUser);
    ExperimentEntityPersistence.saveExperiment(experiment, adminUser);

    List<ExperimentDAO> experiments = ExperimentEntityPersistence.getExperimentsAdministeredBy(adminUser);
    assertEquals(1, experiments.size());
  }

  public void testSaveExperimentAsEntityRetrieveByPublished() {
    String adminUser = ADMIN_MAN_EXAMPLE_COM;
    String[] admins = new String[] {adminUser};
    ExperimentDAO experiment = new ExperimentDAO();
    experiment.setTitle(EXPERIMENT_TITLE);
    experiment.setAdmins(admins);
    experiment.setCreator(adminUser);

    String publishedUser = PUB_MAN_EXAMPLE_COM;
    String[] publishedUsers = new String[] {publishedUser};
    experiment.setPublished(true);
    experiment.setPublishedUsers(publishedUsers);

    ExperimentEntityPersistence.saveExperiment(experiment, adminUser);

    List<ExperimentDAO> experiments = ExperimentEntityPersistence.getExperimentsPublishedTo(publishedUser);
    assertEquals(1, experiments.size());

    experiments = ExperimentEntityPersistence.getExperimentsPublishedTo(null);
    assertEquals(0, experiments.size());

    experiment.setPublished(false);
    ExperimentEntityPersistence.saveExperiment(experiment, adminUser);
    experiments = ExperimentEntityPersistence.getExperimentsPublishedTo(publishedUser);
    assertEquals(0, experiments.size());

  }

  public void testSaveExperimentAsEntityRetrieveByAdmin_TwoAdmins() {
    String adminUser = ADMIN_MAN_EXAMPLE_COM;
    String adminUser2 = ADMIN_MAN2_EXAMPLE_COM;
    String[] admins = new String[] {adminUser, adminUser2};
    ExperimentDAO experiment = new ExperimentDAO();
    experiment.setTitle(EXPERIMENT_TITLE);
    experiment.setAdmins(admins);
    experiment.setCreator(adminUser);
    ExperimentEntityPersistence.saveExperiment(experiment, adminUser);

    List<ExperimentDAO> experiments = ExperimentEntityPersistence.getExperimentsAdministeredBy(adminUser);
    assertEquals(1, experiments.size());

    experiments = ExperimentEntityPersistence.getExperimentsAdministeredBy(adminUser2);
    assertEquals(1, experiments.size());
  }


  public void testChangeExperimentAdmins() {
    String adminUser1 = ADMIN_MAN_EXAMPLE_COM;
    String[] admins = new String[] {adminUser1};
    ExperimentDAO experiment = new ExperimentDAO();
    experiment.setTitle(EXPERIMENT_TITLE);
    experiment.setAdmins(admins);
    experiment.setCreator(adminUser1);
    ExperimentEntityPersistence.saveExperiment(experiment, adminUser1);

    List<ExperimentDAO> experiments = ExperimentEntityPersistence.getExperimentsAdministeredBy(adminUser1);
    assertEquals(1, experiments.size());

    String adminUser2 = ADMIN_WOMAN_EXAMPLE_COM;
    admins[0] = adminUser2;
    experiment.setAdmins(admins);
    ExperimentEntityPersistence.saveExperiment(experiment, adminUser1);

    experiments = ExperimentEntityPersistence.getExperimentsAdministeredBy(adminUser1);
    assertEquals(1, experiments.size());

    experiments = ExperimentEntityPersistence.getExperimentsAdministeredBy(adminUser2);
    assertEquals(1, experiments.size());
  }

  public void testChangeExperimentAdmins_TwoAdmins_OneChanged() {
    String adminUser1 = ADMIN_MAN_EXAMPLE_COM;
    String adminUser2 = ADMIN_MAN2_EXAMPLE_COM;
    String[] admins = new String[] {adminUser1, adminUser2};
    ExperimentDAO experiment = new ExperimentDAO();
    experiment.setTitle(EXPERIMENT_TITLE);
    experiment.setAdmins(admins);
    experiment.setCreator(adminUser1);
    ExperimentEntityPersistence.saveExperiment(experiment, adminUser1);

    List<ExperimentDAO> experiments = ExperimentEntityPersistence.getExperimentsAdministeredBy(adminUser1);
    assertEquals(1, experiments.size());

    String adminUser3 = ADMIN_WOMAN_EXAMPLE_COM;
    admins[0] = adminUser3;
    experiment.setAdmins(admins);
    ExperimentEntityPersistence.saveExperiment(experiment, adminUser2);

    experiments = ExperimentEntityPersistence.getExperimentsAdministeredBy(adminUser1);
    assertEquals(1, experiments.size());

    experiments = ExperimentEntityPersistence.getExperimentsAdministeredBy(adminUser2);
    assertEquals(1, experiments.size());

    experiments = ExperimentEntityPersistence.getExperimentsAdministeredBy(adminUser3);
    assertEquals(1, experiments.size());

  }

  public void testSaveExperimentDeleteExperiment() {
    String adminUser = ADMIN_MAN_EXAMPLE_COM;
    String[] admins = new String[] {adminUser};
    ExperimentDAO experiment = new ExperimentDAO();
    experiment.setTitle(EXPERIMENT_TITLE);
    experiment.setAdmins(admins);
    experiment.setCreator(adminUser);
    Key key = ExperimentEntityPersistence.saveExperiment(experiment, adminUser);

    ExperimentDAO retrievedExperiment = ExperimentEntityPersistence.getExperimentById(key.getId());
    assertNotNull(retrievedExperiment);

    ExperimentEntityPersistence.deleteExperiment(retrievedExperiment, adminUser);
    ExperimentDAO reRetrievedExperiment = ExperimentEntityPersistence.getExperimentById(key.getId());
    assertNull(reRetrievedExperiment);

    List<ExperimentDAO> adminedExperiments = ExperimentEntityPersistence.getExperimentsAdministeredBy(adminUser);
    assertEquals(0, adminedExperiments.size());
  }

  public void testDeleteNonExistent() {
    try {
      ExperimentEntityPersistence.deleteExperiment(new ExperimentDAO(), ADMIN_MAN_EXAMPLE_COM);
      fail("should have thrown illegalstateexception for null experiment id");
    } catch (IllegalStateException is) {
    }
  }

  public void testDeleteNonExistentWithId() {
    try {
      ExperimentDAO experimentDAO = new ExperimentDAO();
      experimentDAO.setId(190l);
      ExperimentEntityPersistence.deleteExperiment(experimentDAO, ADMIN_MAN_EXAMPLE_COM);
      fail("should have thrown illegalstateexception for null experiment id");
    } catch (IllegalStateException is) {
    }
  }

  public void testPublishedToAll() {
    String adminUser = ADMIN_MAN_EXAMPLE_COM;
    String[] admins = new String[] {adminUser};
    ExperimentDAO experiment = new ExperimentDAO();
    experiment.setTitle(EXPERIMENT_TITLE);
    experiment.setAdmins(admins);
    experiment.setCreator(adminUser);
    experiment.setPublished(true);
    experiment.setPublishedUsers(new String[0]);
    Key key = ExperimentEntityPersistence.saveExperiment(experiment, adminUser);

    List<ExperimentDAO> retrievedExperiments = ExperimentEntityPersistence.getExperimentsPublishedToAll(new DateTime().getZone());
    assertNotNull(retrievedExperiments);
    assertEquals(1, retrievedExperiments.size());
  }



}
