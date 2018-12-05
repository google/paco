package com.google.sampling.experiential.server;

import java.util.ArrayList;
import java.util.Map;

import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.google.sampling.experiential.cloudsql.columns.EventServerColumns;
import com.pacoapp.paco.shared.model2.EventBaseColumns;
import com.pacoapp.paco.shared.model2.OutputBaseColumns;
import com.pacoapp.paco.shared.util.QueryPreprocessor;
import com.pacoapp.paco.shared.util.SearchUtil;

import junit.framework.TestCase;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.statement.select.Select;

public class ACLHelperTest extends TestCase {
  String userInHttpRequest = null;
  String userWhoIsAdmin = null;
  String userWhoIsOnlyParticipant = null;
  ArrayList<Long> expListinDBForAdmin = new ArrayList<Long>();
  ArrayList<Long> expListinDBForParticipant = new ArrayList<Long>();
  private static Map<String, Class> validColumnNamesDataTypeInDb = Maps.newHashMap();
  private static final String ID = "_id";
  private DateTimeZone dtz = DateTimeZone.forID("America/Los_Angeles");
  ArrayList<String> reqDateColumns = null;
  private boolean webRequest = false;

  // User 'admin1' is an admin of expts expt 1,2,3
  // S1 : no exp id clause, no who clause
  // if admin, no expt id, so no processing
  String actualS1Qry = "select * from events ";

  // S2 : exp id clause includes all in db, no who clause
  // if admin, no change
  String actualS2Qry = "select * from events where experiment_id in(1, 2, 3)";
  String expectedS2Qry = "select * from events where experiment_id in (1, 2, 3)";

  // S3 : exp id clause includes some in db as admin, some not in db as admin no
  // who clause
  // if admin, mixed acl without who, so no processing
  String actualS3Qry = "select * from events where experiment_id in(1, 2, 3, 4)";

  // S4 : who is loggedin user, no expt id clause
  // if admin, no expt id, so no processing
  String actualS4Qry = "select * from events where who = 'admin1'";

  // S5a : who list contains loggedin user and not loggedin admin, no expt id
  // clause
  // if admin, no expt id, so no processing
  String actualS5aQry = "select * from events where who = 'admin1' or who ='admin2'";

  // S5b : who list contains loggedin user and not participant, no expt id
  // clause
  // if admin, no expt id, so no processing
  String actualS5bQry = "select * from events where who = 'admin1' or who ='participant1'";

  // S6 : who list contains same loggedin user multiple times, no expt id clause
  // if admin, no expt id, so no processing
  String actualS6Qry = "select * from events where (who = 'admin1' and experiment_version=40) or (who='admin1' and experiment_version=42)";

  // S7 : expt id list as in db admin list, who list is same as loggedin user
  // if admin, no change
  String actualS7Qry = "select * from events where experiment_id in(1, 2, 3) and who='admin1'";
  String expectedS7Qry = "select * from events where experiment_id in (1, 2, 3) and who = 'admin1'";

  // S8a : expt id list as in db admin list, who list contains loggedin admin
  // user and some random user (hopefully a participant)
  // if admin, no change
  String actualS8aQry = "select * from events where experiment_id in(1, 2, 3) and (who='admin1' or who='participant1')";
  String expectedS8aQry = "select * from events where experiment_id in (1, 2, 3) and (who = 'admin1' or who = 'participant1')";

  // S9 : expt id list as in db admin list, who list contains same loggedin user
  // multiple times
  // if admin, no change
  String actualS9Qry = "select * from events where experiment_id in(1, 2, 3) and who='admin1' or (who='admin1' and experiment_version=40)";
  String expectedS9Qry = "select * from events where experiment_id in (1, 2, 3) and who = 'admin1' or (who = 'admin1' and experiment_version = 40)";

  // S10: exp id clause includes some in db as admin, some not in db as admin,
  // who list contains same loggedin user
  // if admin, no change
  String actualS10Qry = "select * from events where experiment_id in(1, 2, 3, 4) and who='admin1'";
  String expectedS10Qry = "select * from events where experiment_id in (1, 2, 3, 4) and who = 'admin1'";

  // S11: exp id clause includes some in db as admin, some not in db as admin,
  // who list contains loggedin user and not loggedin user
  // if admin, mixed acl with 'who' who is not the loggedin user
  String actualS11Qry = "select * from events where experiment_id in(1, 2, 3, 4) and who in('admin1','admin2','participant1')";

  // S12: exp id clause includes some in db as admin, some not in db as admin,
  // who list contains same loggedin user multiple times
  // if admin, no change
  String actualS12Qry = "select * from events where experiment_id in(1, 2, 3, 4) and (who = 'admin1' and experiment_version=40) or (who='admin1' and experiment_version=42)";
  String expectedS12Qry = "select * from events where experiment_id in (1, 2, 3, 4) and (who = 'admin1' and experiment_version = 40) or (who = 'admin1' and experiment_version = 42)";

  @Test
  public void testS1() {
    try {
      Select selStmt = SearchUtil.getJsqlSelectStatement(actualS1Qry);
      QueryPreprocessor qp = new QueryPreprocessor(selStmt, validColumnNamesDataTypeInDb, webRequest, reqDateColumns);
      ACLHelper.getModifiedQueryBasedOnACL(actualS1Qry, userWhoIsAdmin, expListinDBForAdmin, qp);
    } catch (Exception e) {
      assertTrue(e.getMessage().startsWith("Unauthorized access"));
    }
  }

  @Test
  public void testS2() {
    String actualQuery;
    try {
      Select selStmt = SearchUtil.getJsqlSelectStatement(actualS2Qry);
      QueryPreprocessor qp = new QueryPreprocessor(selStmt, validColumnNamesDataTypeInDb, webRequest, reqDateColumns);
      actualQuery = ACLHelper.getModifiedQueryBasedOnACL(actualS2Qry, userWhoIsAdmin, expListinDBForAdmin, qp);
      assertTrue(expectedS2Qry.equalsIgnoreCase(actualQuery));
    } catch (Exception e) {
      assertFalse(e != null);
    }
  }

  @Test
  public void testS3() {
    try {
      Select selStmt = SearchUtil.getJsqlSelectStatement(actualS3Qry);
      QueryPreprocessor qp = new QueryPreprocessor(selStmt, validColumnNamesDataTypeInDb, webRequest, reqDateColumns);
      ACLHelper.getModifiedQueryBasedOnACL(actualS3Qry, userWhoIsAdmin, expListinDBForAdmin, qp);
    } catch (Exception e) {
      assertTrue(e.getMessage().startsWith("Unauthorized access"));
    }
  }

  @Test
  public void testS4() {
    try {
      Select selStmt = SearchUtil.getJsqlSelectStatement(actualS4Qry);
      QueryPreprocessor qp = new QueryPreprocessor(selStmt, validColumnNamesDataTypeInDb, webRequest, reqDateColumns);
      ACLHelper.getModifiedQueryBasedOnACL(actualS4Qry, userWhoIsAdmin, expListinDBForAdmin, qp);
    } catch (Exception e) {
      assertTrue(e.getMessage().startsWith("Unauthorized access"));
    }
  }

  @Test
  public void testS5a() {
    try {
      Select selStmt = SearchUtil.getJsqlSelectStatement(actualS5aQry);
      QueryPreprocessor qp = new QueryPreprocessor(selStmt, validColumnNamesDataTypeInDb, webRequest, reqDateColumns);
      ACLHelper.getModifiedQueryBasedOnACL(actualS5aQry, userWhoIsAdmin, expListinDBForAdmin, qp);
    } catch (Exception e) {
      assertTrue(e.getMessage().startsWith("Unauthorized access"));
    }
  }

  @Test
  public void testS5b() {
    try {
      Select selStmt = SearchUtil.getJsqlSelectStatement(actualS5bQry);
      QueryPreprocessor qp = new QueryPreprocessor(selStmt, validColumnNamesDataTypeInDb, webRequest, reqDateColumns);
      ACLHelper.getModifiedQueryBasedOnACL(actualS5bQry, userWhoIsAdmin, expListinDBForAdmin, qp);
    } catch (Exception e) {
      assertTrue(e.getMessage().startsWith("Unauthorized access"));
    }
  }

  @Test
  public void testS6() {
    try {
      Select selStmt = SearchUtil.getJsqlSelectStatement(actualS6Qry);
      QueryPreprocessor qp = new QueryPreprocessor(selStmt, validColumnNamesDataTypeInDb, webRequest, reqDateColumns);
      ACLHelper.getModifiedQueryBasedOnACL(actualS6Qry, userWhoIsAdmin, expListinDBForAdmin, qp);
    } catch (Exception e) {
      assertTrue(e.getMessage().startsWith("Unauthorized access"));
    }
  }

  @Test
  public void testS7() {
    String actualQuery;
    try {
      Select selStmt = SearchUtil.getJsqlSelectStatement(actualS7Qry);
      QueryPreprocessor qp = new QueryPreprocessor(selStmt, validColumnNamesDataTypeInDb, webRequest, reqDateColumns);
      actualQuery = ACLHelper.getModifiedQueryBasedOnACL(actualS7Qry, userWhoIsAdmin, expListinDBForAdmin, qp);
      assertTrue(actualQuery.equalsIgnoreCase(expectedS7Qry));
    } catch (Exception e) {
      assertFalse(e != null);
    }
  }

  @Test
  public void testS8a() {
    String actualQuery;
    try {
      Select selStmt = SearchUtil.getJsqlSelectStatement(actualS8aQry);
      QueryPreprocessor qp = new QueryPreprocessor(selStmt, validColumnNamesDataTypeInDb, webRequest, reqDateColumns);
      actualQuery = ACLHelper.getModifiedQueryBasedOnACL(actualS8aQry, userWhoIsAdmin, expListinDBForAdmin, qp);
    } catch (Exception e) {
      assertTrue(e.getMessage().startsWith("Unauthorized access"));
    }
  }

  @Test
  public void testS9() {
    String actualQuery;
    try {
      Select selStmt = SearchUtil.getJsqlSelectStatement(actualS9Qry);
      QueryPreprocessor qp = new QueryPreprocessor(selStmt, validColumnNamesDataTypeInDb, webRequest, reqDateColumns);
      actualQuery = ACLHelper.getModifiedQueryBasedOnACL(actualS9Qry, userWhoIsAdmin, expListinDBForAdmin, qp);
      assertTrue(actualQuery.equalsIgnoreCase(expectedS9Qry));
    } catch (Exception e) {
      assertFalse(e != null);
    }
  }

  @Test
  public void testS10() {
    String actualQuery;
    try {
      Select selStmt = SearchUtil.getJsqlSelectStatement(actualS10Qry);
      QueryPreprocessor qp = new QueryPreprocessor(selStmt, validColumnNamesDataTypeInDb, webRequest, reqDateColumns);
      actualQuery = ACLHelper.getModifiedQueryBasedOnACL(actualS10Qry, userWhoIsAdmin, expListinDBForAdmin, qp);
      assertTrue(actualQuery.equalsIgnoreCase(expectedS10Qry));
    } catch (Exception e) {
      assertFalse(e != null);
    }
  }

  @Test
  public void testS11() {
    try {
      Select selStmt = SearchUtil.getJsqlSelectStatement(actualS11Qry);
      QueryPreprocessor qp = new QueryPreprocessor(selStmt, validColumnNamesDataTypeInDb, webRequest, reqDateColumns);
      ACLHelper.getModifiedQueryBasedOnACL(actualS11Qry, userWhoIsAdmin, expListinDBForAdmin, qp);
    } catch (Exception e) {
      assertTrue(e.getMessage().startsWith("Unauthorized access"));
    }
  }

  @Test
  public void testS12() {
    String actualQuery;
    try {
      Select selStmt = SearchUtil.getJsqlSelectStatement(actualS12Qry);
      QueryPreprocessor qp = new QueryPreprocessor(selStmt, validColumnNamesDataTypeInDb, webRequest, reqDateColumns);
      actualQuery = ACLHelper.getModifiedQueryBasedOnACL(actualS12Qry, userWhoIsAdmin, expListinDBForAdmin, qp);
      assertTrue(actualQuery.equalsIgnoreCase(expectedS12Qry));
    } catch (Exception e) {
      assertFalse(e != null);
    }
  }

  @Before
  public void setUp() {

    userInHttpRequest = "admin1";
    userWhoIsAdmin = "admin1";
    userWhoIsOnlyParticipant = "participant1";

    expListinDBForAdmin.add(1L);
    expListinDBForAdmin.add(2L);
    expListinDBForAdmin.add(3L);
    loadColInfo();

  }

  private void loadColInfo() {
    if (validColumnNamesDataTypeInDb.size() == 0) {

      validColumnNamesDataTypeInDb.put(ID, LongValue.class);

      validColumnNamesDataTypeInDb.put(EventBaseColumns.EXPERIMENT_ID, LongValue.class);
      validColumnNamesDataTypeInDb.put(EventBaseColumns.EXPERIMENT_NAME, StringValue.class);
      validColumnNamesDataTypeInDb.put(EventBaseColumns.EXPERIMENT_VERSION, LongValue.class);
      validColumnNamesDataTypeInDb.put(EventBaseColumns.SCHEDULE_TIME, StringValue.class);

      validColumnNamesDataTypeInDb.put(EventBaseColumns.RESPONSE_TIME, StringValue.class);
      validColumnNamesDataTypeInDb.put(EventBaseColumns.GROUP_NAME, StringValue.class);
      validColumnNamesDataTypeInDb.put(EventBaseColumns.ACTION_TRIGGER_ID, LongValue.class);
      validColumnNamesDataTypeInDb.put(EventBaseColumns.ACTION_TRIGGER_SPEC_ID, LongValue.class);
      validColumnNamesDataTypeInDb.put(EventBaseColumns.ACTION_ID, LongValue.class);

      validColumnNamesDataTypeInDb.put(EventServerColumns.WHO, StringValue.class);
      validColumnNamesDataTypeInDb.put(EventServerColumns.WHEN, StringValue.class);
      validColumnNamesDataTypeInDb.put(EventServerColumns.PACO_VERSION, LongValue.class);
      validColumnNamesDataTypeInDb.put(EventServerColumns.APP_ID, StringValue.class);
      validColumnNamesDataTypeInDb.put(EventServerColumns.JOINED, LongValue.class);

      validColumnNamesDataTypeInDb.put(EventServerColumns.SORT_DATE, StringValue.class);
      validColumnNamesDataTypeInDb.put(EventServerColumns.CLIENT_TIME_ZONE, StringValue.class);
      validColumnNamesDataTypeInDb.put(OutputBaseColumns.NAME, StringValue.class);
      validColumnNamesDataTypeInDb.put(OutputBaseColumns.ANSWER, StringValue.class);
    }
  }

}
