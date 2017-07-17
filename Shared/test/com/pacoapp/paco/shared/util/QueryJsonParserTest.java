package com.pacoapp.paco.shared.util;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.pacoapp.paco.shared.model2.SQLQuery;
import com.pacoapp.paco.shared.util.QueryJsonParser;

public class QueryJsonParserTest {

  @Before
  public void before() {

  }

  @Test
  public void testConvertJSONToPOJO_null() throws JSONException {
    SQLQuery q = QueryJsonParser.parseSqlQueryFromJson(null, false);
    Assert.assertEquals(null, q);
  }

  @Test
  public void testConvertJSONToPOJO_partialValues_NoGroupByButHaving() {
    String inputString = "{query: {criteria: '(group_name =? and answer=?)',values:['New Group','bombay']},limit: 100, order: 'response_time' ,select: ['group_name','response_time', 'experiment_name','answer'], having: 'response_time>10'}";
//    String inputString = "{\"query\": {criteria: \"(group_name =? and answer=?)\",values:[\"New Group\",\"bombay\"]},limit: 100, order: \"response_time\" ,select: [\"group_name\",\"response_time\", \"experiment_name\",\"answer\"], having: \"response_time>10\"}";
    
    SQLQuery.Builder expectedValueBldr = new SQLQuery.Builder();
    expectedValueBldr.projection(new String[] { "group_name", "response_time", "experiment_name", "answer" });
    expectedValueBldr.criteriaQuery("(group_name =? and answer=?)");
    expectedValueBldr.criteriaValues(new String[] { "'New Group'", "'bombay'" });
    expectedValueBldr.limit("100");
    expectedValueBldr.sortBy("response_time");
    expectedValueBldr.groupBy(null);
    expectedValueBldr.having(null);
    SQLQuery expectedValue = expectedValueBldr.buildWithDefaultValues();
    
    SQLQuery actualValue = null;
    try {
      actualValue = QueryJsonParser.parseSqlQueryFromJson(inputString, true);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
   
    Assert.assertNotNull(actualValue);
    Assert.assertEquals(expectedValue.getProjection(), actualValue.getProjection());
    Assert.assertEquals(expectedValue.getCriteriaQuery().trim(), actualValue.getCriteriaQuery());
    Assert.assertEquals(expectedValue.getCriteriaValue(), actualValue.getCriteriaValue());
    Assert.assertEquals(expectedValue.getSortOrder().trim(), actualValue.getSortOrder());
    Assert.assertEquals(expectedValue.getLimit().trim(), actualValue.getLimit());
    Assert.assertEquals(expectedValue.getCriteriaQuery().trim(), actualValue.getCriteriaQuery());
    Assert.assertNull(actualValue.getGroupBy());
    Assert.assertNull(actualValue.getHaving());
  }
  @Test
   public void testConvertJSONToPOJO_allValues(){
     String inputString = "{query: {criteria: '(group_name =? and answer=?)',values:['New Group','bombay']},limit: 100, order: 'response_time' ,select: ['group_name','response_time', 'experiment_name','answer'], group: 'response_time', having: 'response_time>5'}";
     SQLQuery.Builder expectedValueBldr = new SQLQuery.Builder();
     expectedValueBldr.projection(new String[] { "group_name", "response_time", "experiment_name", "answer" });
     
     expectedValueBldr.criteriaQuery("(group_name =? and answer=?)");
     expectedValueBldr.criteriaValues(new String[] { "'New Group'", "'bombay'" });
     expectedValueBldr.limit("100");
     expectedValueBldr.sortBy("response_time");
     expectedValueBldr.groupBy("response_time");
     expectedValueBldr.having("response_time>5");

     SQLQuery expectedValue = expectedValueBldr.buildWithDefaultValues();
     SQLQuery actualValue = null;
     try {
      actualValue = QueryJsonParser.parseSqlQueryFromJson(inputString, true);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
     
     Assert.assertNotNull(actualValue);
     Assert.assertEquals(expectedValue.getProjection(), actualValue.getProjection());
     Assert.assertEquals(expectedValue.getCriteriaQuery().trim(), actualValue.getCriteriaQuery());
     Assert.assertEquals(expectedValue.getCriteriaValue(), actualValue.getCriteriaValue());
     Assert.assertEquals(expectedValue.getSortOrder().trim(), actualValue.getSortOrder());
     Assert.assertEquals(expectedValue.getLimit().trim(), actualValue.getLimit());
     Assert.assertEquals(expectedValue.getCriteriaQuery().trim(), actualValue.getCriteriaQuery());
     Assert.assertEquals(expectedValue.getGroupBy(),actualValue.getGroupBy());
     Assert.assertEquals(expectedValue.getHaving(),actualValue.getHaving());
   }
  
   @Test
   public void testConvertJSONToPOJO_partialValues_NoSortOrderButLimit(){
     String inputString = "{query: {criteria: '(group_name =? and answer=?)',values:['New Group','bombay']},limit: 100, select: ['group_name','response_time', 'experiment_name','answer'], limit: '10'}";
     SQLQuery.Builder expectedValueBldr = new SQLQuery.Builder();
     expectedValueBldr.projection(new String[] { "group_name", "response_time", "experiment_name", "answer" });
     
     expectedValueBldr.criteriaQuery("(group_name =? and answer=?)");
     expectedValueBldr.criteriaValues(new String[] { "'New Group'", "'bombay'" });
     expectedValueBldr.groupBy(null);
     expectedValueBldr.having(null);
     expectedValueBldr.limit("10");
     SQLQuery expectedValue = expectedValueBldr.buildWithDefaultValues();
     
     SQLQuery actualValue = null;

     try {
      actualValue = QueryJsonParser.parseSqlQueryFromJson(inputString, true);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
     
     
     Assert.assertNotNull(actualValue);
     Assert.assertEquals(expectedValue.getProjection(), actualValue.getProjection());
     Assert.assertEquals(expectedValue.getCriteriaQuery().trim(), actualValue.getCriteriaQuery());
     Assert.assertEquals(expectedValue.getCriteriaValue(), actualValue.getCriteriaValue());
     Assert.assertEquals(expectedValue.getSortOrder(), actualValue.getSortOrder());
     Assert.assertEquals(expectedValue.getLimit(),actualValue.getLimit());
     Assert.assertEquals(expectedValue.getCriteriaQuery().trim(), actualValue.getCriteriaQuery());
     Assert.assertNull(actualValue.getGroupBy());
     Assert.assertNull(actualValue.getHaving());

   }
}
