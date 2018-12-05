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

public class ACLHelperTestAsAdminAndParticipant extends TestCase {
  String userInHttpRequest = null;
  String userWhoIsAdminAndParticipant = null;
  ArrayList<Long> expListinDBForAdmin = new ArrayList<Long>();
  private static Map<String, Class> validColumnNamesDataTypeInDb = Maps.newHashMap();
  private static final String ID = "_id";

  // User 'participant1' is a participant of Expt 4, and admin of Experiments
  // 1,2,3
  // S1 : no exp id clause, no who clause
  // if participant and admin no expt id, so no processing
  String actualS1Qry = "select * from events ";

  // S2 : exp id clause includes all in db as admin, no who clause
  // if participant and admin, no change
  String actualS2Qry = "select * from events where experiment_id in(1, 2, 3)";
  String expectedS2Qry = "select * from events where experiment_id in (1, 2, 3)";

  // S3a : exp id clause includes some in db as admin, some in db as participant
  // no who clause
  // if participant and admin, throw exception
  String actualS3aQry = "select * from events where experiment_id in(1, 2, 3, 4)";

  // S3b : exp id clause includes expt in db as participant no who clause
  // if participant and admin, kind of mixed acl, with no who clause
  String actualS3bQry = "select * from events where experiment_id in(4)";
  // String expectedS3bQry = "select * from events where experiment_id in (4)
  // and who = 'participant1'";

  // S4 : who is loggedin user, no expt id clause
  // if participant and admin, no expt id, so no processing
  String actualS4Qry = "select * from events where who = 'participant1'";

  // S5a : who list contains loggedin user and not loggedin user, no expt id
  // clause
  // if participant and admin, no expt id, so no processing
  String actualS5aQry = "select * from events where who = 'participant1' or who ='adminorparticipant'";

  // S6 : who list contains same loggedin user multiple times, no expt id clause
  // if participant and admin, no expt id, so no processing
  String actualS6Qry = "select * from events where (who = 'participant1' and experiment_version=40) or (who='participant1' and experiment_version=42)";

  // S7 : expt id list contains expt as admin list, who list is participant1
  // if participant and admin, no change in query
  String actualS7Qry = "select * from events where experiment_id in(1, 2, 3) and who='participant1'";
  String expectedS7Qry = "select * from events where experiment_id in (1, 2, 3) and who = 'participant1'";

  // S8a : expt id list contains expt as admin list, who list contains
  // participant and some other user
  // if participant and admin, no change (since he is also admin of these expts)
  String actualS8Qry = "select * from events where experiment_id in(1, 2, 3) and (who='participant1' or who='admin2')";
  String expectedS8Qry = "select * from events where experiment_id in (1, 2, 3) and (who = 'participant1' or who = 'admin2')";

  // S9 : expt id list contains expt as admin list, who list contains same
  // participant multiple times
  // if participant and admin, no change
  String actualS9Qry = "select * from events where experiment_id in(1, 2, 3) and who='participant1' or (who='participant1' and experiment_version=40)";
  String expectedS9Qry = "select * from events where experiment_id in (1, 2, 3) and who = 'participant1' or (who = 'participant1' and experiment_version = 40)";

  // S10: expt id list contains expt as admin list, some in db as participant,
  // who list contains only participant
  // if participant and admin, no change
  String actualS10Qry = "select * from events where experiment_id in(1, 2, 3, 4) and who='participant1'";
  String expectedS10Qry = "select * from events where experiment_id in (1, 2, 3, 4) and who = 'participant1'";

  // S11: expt id list contains expt as admin list, some in db as participant,
  // who list contains particpant1
  // if participant and admin,
  String actualS11Qry = "select * from events where experiment_id in(1, 2, 3, 4) and who in('admin1','admin2','participant1')";

  // S12: expt id list contains expt as admin list, some in db as participant,
  // who list contains participant1 multiple times
  // if participant and admin, NA
  String actualS12Qry = "select * from events where experiment_id in(1, 2, 3, 4) and (who = 'participant1' and experiment_version=40) or (who='participant1' and experiment_version=42)";
  String expectedS12Qry = "select * from events where experiment_id in (1, 2, 3, 4) and (who = 'participant1' and experiment_version = 40) or (who = 'participant1' and experiment_version = 42)";
  
  DateTimeZone dtz = DateTimeZone.forID("America/Los_Angeles");

  @Test
  public void testS1() {
    try {
      Select selStmt = SearchUtil.getJsqlSelectStatement(actualS1Qry);
      QueryPreprocessor qp = new QueryPreprocessor(selStmt, validColumnNamesDataTypeInDb, false, null);
      ACLHelper.getModifiedQueryBasedOnACL(actualS1Qry, userWhoIsAdminAndParticipant, expListinDBForAdmin, qp);
    } catch (Exception e) {
      assertTrue(e.getMessage().startsWith("Unauthorized access"));
    }
  }

  @Test
  public void testS2() {
    String actualQuery;
    try {
      Select selStmt = SearchUtil.getJsqlSelectStatement(actualS2Qry);
      QueryPreprocessor qp = new QueryPreprocessor(selStmt, validColumnNamesDataTypeInDb, false, null);
      actualQuery = ACLHelper.getModifiedQueryBasedOnACL(actualS2Qry, userWhoIsAdminAndParticipant, expListinDBForAdmin,
                                                         qp);
      assertTrue(expectedS2Qry.equalsIgnoreCase(actualQuery));
    } catch (Exception e) {
      assertFalse(e != null);
    }
  }

  @Test
  public void testS3a() {
    try {
      Select selStmt = SearchUtil.getJsqlSelectStatement(actualS3aQry);
      QueryPreprocessor qp = new QueryPreprocessor(selStmt, validColumnNamesDataTypeInDb, false, null);
      ACLHelper.getModifiedQueryBasedOnACL(actualS3aQry, userWhoIsAdminAndParticipant, expListinDBForAdmin, qp);
    } catch (Exception e) {
      assertTrue(e.getMessage().startsWith("Unauthorized access"));
    }
  }

  @Test
  public void testS3b() {
    try {
      Select selStmt = SearchUtil.getJsqlSelectStatement(actualS3bQry);
      QueryPreprocessor qp = new QueryPreprocessor(selStmt, validColumnNamesDataTypeInDb, false, null);
      String actualQuery = ACLHelper.getModifiedQueryBasedOnACL(actualS3bQry, userWhoIsAdminAndParticipant,
                                                                expListinDBForAdmin, qp);

    } catch (Exception e) {
      assertTrue(e.getMessage().startsWith("Unauthorized access"));
    }
  }

  @Test
  public void testS4() {
    try {
      Select selStmt = SearchUtil.getJsqlSelectStatement(actualS4Qry);
      QueryPreprocessor qp = new QueryPreprocessor(selStmt, validColumnNamesDataTypeInDb, false, null);
      ACLHelper.getModifiedQueryBasedOnACL(actualS4Qry, userWhoIsAdminAndParticipant, expListinDBForAdmin, qp);
    } catch (Exception e) {
      assertTrue(e.getMessage().startsWith("Unauthorized access"));
    }
  }

  @Test
  public void testS5a() {
    try {
      Select selStmt = SearchUtil.getJsqlSelectStatement(actualS5aQry);
      QueryPreprocessor qp = new QueryPreprocessor(selStmt, validColumnNamesDataTypeInDb, false, null);
      ACLHelper.getModifiedQueryBasedOnACL(actualS5aQry, userWhoIsAdminAndParticipant, expListinDBForAdmin, qp);
    } catch (Exception e) {
      assertTrue(e.getMessage().startsWith("Unauthorized access"));
    }
  }

  @Test
  public void testS6() {
    try {
      Select selStmt = SearchUtil.getJsqlSelectStatement(actualS6Qry);
      QueryPreprocessor qp = new QueryPreprocessor(selStmt, validColumnNamesDataTypeInDb, false, null);
      ACLHelper.getModifiedQueryBasedOnACL(actualS6Qry, userWhoIsAdminAndParticipant, expListinDBForAdmin, qp);
    } catch (Exception e) {
      assertTrue(e.getMessage().startsWith("Unauthorized access"));
    }
  }

  @Test
  public void testS7() {
    String actualQuery;
    try {
      Select selStmt = SearchUtil.getJsqlSelectStatement(actualS7Qry);
      QueryPreprocessor qp = new QueryPreprocessor(selStmt, validColumnNamesDataTypeInDb, false, null);
      actualQuery = ACLHelper.getModifiedQueryBasedOnACL(actualS7Qry, userWhoIsAdminAndParticipant, expListinDBForAdmin,
                                                         qp);
      assertTrue(actualQuery.equalsIgnoreCase(expectedS7Qry));
    } catch (Exception e) {
      assertFalse(e != null);
    }
  }

  @Test
  public void testS8() {
    String actualQuery;
    try {
      Select selStmt = SearchUtil.getJsqlSelectStatement(actualS8Qry);
      QueryPreprocessor qp = new QueryPreprocessor(selStmt, validColumnNamesDataTypeInDb, false, null);
      actualQuery = ACLHelper.getModifiedQueryBasedOnACL(actualS8Qry, userWhoIsAdminAndParticipant, expListinDBForAdmin,
                                                         qp);
      assertTrue(actualQuery.equalsIgnoreCase(expectedS8Qry));
    } catch (Exception e) {
      assertFalse(e != null);
    }
  }

  @Test
  public void testS9() {
    String actualQuery;
    try {
      Select selStmt = SearchUtil.getJsqlSelectStatement(actualS9Qry);
      QueryPreprocessor qp = new QueryPreprocessor(selStmt, validColumnNamesDataTypeInDb, false, null);
      actualQuery = ACLHelper.getModifiedQueryBasedOnACL(actualS9Qry, userWhoIsAdminAndParticipant, expListinDBForAdmin,
                                                         qp);
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
      QueryPreprocessor qp = new QueryPreprocessor(selStmt, validColumnNamesDataTypeInDb, false, null);
      actualQuery = ACLHelper.getModifiedQueryBasedOnACL(actualS10Qry, userWhoIsAdminAndParticipant,
                                                         expListinDBForAdmin, qp);
      assertTrue(actualQuery.equalsIgnoreCase(expectedS10Qry));
    } catch (Exception e) {
      assertFalse(e != null);
    }
  }

  @Test
  public void testS11() {
    try {
      Select selStmt = SearchUtil.getJsqlSelectStatement(actualS11Qry);
      QueryPreprocessor qp = new QueryPreprocessor(selStmt, validColumnNamesDataTypeInDb, false, null);
      ACLHelper.getModifiedQueryBasedOnACL(actualS11Qry, userWhoIsAdminAndParticipant, expListinDBForAdmin, qp);
    } catch (Exception e) {
      assertTrue(e.getMessage().startsWith("Unauthorized access"));
    }
  }

  @Test
  public void testS12() {
    String actualQuery;
    try {
      Select selStmt = SearchUtil.getJsqlSelectStatement(actualS12Qry);
      QueryPreprocessor qp = new QueryPreprocessor(selStmt, validColumnNamesDataTypeInDb, false, null);
      actualQuery = ACLHelper.getModifiedQueryBasedOnACL(actualS12Qry, userWhoIsAdminAndParticipant,
                                                         expListinDBForAdmin, qp);
      assertTrue(actualQuery.equalsIgnoreCase(expectedS12Qry));
    } catch (Exception e) {
      assertFalse(e != null);
    }
  }

  @Before
  public void setUp() {
    // Initial set up
    // Expt 1,2,3,4
    // Admin is admin1
    // Participant participant1 is admin for all expt 1 2 3 and participant for
    // expt 4
    // Admin admin1 is admin and participant of Expt 1, 2, 3

    userWhoIsAdminAndParticipant = "participant1";

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
