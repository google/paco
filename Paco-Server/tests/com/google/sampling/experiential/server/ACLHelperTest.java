package com.google.sampling.experiential.server;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

public class ACLHelperTest extends TestCase {
  String userInHttpRequest = null;
  String userWhoIsAdmin = null;
  String userWhoIsOnlyParticipant = null;
  ArrayList<Long> expListinDBForAdmin = new ArrayList<Long>();
  ArrayList<Long> expListinDBForParticipant = new ArrayList<Long>();
  ArrayList<Long> expIdInQryFilterZero = new ArrayList<Long>();
  ArrayList<Long> expIdInQryFilterAllInDB = new ArrayList<Long>();
  ArrayList<Long> expIdInQryFilterPartialInDB = new ArrayList<Long>();
  ArrayList<String> whoInQryFilterIsAnAdmin = new ArrayList<String>();
  ArrayList<String> whoInQryFilterIsOnlyParticiapnt = new ArrayList<String>();
  ArrayList<String> whoInQryFilterZero = new ArrayList<String>(); 
  ArrayList<String> whoInQryFilterMixed = new ArrayList<String>();
  ArrayList<String> whoInQryFilterSame = new ArrayList<String>();
 
  // User 'admin1' is an admin of expts expt 1,2,3
  // S1 : no exp id clause, no who clause
  //      if admin, no expt id, so no processing
  //      if participant no expt id, so no processing
  String actualS1Qry = "select * from events ";
  
  // S2 : exp id clause includes all in db, no who clause
  //      if admin, no change
  //      if participant, NA
  String actualS2Qry = "select * from events where experiment_id in(1, 2, 3)";
  String expectedS2Qry = "select * from events where experiment_id in(1, 2, 3)";
  
  // S3 : exp id clause includes some in db as admin, some not in db as admin no who clause
  //      if admin, mixed acl without who, so no processing
  //      if participant, NA
  String actualS3Qry = "select * from events where experiment_id in(1, 2, 3, 4)"; 
  
  // S4 : who is loggedin user, no expt id clause
  //      if admin, no expt id, so no processing
  //      if participant, no expt id, so no processing
  String actualS4Qry = "select * from events where who = 'admin1'";
  
  // S5a : who list contains loggedin user and not loggedin admin, no expt id clause
  //      if admin, no expt id, so no processing
  //      if participant, no expt id, so no processing
  String actualS5aQry = "select * from events where who = 'admin1' or who ='admin2'";

  //S5b : who list contains loggedin user and not participant, no expt id clause
   //      if admin, no expt id, so no processing
   //      if participant, no expt id, so no processing
  String actualS5bQry = "select * from events where who = 'admin1' or who ='participant1'";
  
  // S6 : who list contains same loggedin user multiple times, no expt id clause
  //      if admin, no expt id, so no processing
  //      if participant, no expt id, so no processing
  String actualS6Qry = "select * from events where (who = 'admin1' and experiment_version=40) or (who='admin1' and experiment_version=42)";
  
   // S7 :  expt id list as in db admin list, who list is same as loggedin user
  //      if admin, no change
  //      if participant, NA
  String actualS7Qry = "select * from events where experiment_id in(1, 2, 3) and who='admin1'";
  String expectedS7Qry = "select * from events where experiment_id in(1, 2, 3) and who='admin1'";

  // S8a :  expt id list as in db admin list, who list contains loggedin admin user and not loggedin admin
  //      if admin, throw exception
  //      if participant, NA
  String actualS8aQry = "select * from events where experiment_id in(1, 2, 3) and (who='admin1' or who='admin2')";
  String expectedS8aQry = "select * from events where experiment_id in(1, 2, 3) and (who='admin1' or who='admin2')";

  // S8b :  expt id list as in db admin list, who list contains loggedin admin user and not loggedin participant
  //      if admin, no change
  //      if participant, NA
  String actualS8bQry = "select * from events where experiment_id in(1, 2, 3) and (who='admin1' or who='participant1')";
  String expectedS8bQry = "select * from events where experiment_id in(1, 2, 3) and (who='admin1' or who='participant1')";
   
  // S9 :  expt id list as in db admin list, who list contains same loggedin user multiple times  
  //      if admin, no change
  //      if participant, NA
  String actualS9Qry = "select * from events where experiment_id in(1, 2, 3) and who='admin1' or (who='admin1' and experiment_version=40)";
  String expectedS9Qry = "select * from events where experiment_id in(1, 2, 3) and who='admin1' or (who='admin1' and experiment_version=40)";

  // S10:  exp id clause includes some in db as admin, some not in db as admin, who list contains same loggedin user  
  //      if admin, no change
  //      if participant, NA
  String actualS10Qry = "select * from events where experiment_id in(1, 2, 3, 4) and who='admin1'";
  String expectedS10Qry = "select * from events where experiment_id in(1, 2, 3, 4) and who='admin1'";   
   
  // S11:  exp id clause includes some in db as admin, some not in db as admin, who list contains loggedin user and not loggedin user  
  //      if admin, mixed acl with 'who' who is not the loggedin user
  //      if participant, NA
  String actualS11Qry = "select * from events where experiment_id in(1, 2, 3, 4) and who in('admin1','admin2','participant1')";
 
  // S12:  exp id clause includes some in db as admin, some not in db as admin, who list contains same loggedin user multiple times  
  //      if admin, no change
  //      if participant, NA
  String actualS12Qry = "select * from events where experiment_id in(1, 2, 3, 4) and (who = 'admin1' and experiment_version=40) or (who='admin1' and experiment_version=42)";
  String expectedS12Qry = "select * from events where experiment_id in(1, 2, 3, 4) and (who = 'admin1' and experiment_version=40) or (who='admin1' and experiment_version=42)";

  @Test
  public void testS1(){
    try {
      expIdInQryFilterAllInDB.clear();
      whoInQryFilterIsAnAdmin.clear();
      ACLHelper.getModifiedQueryBasedOnACL(actualS1Qry, userWhoIsAdmin, expListinDBForAdmin);
    } catch (Exception e) {
      assertTrue(e.getMessage().startsWith("Unauthorized access"));
    }
  }

  @Test
  public void testS2(){
    String actualQuery;
    try {
      whoInQryFilterIsAnAdmin.clear();
      actualQuery = ACLHelper.getModifiedQueryBasedOnACL(actualS2Qry, userWhoIsAdmin, expListinDBForAdmin);
      assertEquals(actualQuery, expectedS2Qry);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  @Test
  public void testS3(){
    try {
      whoInQryFilterIsAnAdmin.clear();
      ACLHelper.getModifiedQueryBasedOnACL(actualS3Qry, userWhoIsAdmin, expListinDBForAdmin);
    } catch (Exception e) {
      assertTrue(e.getMessage().startsWith("Unauthorized access"));
    }
  }
  
  @Test
  public void testS4(){
    try {
      expIdInQryFilterAllInDB.clear();
      ACLHelper.getModifiedQueryBasedOnACL(actualS4Qry, userWhoIsAdmin, expListinDBForAdmin);
    } catch (Exception e) {
      assertTrue(e.getMessage().startsWith("Unauthorized access"));
    }
  }
  
  @Test
  public void testS5a(){
    try {
      expIdInQryFilterAllInDB.clear();
      ACLHelper.getModifiedQueryBasedOnACL(actualS5aQry, userWhoIsAdmin, expListinDBForAdmin);
    } catch (Exception e) {
      assertTrue(e.getMessage().startsWith("Unauthorized access"));
    }
  }
  
  @Test
  public void testS5b(){
    try {
      expIdInQryFilterAllInDB.clear();
      ACLHelper.getModifiedQueryBasedOnACL(actualS5bQry, userWhoIsAdmin, expListinDBForAdmin);
    } catch (Exception e) {
      assertTrue(e.getMessage().startsWith("Unauthorized access"));
    }
  }

  @Test
  public void testS6(){
    try {
      expIdInQryFilterAllInDB.clear();
      ACLHelper.getModifiedQueryBasedOnACL(actualS6Qry, userWhoIsAdmin, expListinDBForAdmin);
    } catch (Exception e) {
      assertTrue(e.getMessage().startsWith("Unauthorized access"));
    }
  }
  
  @Test
  public void testS7(){
    String actualQuery;
    try {
      actualQuery = ACLHelper.getModifiedQueryBasedOnACL(actualS7Qry, userWhoIsAdmin, expListinDBForAdmin);
      assertEquals(actualQuery, expectedS7Qry);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  @Test
  public void testS8a(){
    String actualQuery;
    try {
      actualQuery = ACLHelper.getModifiedQueryBasedOnACL(actualS8aQry, userWhoIsAdmin, expListinDBForAdmin);
      assertEquals(actualQuery, expectedS8aQry);
    } catch (Exception e) {
      
    }
  }
  
  @Test
  public void testS8b(){
    String actualQuery;
    try {
      actualQuery = ACLHelper.getModifiedQueryBasedOnACL(actualS8bQry, userWhoIsAdmin, expListinDBForAdmin);
      assertEquals(actualQuery, expectedS8bQry);
    } catch (Exception e) {
      
    }
  }
  
  @Test
  public void testS9(){
    String actualQuery;
    try {
      actualQuery = ACLHelper.getModifiedQueryBasedOnACL(actualS9Qry, userWhoIsAdmin, expListinDBForAdmin);
      assertEquals(actualQuery, expectedS9Qry);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Test
  public void testS10(){
    String actualQuery;
    try {
      actualQuery = ACLHelper.getModifiedQueryBasedOnACL(actualS10Qry, userWhoIsAdmin, expListinDBForAdmin);
      assertEquals(actualQuery, expectedS10Qry);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  @Test
  public void testS11(){
    try {
      ACLHelper.getModifiedQueryBasedOnACL(actualS11Qry, userWhoIsAdmin, expListinDBForAdmin);
    } catch (Exception e) {
      assertTrue(e.getMessage().startsWith("Unauthorized access"));
    }
  }
  
  @Test
  public void testS12(){
    String actualQuery;
    try {
      actualQuery = ACLHelper.getModifiedQueryBasedOnACL(actualS12Qry, userWhoIsAdmin, expListinDBForAdmin);
      assertEquals(actualQuery, expectedS12Qry);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }


  @Before
  public void setUp(){
    //Initial set up 
    // Expt 1,2,3,4
    // Admin is admin1 
    // Participant is participant1, participant2, participant3 for all expt 1 2 3 4
    // Participant p4 
    // Admin admin1 is admin and participant of Expt 1, 2, 3
    
    userInHttpRequest = "admin1";
    userWhoIsAdmin = "admin1";
    userWhoIsOnlyParticipant ="participant1";
   
    expListinDBForAdmin.add(1L);
    expListinDBForAdmin.add(2L);
    expListinDBForAdmin.add(3L);
    
    expIdInQryFilterAllInDB.add(1L);
    expIdInQryFilterAllInDB.add(2L);
    expIdInQryFilterAllInDB.add(3L);
    
    expIdInQryFilterPartialInDB.add(1L);
    expIdInQryFilterPartialInDB.add(2L);
    expIdInQryFilterPartialInDB.add(3L);
    expIdInQryFilterPartialInDB.add(4L);
    
    whoInQryFilterIsAnAdmin.add("admin1");
    
    whoInQryFilterIsOnlyParticiapnt.add("participant1");
    
    whoInQryFilterMixed.add("admin1");
    whoInQryFilterMixed.add("admin2");
    whoInQryFilterMixed.add("participant1");
    whoInQryFilterMixed.add("participant2");
    whoInQryFilterMixed.add("participant3");
    
    whoInQryFilterSame.add("admin1");
    whoInQryFilterSame.add("admin1");
  }

}
