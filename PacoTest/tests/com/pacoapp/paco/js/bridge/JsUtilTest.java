package com.pacoapp.paco.js.bridge;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.pacoapp.paco.shared.model2.SQLQuery;
import com.pacoapp.paco.shared.util.JsUtil;

public class JsUtilTest {

  @Before
  public void before() {

  }

  @Test
  public void testConvertJSONToPOJO_null() throws JSONException {
    SQLQuery q = JsUtil.convertJSONToPOJO(null);
    Assert.assertEquals(q, null);
  }

  @Test
  public void testConvertJSONToPOJO_partialValues_NoGroupByButHaving() {
    String inputString = "{query: {criteria: '(group_name =? and answer=?)',values:['New Group','bombay']},limit: 100, order: 'response_time' ,select: ['group_name','response_time', 'experiment_name','answer'], having: 'response_time>10'}";
    SQLQuery.Builder expectedValueBldr = new SQLQuery.Builder(new String[] { "group_name", "response_time", "experiment_name", "answer" });
    expectedValueBldr.criteriaQuery("(group_name =? and answer=?)");
    expectedValueBldr.criteriaValues(new String[] { "New Group", "bombay" });
    expectedValueBldr.limit("100");
    expectedValueBldr.sortBy("response_time");
    expectedValueBldr.groupBy(null);
    expectedValueBldr.having(null);
    SQLQuery expectedValue = expectedValueBldr.buildWithDefaultValues();
    
    SQLQuery actualValue = null;
    actualValue = JsUtil.convertJSONToPOJO(inputString);
   
    
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
     SQLQuery.Builder expectedValueBldr = new SQLQuery.Builder(new String[] { "group_name", "response_time", "experiment_name", "answer" });
     
     expectedValueBldr.criteriaQuery("(group_name =? and answer=?)");
     expectedValueBldr.criteriaValues(new String[] { "New Group", "bombay" });
     expectedValueBldr.limit("100");
     expectedValueBldr.sortBy("response_time");
     expectedValueBldr.groupBy("response_time");
     expectedValueBldr.having("response_time>5");

     SQLQuery expectedValue = expectedValueBldr.buildWithDefaultValues();
     SQLQuery actualValue = null;
     actualValue = JsUtil.convertJSONToPOJO(inputString);
     
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
     SQLQuery.Builder expectedValueBldr = new SQLQuery.Builder(new String[] { "group_name", "response_time", "experiment_name", "answer" });
//     expectedValue.setProjection(new String[] { "group_name", "response_time", "experiment_name", "answer" });
     expectedValueBldr.criteriaQuery("(group_name =? and answer=?)");
     expectedValueBldr.criteriaValues(new String[] { "New Group", "bombay" });
     expectedValueBldr.limit(null);
     expectedValueBldr.sortBy(null);
     expectedValueBldr.groupBy(null);
     expectedValueBldr.having(null);
     SQLQuery expectedValue = expectedValueBldr.buildWithDefaultValues();
     

     SQLQuery actualValue = null;

     actualValue = JsUtil.convertJSONToPOJO(inputString);
     
     
     Assert.assertNotNull(actualValue);
     Assert.assertEquals(expectedValue.getProjection(), actualValue.getProjection());
     Assert.assertEquals(expectedValue.getCriteriaQuery().trim(), actualValue.getCriteriaQuery());
     Assert.assertEquals(expectedValue.getCriteriaValue(), actualValue.getCriteriaValue());
     Assert.assertNull(actualValue.getSortOrder());
     Assert.assertNull(actualValue.getLimit());
     Assert.assertEquals(expectedValue.getCriteriaQuery().trim(), actualValue.getCriteriaQuery());
     Assert.assertNull(actualValue.getGroupBy());
     Assert.assertNull(actualValue.getHaving());

   }
}
