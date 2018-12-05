package com.google.sampling.experiential.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.sampling.experiential.cloudsql.columns.EventServerColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentUserColumns;
import com.google.sampling.experiential.cloudsql.columns.UserColumns;
import com.google.sampling.experiential.dao.CSExperimentUserDao;
import com.google.sampling.experiential.dao.CSFailedEventDao;
import com.google.sampling.experiential.dao.CSUserDao;
import com.google.sampling.experiential.dao.dataaccess.User;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.ExceptionUtil;
import com.google.sampling.experiential.server.ExperimentAccessManager;
import com.google.sampling.experiential.server.PacoId;
import com.google.sampling.experiential.server.PacoUser;
import com.google.sampling.experiential.server.QueryConstants;
import com.pacoapp.paco.shared.util.ErrorMessages;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;

public class CSExperimentUserDaoImpl implements CSExperimentUserDao {
  public static final Logger log = Logger.getLogger(CSExperimentUserDaoImpl.class.getName());
  private static List<Column> experimentUserColList = Lists.newArrayList();
  private CSUserDao userDaoImpl = new CSUserDaoImpl();
  private CSFailedEventDao failedEventDaoImpl = new CSFailedEventDaoImpl();
  
  static {
    experimentUserColList.add(new Column(ExperimentUserColumns.EXPERIMENT_ID));
    experimentUserColList.add(new Column(ExperimentUserColumns.USER_ID));
    experimentUserColList.add(new Column(ExperimentUserColumns.EXP_USER_ANON_ID));
    experimentUserColList.add(new Column(ExperimentUserColumns.USER_TYPE));
  }
  
  @Override
  public PacoId getAnonymousIdAndCreate(Long experimentId, String email, boolean createOption) throws SQLException{
    
    PacoId pacoAnonId = new PacoId();
    Integer anonId = getAnonymousId(experimentId, email);
    if (anonId != null) {
      pacoAnonId.setId(anonId.longValue());
      pacoAnonId.setIsCreatedWithThisCall(false);
    } else if (createOption) {
      insertUserForExperiment(experimentId, email);
      pacoAnonId.setId(getAnonymousId(experimentId, email).longValue());
      pacoAnonId.setIsCreatedWithThisCall(true);
    } else {
      //TODO not sure if this is a good option to set to 0
      pacoAnonId.setId(0L);
      pacoAnonId.setIsCreatedWithThisCall(false);
    }
    return pacoAnonId;
  }
  
 
  
  @Override
  public Integer getMaxAnonId(Long expId) throws SQLException {
    List<PacoUser> userLst = getAllUsersForExperiment(expId);
    return getMaxAnonId(userLst);
  }
  
  @Override
  public List<PacoUser> getAllUsersForExperiment(Long experimentId) throws SQLException {
    List<PacoUser> pacoUsersForExperiment = Lists.newArrayList();
    Connection conn = null;
    PreparedStatement findAllUsersStatement = null;
    PacoUser pUser = null;
    ResultSet rs = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      findAllUsersStatement = conn.prepareStatement(QueryConstants.GET_ALL_USERS_FOR_EXPERIMENT.toString());
      findAllUsersStatement.setLong(1, experimentId);
      rs = findAllUsersStatement.executeQuery();
      while(rs.next()){
        pUser = new PacoUser();
        pUser.setType(rs.getString(ExperimentUserColumns.USER_TYPE).charAt(0));
        pUser.setId(rs.getLong(ExperimentUserColumns.USER_ID));
        pUser.setAnonId(rs.getInt(ExperimentUserColumns.EXP_USER_ANON_ID));
        pUser.setEmail(rs.getString(UserColumns.WHO));
        pacoUsersForExperiment.add(pUser);
      }
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (findAllUsersStatement != null) {
          findAllUsersStatement.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }

    return pacoUsersForExperiment;
  }
  
  @Override
  public List<Integer> getAllAnonIdsForEVGMId(Long evgmId) throws SQLException {
    List<Integer> anonIdsForExperiment = Lists.newArrayList();
    Connection conn = null;
    PreparedStatement findAllUsersStatement = null;
    ResultSet rs = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      findAllUsersStatement = conn.prepareStatement(QueryConstants.GET_ALL_USERS_FOR_EVGM.toString());
      findAllUsersStatement.setLong(1, evgmId);
      rs = findAllUsersStatement.executeQuery();
      while(rs.next()){
        anonIdsForExperiment.add(rs.getInt(EventServerColumns.WHO+"_bk"));
      }
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (findAllUsersStatement != null) {
          findAllUsersStatement.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }

    return anonIdsForExperiment;
  }
  
  @Override
  public void ensureUserId(Long expId, Set<String> adminEmailsInRequest, Set<String> participantEmailsInRequest) {
    if (adminEmailsInRequest == null && participantEmailsInRequest == null) {
      return;
    }
    
    // insert to cloud sql
    List<PacoUser> pacoUsersInDb = null;
    Set<Long> adminIdsInDb = Sets.newHashSet();
    Set<Long> participantIdsInDb = Sets.newHashSet();
    Set<Long> adminIdsInRequest = Sets.newHashSet();
    Set<Long> participantIdsInRequest = Sets.newHashSet();
    List<PacoUser> toBeInsertedIntoExptUserTable = Lists.newArrayList();
    Set<Long> idsToBeUpdatedAsAdmin = Sets.newHashSet();
    Set<Long> idsToBeUpdatedAsParticipant = Sets.newHashSet();
    Set<String> allUsersEmailsInRequest = Sets.newHashSet();
    
    if (adminEmailsInRequest != null) {
      allUsersEmailsInRequest.addAll(adminEmailsInRequest);
    }
    if (participantEmailsInRequest != null) {
      allUsersEmailsInRequest.addAll(participantEmailsInRequest);
    }
    log.info("Persisting users for experiment id:"+ expId + "with adminList size:" + (adminEmailsInRequest != null ? adminEmailsInRequest.size(): "0") + "with participantList size:" + (participantEmailsInRequest != null ? participantEmailsInRequest.size(): "0"));
    
    // if same id is requested as admin and participant, admin takes precedence
    if (participantEmailsInRequest != null) {
      participantEmailsInRequest.removeAll(adminEmailsInRequest);
    }
    
    try {  
      // find all the user ids in user table for all emails (all admin and all participant) in request
      Map<String, Long> requestedEmailIdsInUserTable = userDaoImpl.getUserIdsForEmails(allUsersEmailsInRequest);
      
      // for all emails in request, insert email into user table if not present already and update map with the newly generated id
      for (String email : allUsersEmailsInRequest) {
        if (requestedEmailIdsInUserTable.get(email) == null) {
          Long genId = userDaoImpl.getUserAndCreate(email, true).getUserId().getId();
          requestedEmailIdsInUserTable.put(email, genId);
        }
      }
      
      // get all users associated with an expt, and identify adminIds and participant Ids  that are stored in database
      pacoUsersInDb = getAllUsersForExperiment(expId);
      Iterator<PacoUser> pacoUsrItr = pacoUsersInDb.iterator();
      while (pacoUsrItr.hasNext()) { 
        PacoUser crtUser = pacoUsrItr.next();
        if (crtUser != null && crtUser.getType().equals(ExperimentUserColumns.ADMIN_TYPE.charAt(0))) {
          adminIdsInDb.add(crtUser.getId());
        } else {
          participantIdsInDb.add(crtUser.getId());
        }
      }
      
      // get max of anon id for this list of paco users
      Integer maxAnonId = getMaxAnonId(pacoUsersInDb);
   // TODO Commented code needs to be removed. Checking it in, to review the commonality between the two blocks
      // For admin type
      identifyChangesToExperimentUserMappingForEachUserType(adminEmailsInRequest, requestedEmailIdsInUserTable, adminIdsInRequest, adminIdsInDb, participantIdsInDb, maxAnonId, idsToBeUpdatedAsAdmin, toBeInsertedIntoExptUserTable, ExperimentUserColumns.ADMIN_TYPE); 
      
//      if (adminEmailsInRequest != null) {
//        Iterator<String> adminItr = adminEmailsInRequest.iterator();
//        // for every admin Email in request
//        while (adminItr.hasNext()) {
//          String adminEmailInRequest = adminItr.next();
//          Long adminIdInRequest = requestedEmailIdsInUserTable.get(adminEmailInRequest);
//          adminIdsInRequest.add(adminIdInRequest);
//          // if admin id in request is not present as admin in db
//          if (!adminIdsInDb.contains(adminIdInRequest)) {
//            // if admin id in request is stored in db as participant. This means email is moved from participant status to admin status.
//            if (participantIdsInDb.contains(adminIdInRequest)) {
//              // update email in db as admin 
//              idsToBeUpdatedAsAdmin.add(adminIdInRequest);
//            } else {
//              // insert a new admin user
//              maxAnonId = maxAnonId + 1;
//              pu = new PacoUser(adminIdInRequest, maxAnonId, 'A', adminEmailInRequest);
//              toBeInsertedIntoExptUserTable.add(pu);  
//            }
//          }
//        }
//      }
      if (toBeInsertedIntoExptUserTable != null && toBeInsertedIntoExptUserTable.size() >= 1) {
        maxAnonId = getMaxAnonId(toBeInsertedIntoExptUserTable);
      }
      // For participant type
      identifyChangesToExperimentUserMappingForEachUserType(participantEmailsInRequest, requestedEmailIdsInUserTable, participantIdsInRequest, participantIdsInDb, adminIdsInDb, maxAnonId, idsToBeUpdatedAsParticipant, toBeInsertedIntoExptUserTable, ExperimentUserColumns.PARTICIPANT_TYPE); 
      
//      if (participantEmailsInRequest != null) {
//        Iterator<String> partItr = participantEmailsInRequest.iterator();
//        while (partItr.hasNext()) {
//          String partInRequest = partItr.next();
//          Long participantIdInRequest =  requestedEmailIdsInUserTable.get(partInRequest);
//          participantIdsInRequest.add(participantIdInRequest);
//          if (!participantIdsInDb.contains(participantIdInRequest)) {
//            // email is stored in db as admin, but now coming in as participant
//            if (adminIdsInDb.contains(participantIdInRequest)) {
//              idsToBeUpdatedAsParticipant.add(participantIdInRequest);
//            } else {
//              //plain new participant so, add a new paco user
//              maxAnonId = maxAnonId + 1;
//              pu = new PacoUser(participantIdInRequest, maxAnonId, 'P', partInRequest);
//              toBeInsertedIntoExptUserTable.add(pu);
//            }
//          }
//        }
//      }
      
      // find if admin ids in database has been removed from current request. This means 'not in admin request email list or in participant request email list'
      adminIdsInDb.removeAll(adminIdsInRequest);
      adminIdsInDb.removeAll(participantIdsInRequest);
      Iterator<Long> adminIdsRemovedItr = adminIdsInDb.iterator();
      // when ids have been removed as admin, then they should be treated as participants. Since we cannot lose the data we might have got until now with that id as admin
      while (adminIdsRemovedItr.hasNext()) {
        idsToBeUpdatedAsParticipant.add(adminIdsRemovedItr.next());
      }
      // new records to be inserted into experiment_user table
      insertIntoExperimentUsers(expId, toBeInsertedIntoExptUserTable);
      // old records that need modification in their user type. From admin to participant or vice versa
      updateUserTypesForExperiment(expId, idsToBeUpdatedAsAdmin, idsToBeUpdatedAsParticipant);
    } catch (SQLException sqle) {
      failedEventDaoImpl.insertFailedEvent(expId.toString(), ErrorMessages.SQL_INSERT_EXCEPTION.getDescription() + "Admin/Participant", sqle.getMessage());
      log.warning(ErrorMessages.SQL_INSERT_EXCEPTION.getDescription() + " for  Admin/ Participant request: " + expId + " : " + ExceptionUtil.getStackTraceAsString(sqle));
    } catch (Exception e) {
      failedEventDaoImpl.insertFailedEvent(expId.toString(), ErrorMessages.GENERAL_EXCEPTION.getDescription(), e.getMessage());
      log.warning(ErrorMessages.GENERAL_EXCEPTION.getDescription() + " for  Admin/ Participant request: " + expId + " : " + ExceptionUtil.getStackTraceAsString(e));
    }
  }
  
  private void identifyChangesToExperimentUserMappingForEachUserType(Set<String> type1EmailsInRequest,  Map<String, Long> requestedEmailIdsInUserTable, Set<Long> userIdsOfType1InRequest, Set<Long> type1IdsInDb, Set<Long> otherTypeIdsInDb, Integer maxAnonId, Set<Long> idsToBeUpdatedAsType1, List<PacoUser> toBeInsertedIntoExptUserTable, String statusTypeOfType1 ) {
    PacoUser pu = null;
    if (type1EmailsInRequest != null) {
      Iterator<String> type1EmailsInRequestItr = type1EmailsInRequest.iterator();
      // for every user Email of type1, Eg type 1 is Admin and other type is Participant
      while (type1EmailsInRequestItr.hasNext()) {
        String type1EmailInRequest = type1EmailsInRequestItr.next();
        Long type1IdInRequest = requestedEmailIdsInUserTable.get(type1EmailInRequest);
        userIdsOfType1InRequest.add(type1IdInRequest);
        // if admin id in request is not present as admin in db
        if (!type1IdsInDb.contains(type1IdInRequest)) {
          // if admin id in request is stored in db as participant. This means email is moved from participant status to admin status.
          if (otherTypeIdsInDb.contains(type1IdInRequest)) {
            // update email in db as admin 
            idsToBeUpdatedAsType1.add(type1IdInRequest);
          } else {
            // insert a new admin user
            maxAnonId = maxAnonId + 1;
            pu = new PacoUser(type1IdInRequest, maxAnonId, statusTypeOfType1.charAt(0), type1EmailInRequest);
            toBeInsertedIntoExptUserTable.add(pu);  
          }
        }
      }
    }
  }
  
  private boolean insertIntoExperimentUsers(Long experimentId, List<PacoUser> users) throws SQLException {
    PreparedStatement statementCreateExperimentUsers = null;
    ExpressionList experimentUserExprList = new ExpressionList();
    List<Expression>  out = Lists.newArrayList();
    Insert experimentUserInsert = new Insert();
    Connection conn = null;
    boolean isSuccess = false;
    if (users == null || users.size() == 0) {
      return false;
    }
    
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      conn.setAutoCommit(false);
      experimentUserInsert.setTable(new Table(ExperimentUserColumns.TABLE_NAME));
      experimentUserInsert.setUseValues(true);
      experimentUserExprList.setExpressions(out);
      experimentUserInsert.setItemsList(experimentUserExprList);
      experimentUserInsert.setColumns(experimentUserColList);
      // Adding ? for prepared stmt
      for (Column c : experimentUserColList) {
        ((ExpressionList) experimentUserInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }
      statementCreateExperimentUsers = conn.prepareStatement(experimentUserInsert.toString());
      for (PacoUser eachUser : users) {
        statementCreateExperimentUsers.setLong(1, experimentId);
        statementCreateExperimentUsers.setLong(2, eachUser.getId());
        statementCreateExperimentUsers.setInt(3, eachUser.getAnonId());
        statementCreateExperimentUsers.setString(4, eachUser.getType().toString());
        statementCreateExperimentUsers.addBatch();
      }
      statementCreateExperimentUsers.executeBatch();
      conn.commit();
      isSuccess = true;
    } finally {
      try {
        if (statementCreateExperimentUsers != null) {
          statementCreateExperimentUsers.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
      }
    }
    return isSuccess;
  }
  
  private boolean updateUserTypesForExperiment(Long experimentId, Set<Long> asAdmin, Set<Long> asParticipant) throws SQLException {
    if ((asAdmin == null && asParticipant == null) || (asAdmin.size() == 0 && asParticipant.size() == 0)) {
      return false;
    }
    PreparedStatement statementUpdateAsAdminExperimentUsers = null;
    PreparedStatement statementUpdateAsParticipantExperimentUsers = null;
    Update update = new Update(); 
    List<Expression> adminExpressionList = Lists.newArrayList();
    List<Expression> participantExpressionList = Lists.newArrayList();
    ExpressionList adminIds = new ExpressionList();
    ExpressionList participantIds = new ExpressionList();
    
    Connection conn = null;
    boolean isSuccess = false;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      conn.setAutoCommit(false);
      
      List<Expression> updateUserExprLst = Lists.newArrayList();
      update.setExpressions(updateUserExprLst);
      
      List<Table> updTableLst = Lists.newArrayList();
      updTableLst.add(new Table(ExperimentUserColumns.TABLE_NAME));
      
      List<Column> updateUserTypeColumnList = Lists.newArrayList();
      updateUserTypeColumnList.add(new Column(ExperimentUserColumns.USER_TYPE));
      
      for (Column c : updateUserTypeColumnList) {
        updateUserExprLst.add(new JdbcParameter());
      }
    
      EqualsTo experimentIdEqualsToCondition = new EqualsTo();
      experimentIdEqualsToCondition.setLeftExpression(new Column(ExperimentUserColumns.EXPERIMENT_ID));
      experimentIdEqualsToCondition.setRightExpression(new JdbcParameter());
      
      if (asAdmin != null && asAdmin.size() > 0) {
        Iterator<Long> itr = asAdmin.iterator();
        while (itr.hasNext()) {
          adminExpressionList.add(new LongValue(itr.next()));
        }
        adminIds.setExpressions(adminExpressionList);
      }
      if (asParticipant != null && asParticipant.size() > 0) {
        Iterator<Long> itr = asParticipant.iterator();
        while (itr.hasNext()) {
          participantExpressionList.add(new LongValue(itr.next()));
        }
        participantIds.setExpressions(participantExpressionList);
      }
      
      InExpression userIdInCondition = new InExpression();
      userIdInCondition.setLeftExpression(new Column(ExperimentUserColumns.USER_ID));
      
      AndExpression andExpr = new AndExpression(experimentIdEqualsToCondition, userIdInCondition);
      
      update.setColumns(updateUserTypeColumnList);
      update.setTables(updTableLst);
      update.setWhere(andExpr);

      // for admins
      if (asAdmin != null && asAdmin.size() > 0) {
        userIdInCondition.setRightItemsList(adminIds);
        statementUpdateAsAdminExperimentUsers = conn.prepareStatement(update.toString());
        statementUpdateAsAdminExperimentUsers.setString(1, ExperimentUserColumns.ADMIN_TYPE);
        statementUpdateAsAdminExperimentUsers.setLong(2, experimentId);
        log.info(statementUpdateAsAdminExperimentUsers.toString());
        statementUpdateAsAdminExperimentUsers.execute();
      }
      // for participants
      if (asParticipant != null && asParticipant.size() > 0) {
        userIdInCondition.setRightItemsList(participantIds);
        statementUpdateAsParticipantExperimentUsers = conn.prepareStatement(update.toString());
        statementUpdateAsParticipantExperimentUsers.setString(1, ExperimentUserColumns.PARTICIPANT_TYPE);
        statementUpdateAsParticipantExperimentUsers.setLong(2, experimentId);
        log.info(statementUpdateAsParticipantExperimentUsers.toString());
        statementUpdateAsParticipantExperimentUsers.execute();
      }
      conn.commit();
      isSuccess = true;
    } finally {
      try {
        if (statementUpdateAsAdminExperimentUsers != null) {
          statementUpdateAsAdminExperimentUsers.close();
        }
        if (statementUpdateAsParticipantExperimentUsers != null) {
          statementUpdateAsParticipantExperimentUsers.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
      }
    }
    return isSuccess;
  }
 
  private Integer getMaxAnonId(List<PacoUser> userLst) throws SQLException {
    Iterator<PacoUser> itr = userLst.iterator();
    int maxAnonId = 0;
    while (itr.hasNext()) { 
      PacoUser crtUser = itr.next();
      if (maxAnonId < crtUser.getAnonId()) {
        maxAnonId = crtUser.getAnonId();
      }
    }
    return maxAnonId;
  }
  
  private void insertUserForExperiment(Long experimentId, String email) throws SQLException {
    PacoUser pacoUser = null;
    Integer newAnonId = null;
    if (experimentId == null || email == null) {
      experimentId = 0L;
      log.info("create Anon Id with expId" + experimentId + " with email : "+ email);
      return;
    } 
   
    try { 
      User user = userDaoImpl.getUserAndCreate(email, true);
      PacoId userId = user.getUserId();
      List<PacoUser> toBeInsertedIntoExptUserTable = Lists.newArrayList();

      // Check the current admin status for email for this experiment and then update experiment_users table
      boolean isAdmin = ExperimentAccessManager.isUserAdmin(experimentId, email);
      newAnonId = getMaxAnonId(experimentId) + 1;
      if (isAdmin) {
        pacoUser = new PacoUser(userId.getId(), newAnonId, ExperimentUserColumns.ADMIN_TYPE.charAt(0), email);
      } else {
        pacoUser = new PacoUser(userId.getId(), newAnonId, ExperimentUserColumns.PARTICIPANT_TYPE.charAt(0), email);
      }
      toBeInsertedIntoExptUserTable.add(pacoUser);
      insertIntoExperimentUsers(experimentId, toBeInsertedIntoExptUserTable);
    } catch (SQLException sqle) {
      failedEventDaoImpl.insertFailedEvent(experimentId.toString() + " -- "+ email, ErrorMessages.SQL_INSERT_EXCEPTION.getDescription() + "Admin/Participant", sqle.getMessage());
      log.warning(ErrorMessages.SQL_INSERT_EXCEPTION.getDescription() + " for  Admin/ Participant request: " + experimentId + " : " + ExceptionUtil.getStackTraceAsString(sqle));
    } catch (Exception e) {
      failedEventDaoImpl.insertFailedEvent(experimentId.toString() + " -- "+ email, ErrorMessages.GENERAL_EXCEPTION.getDescription(), e.getMessage());
      log.warning(ErrorMessages.GENERAL_EXCEPTION.getDescription() + " for  Admin/ Participant request: " + experimentId + " : " + ExceptionUtil.getStackTraceAsString(e));
    }
  }

  private Integer getAnonymousId(Long experimentId, String email) throws SQLException {
    Connection conn = null;
    ResultSet rs = null;
    Integer anonId = null;
    PreparedStatement statementGetAnonId = null;
    if (experimentId != null && email != null) {
     
      try {
        conn = CloudSQLConnectionManager.getInstance().getConnection();
        statementGetAnonId = conn.prepareStatement(QueryConstants.GET_ANON_ID_FOR_EMAIL.toString());
        statementGetAnonId.setLong(1, experimentId);
        statementGetAnonId.setString(2, email);
        rs = statementGetAnonId.executeQuery();
        if (rs.next()){
          anonId = rs.getInt(ExperimentUserColumns.EXP_USER_ANON_ID);
        }
      } finally {
        try {
          if ( rs != null) {
            rs.close();
          }
          if (statementGetAnonId != null) {
            statementGetAnonId.close();
          }
          if (conn != null) {
            conn.close();
          }
        } catch (SQLException ex1) {
          log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
        }
      }
    } else {
      log.info("get anonymous id with experiment id: "+ experimentId );
    }
    return anonId;
  }
}
