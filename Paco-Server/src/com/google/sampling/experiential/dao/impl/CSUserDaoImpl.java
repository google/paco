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

import com.google.cloud.sql.jdbc.Statement;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.sampling.experiential.cloudsql.columns.UserColumns;
import com.google.sampling.experiential.dao.CSUserDao;
import com.google.sampling.experiential.dao.dataaccess.User;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.PacoId;
import com.pacoapp.paco.shared.util.ErrorMessages;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

public class CSUserDaoImpl implements CSUserDao {
  public static final Logger log = Logger.getLogger(CSUserDaoImpl.class.getName());
  private static List<Column> userColList = Lists.newArrayList();
  static {
    userColList.add(new Column(UserColumns.WHO));
  }
  @Override
  public User getUserAndCreate(String email, boolean createOption) throws SQLException {
    User user = new User();
    PacoId userId = new PacoId();
    Set<String> userSet = Sets.newHashSet();
    userSet.add(email);
    Map<String, Long> singleUserMap = getUserIdsForEmails(userSet);
    Set<String> key = singleUserMap.keySet();
    Iterator<String> itr = key.iterator();
    if (itr.hasNext()) { 
      userId.setId(singleUserMap.get(itr.next()));
      userId.setIsCreatedWithThisCall(false);
    } else if (createOption) {
      userId.setId(insertUserAndRetrieveId(email));
      userId.setIsCreatedWithThisCall(true);
    } else {
      //TODO not sure if this is a good option to set to 0
      userId.setId(0L);
      userId.setIsCreatedWithThisCall(false);
    }
    user.setUserId(userId);
    user.setWho(email);
    
    return user;
  }
  
  @Override
  public Map<String, Long> getUserIdsForEmails(Set<String> userEmailLst) throws SQLException {
    Connection conn = null;
    PreparedStatement findUsersStatement = null;
    ResultSet rs = null;
    Map<String, Long> userIds = Maps.newHashMap();
    int ct = 1;
    try {
      if (userEmailLst != null) {
        conn = CloudSQLConnectionManager.getInstance().getConnection();
        
        List<Expression> lstExpr = Lists.newArrayList();
        Iterator<String> emailIterator = userEmailLst.iterator();
        while (emailIterator.hasNext()) {
          lstExpr.add(new JdbcParameter());
          emailIterator.next();
        }
        
        ExpressionList exprList = new ExpressionList();
        exprList.setExpressions(lstExpr);
        
        InExpression emailInClause = new InExpression();
        emailInClause.setLeftExpression(new Column(UserColumns.WHO));
        emailInClause.setRightItemsList(exprList);
        
        SelectItem userId = new SelectExpressionItem();
        ((SelectExpressionItem)userId).setExpression(new Column(UserColumns.USER_ID));
        SelectItem userEmail = new SelectExpressionItem();
        ((SelectExpressionItem)userEmail).setExpression(new Column(UserColumns.WHO));
        
        List<SelectItem> selectColLst = Lists.newArrayList();
        selectColLst.add(userId);
        selectColLst.add(userEmail);
        
        PlainSelect selectIdEmailQry = new PlainSelect(); 
        selectIdEmailQry.setFromItem(new Table(UserColumns.TABLE_NAME));
        selectIdEmailQry.setSelectItems(selectColLst);
        selectIdEmailQry.setWhere(emailInClause);
  
        findUsersStatement = conn.prepareStatement(selectIdEmailQry.toString());
        for (String s: userEmailLst) {
          findUsersStatement.setString(ct++, s);
        }
        rs = findUsersStatement.executeQuery();
        while (rs.next()) {
          userIds.put(rs.getString(UserColumns.WHO), rs.getLong(UserColumns.USER_ID));
        }
      } else {
        log.warning("user email list is " +  userEmailLst);
      }
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (findUsersStatement != null) {
          findUsersStatement.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }

    return userIds;
  }
 
  private Long insertUserAndRetrieveId(String email) throws SQLException {
    Connection conn = null;
    PreparedStatement statementCreateUser = null;
    ResultSet rs = null;
    ExpressionList insertEventExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    Insert userInsert = new Insert();
    Long userId = null;
    if (email != null) {
      try {
        conn = CloudSQLConnectionManager.getInstance().getConnection();
        conn.setAutoCommit(false);
        userInsert.setTable(new Table(UserColumns.TABLE_NAME));
        userInsert.setUseValues(true);
        insertEventExprList.setExpressions(exp);
        userInsert.setItemsList(insertEventExprList);
        userInsert.setColumns(userColList);
        // Adding ? for prepared stmt
        for (Column c : userColList) {
          ((ExpressionList) userInsert.getItemsList()).getExpressions().add(new JdbcParameter());
        }
  
        statementCreateUser = conn.prepareStatement(userInsert.toString(), Statement.RETURN_GENERATED_KEYS);
        statementCreateUser.setString(1, email);
        statementCreateUser.execute();
        rs = statementCreateUser.getGeneratedKeys();
        if (rs.next()) {
          userId = rs.getLong(1);
        }
        conn.commit();
      } catch(SQLException sqle) {
        log.warning("Exception while inserting to user table" + email + ":" +  sqle);
      }
      finally {
        try {
          if( rs != null) { 
            rs.close();
          }
          if (statementCreateUser != null) {
            statementCreateUser.close();
          }
          if (conn != null) {
            conn.close();
          }
        } catch (SQLException ex1) {
          log.info(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
        }
      }
    } else {
      log.warning("insert user email:"+ email);
    }
    return userId;
  }

}
