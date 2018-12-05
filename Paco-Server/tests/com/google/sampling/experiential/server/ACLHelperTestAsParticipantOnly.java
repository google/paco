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

public class ACLHelperTestAsParticipantOnly extends TestCase {
  String userInHttpRequest = null;
  String userWhoIsOnlyParticipant = null;
  ArrayList<Long> expListinDBForAdmin = new ArrayList<Long>();
  ArrayList<Long> expListinDBForParticipant = new ArrayList<Long>();
  private static Map<String, Class> validColumnNamesDataTypeInDb = Maps.newHashMap();
  private static final String ID = "_id";
  private DateTimeZone dtz = DateTimeZone.forID("America/Los_Angeles");

  // User 'pariticipant1' is a participant of expts expt 1,2,3
  // S1 : no exp id clause, no who clause
  // if participant, no expt id, so no processing
  String actualS1Qry = "select * from events ";

  // S2 : exp id clause includes all in db where user is participant, no who clause
  // if participant, since no who clause, append who clause
  String actualS2Qry = "select * from events where experiment_id in(1, 2, 3)";
  String expectedS2Qry = "select * from events where experiment_id in (1, 2, 3) and who = 'participant1'";

  // S3: exp id clause includes some in db as participant, some not in db as participant,
  // who list contains more than 1 user
  // if participant, asking more than his/her data, so no processing
  String actualS3Qry = "select * from events where experiment_id in(1, 2, 3, 4) and who in('admin1','admin2','participant1')";

  @Test
  public void testS1() {
    try {
      Select selStmt = SearchUtil.getJsqlSelectStatement(actualS1Qry);
      QueryPreprocessor qp = new QueryPreprocessor(selStmt, validColumnNamesDataTypeInDb, false, null);
      ACLHelper.getModifiedQueryBasedOnACL(actualS1Qry, userWhoIsOnlyParticipant, expListinDBForAdmin, qp);
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
      actualQuery = ACLHelper.getModifiedQueryBasedOnACL(actualS2Qry, userWhoIsOnlyParticipant, expListinDBForAdmin, qp);
      assertTrue(expectedS2Qry.equalsIgnoreCase(actualQuery));
    } catch (Exception e) {
      assertFalse(e != null);
    }
  }

  @Test
  public void testS3() {
    try {
      Select selStmt = SearchUtil.getJsqlSelectStatement(actualS3Qry);
      QueryPreprocessor qp = new QueryPreprocessor(selStmt, validColumnNamesDataTypeInDb, false, null);
      ACLHelper.getModifiedQueryBasedOnACL(actualS3Qry, userWhoIsOnlyParticipant, expListinDBForAdmin, qp);
    } catch (Exception e) {
      assertTrue(e.getMessage().startsWith("Unauthorized access"));
    }
  }

  @Before
  public void setUp() {

    userInHttpRequest = "admin1";
    userWhoIsOnlyParticipant = "participant1";
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
