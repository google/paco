package com.google.sampling.experiential.server.migration.jobs;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.joda.time.DateTime;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.sampling.experiential.server.PMF;
import com.google.sampling.experiential.server.migration.MigrationJob;

public class TestJDODSCompat implements MigrationJob {

  @Override
  public boolean doMigration(String optionalCursor, DateTime startTime, DateTime endTime) {
    // create JDO for a kind with sub kinds
    PersistenceManagerFactory pmf = PMF.get();
    PersistenceManager pm = pmf.getPersistenceManager();
    System.out.println("Creating jdo");
    TestTable testTable = new TestTable("row one", new Date());
    pm.makePersistent(testTable);
    System.out.println("row one id: " + testTable.getId());
    pm.close();
    System.out.println("Done creating jdo");

    System.out.println("Retrieving jdo object with ds");
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("TestTable");
    PreparedQuery q = ds.prepare(query);
    List<Entity> res = q.asList(FetchOptions.Builder.withDefaults());
    System.out.println("Assertion: res.size() == 1: " + (res.size() == 1));
    Entity one = res.get(0);
    System.out.println("Entity props : ");
    System.out.println("Kind: " + one.getKind());
    System.out.println("Id: " + one.getKey().getId());
    Map<String, Object> keys = one.getProperties();
    System.out.println("Properties:");
    for (String key : keys.keySet()) {
        Object value = keys.get(key);
        System.out.println(key + " : " + value);
    }


    System.out.println("Modifying jdo with ds");
    one.setProperty("name", "changedName");
    Key changed = ds.put(one);
    System.out.println("ChangedName edit key = " + changed.getId());

    System.out.println("Creating new entity for same id with different properties set");
    Entity newOne = new Entity("TestTable", one.getKey().getId());
    newOne.setProperty("name", "changed again");
    ds.put(newOne);

    // read with both to verify correctness
    System.out.println("retrieving jdo again");
    pm = pmf.getPersistenceManager();
    //TestTable oneSecond = (TestTable) pm.getObjectById(testTable.getId());
    javax.jdo.Query qAgain = pm.newQuery(TestTable.class);
    List<TestTable> jdoAgain = (List<com.google.sampling.experiential.server.migration.jobs.TestTable>) qAgain.execute();
    System.out.println("jdoAgain size == " + jdoAgain.size());
    TestTable oneSecond = jdoAgain.get(0);
    System.out.println("Got oneSecond: " + oneSecond.getName() + ", " + oneSecond.getId());
    pm.close();

    System.out.println("retrieving DS again");

    try {
      Entity oneSecondDS = ds.get(KeyFactory.createKey("TestTable", oneSecond.getId()));
      System.out.println("Got oneSecondDS: " + (oneSecondDS != null));

    } catch (EntityNotFoundException e) {
      e.printStackTrace();
    }

    System.out.println("Creating entity with low-level first, missing props");
    Entity two = new Entity("TestTable");
    two.setProperty("name", "row two");
    two.setProperty("json", "{\"name\" : \"row two\", \"modifyDate\" : \"2015-04-28\"}");
    Key resTwo = ds.put(two);
    System.out.println("Saved row-two with id: " + resTwo.getId());

    System.out.println("Retrieving row-two with jdo");
    pm = pmf.getPersistenceManager();
    //TestTable oneSecond = (TestTable) pm.getObjectById(testTable.getId());
    javax.jdo.Query qTwo = pm.newQuery(TestTable.class);
    qTwo.setFilter("id == idParam");
    qTwo.declareParameters("Long idParam");
    List<TestTable> jdoTwo = (List<com.google.sampling.experiential.server.migration.jobs.TestTable>) qTwo.execute(resTwo.getId());
    System.out.println("jdoTwo size == " + jdoTwo.size());
    TestTable twoJdo = jdoTwo.get(0);
    System.out.println("Got twoJdo: " + twoJdo.getName() + ", " + twoJdo.getModifyDate() + ", " + twoJdo.getId());
    pm.close();


    return true;
  }

}
