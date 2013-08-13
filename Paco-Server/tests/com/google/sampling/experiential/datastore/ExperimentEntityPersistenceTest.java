package com.google.sampling.experiential.datastore;

import java.util.List;

import junit.framework.TestCase;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.paco.shared.model.ExperimentDAO;

public class ExperimentEntityPersistenceTest extends TestCase {

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  public void setUp() {
    helper.setUp();
  }

  public void tearDown() {
    helper.tearDown();
  }

  public void testSaveExperimentAsEntityRetrieveByExperimentId() {
    String adminUser = "admin_man@example.com";
    String[] admins = new String[] {adminUser};
    ExperimentDAO experiment = new ExperimentDAO();
    experiment.setTitle("ex1");
    experiment.setAdmins(admins);
    Key key = ExperimentEntityPersistence.saveExperiment(experiment);

    ExperimentDAO retrievedExperiment = ExperimentEntityPersistence.getExperimentById(key.getId());
    assertNotNull(retrievedExperiment);
  }

  public void testSaveExperimentAsEntityRetrieveByAdmin() {
    String adminUser = "admin_man@example.com";
    String[] admins = new String[] {adminUser};
    ExperimentDAO experiment = new ExperimentDAO();
    experiment.setAdmins(admins);
    ExperimentEntityPersistence.saveExperiment(experiment);

    List<ExperimentDAO> experiments = ExperimentEntityPersistence.getExperimentsAdministeredBy(adminUser);
    assertEquals(1, experiments.size());
  }

  public void testSaveExperimentAsEntityRetrieveByPublished() {
    String adminUser = "admin_man@example.com";
    String[] admins = new String[] {adminUser};
    ExperimentDAO experiment = new ExperimentDAO();
    experiment.setAdmins(admins);

    String publishedUser = "pub_man@example.com";
    String[] publishedUsers = new String[] {publishedUser};
    experiment.setPublished(true);
    experiment.setPublishedUsers(publishedUsers);

    ExperimentEntityPersistence.saveExperiment(experiment);

    List<ExperimentDAO> experiments = ExperimentEntityPersistence.getExperimentsPublishedTo(publishedUser);
    assertEquals(1, experiments.size());

    experiments = ExperimentEntityPersistence.getExperimentsPublishedTo(null);
    assertEquals(0, experiments.size());

    experiment.setPublished(false);
    ExperimentEntityPersistence.saveExperiment(experiment);
    experiments = ExperimentEntityPersistence.getExperimentsPublishedTo(publishedUser);
    assertEquals(0, experiments.size());

  }

  public void testSaveExperimentAsEntityRetrieveByAdmin_TwoAdmins() {
    String adminUser = "admin_man@example.com";
    String adminUser2 = "admin_man2@example.com";
    String[] admins = new String[] {adminUser, adminUser2};
    ExperimentDAO experiment = new ExperimentDAO();
    experiment.setAdmins(admins);
    ExperimentEntityPersistence.saveExperiment(experiment);

    List<ExperimentDAO> experiments = ExperimentEntityPersistence.getExperimentsAdministeredBy(adminUser);
    assertEquals(1, experiments.size());

    experiments = ExperimentEntityPersistence.getExperimentsAdministeredBy(adminUser2);
    assertEquals(1, experiments.size());
  }


  public void testChangeExperimentAdmins() {
    String adminUser1 = "admin_man@example.com";
    String[] admins = new String[] {adminUser1};
    ExperimentDAO experiment = new ExperimentDAO();
    experiment.setAdmins(admins);
    ExperimentEntityPersistence.saveExperiment(experiment);

    List<ExperimentDAO> experiments = ExperimentEntityPersistence.getExperimentsAdministeredBy(adminUser1);
    assertEquals(1, experiments.size());

    String adminUser2 = "admin_woman@example.com";
    admins[0] = adminUser2;
    experiment.setAdmins(admins);
    ExperimentEntityPersistence.saveExperiment(experiment);

    experiments = ExperimentEntityPersistence.getExperimentsAdministeredBy(adminUser1);
    assertEquals(0, experiments.size());

    experiments = ExperimentEntityPersistence.getExperimentsAdministeredBy(adminUser2);
    assertEquals(1, experiments.size());

  }


  public void testChangeExperimentAdmins_TwoAdmins_OneChanged() {
    String adminUser1 = "admin_man@example.com";
    String adminUser2 = "admin_man2@example.com";
    String[] admins = new String[] {adminUser1, adminUser2};
    ExperimentDAO experiment = new ExperimentDAO();
    experiment.setAdmins(admins);
    ExperimentEntityPersistence.saveExperiment(experiment);

    List<ExperimentDAO> experiments = ExperimentEntityPersistence.getExperimentsAdministeredBy(adminUser1);
    assertEquals(1, experiments.size());

    String adminUser3 = "admin_woman@example.com";
    admins[0] = adminUser3;
    experiment.setAdmins(admins);
    ExperimentEntityPersistence.saveExperiment(experiment);

    experiments = ExperimentEntityPersistence.getExperimentsAdministeredBy(adminUser1);
    assertEquals(0, experiments.size());

    experiments = ExperimentEntityPersistence.getExperimentsAdministeredBy(adminUser2);
    assertEquals(1, experiments.size());

    experiments = ExperimentEntityPersistence.getExperimentsAdministeredBy(adminUser3);
    assertEquals(1, experiments.size());

  }

}
