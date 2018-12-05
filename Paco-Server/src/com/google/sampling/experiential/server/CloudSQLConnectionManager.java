package com.google.sampling.experiential.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import com.pacoapp.paco.shared.util.ErrorMessages;

public class CloudSQLConnectionManager {
  public static final Logger log = Logger.getLogger(CloudSQLConnectionManager.class.getName());

  private static CloudSQLConnectionManager instance = null;
  private static DataSource ds = null;
  private static GenericObjectPool connectionPool = null;

  private CloudSQLConnectionManager() {

  }

  public static synchronized CloudSQLConnectionManager getInstance() throws SQLException {
    if (instance == null) {
      try {
        ds = setUp();
        instance = new CloudSQLConnectionManager();
      } catch (ClassNotFoundException e) {
        log.warning( ErrorMessages.DATASOURCE_SETUP_EXCEPTION.getDescription() + e.getMessage());
        throw new SQLException(ErrorMessages.DATASOURCE_SETUP_EXCEPTION.getDescription(), e);
      } catch (Exception ex) {
        log.warning(ErrorMessages.GENERAL_EXCEPTION.getDescription() + ex.getMessage());
        throw new SQLException(ErrorMessages.GENERAL_EXCEPTION.getDescription(), ex);
      }
    }
    return instance;
  }

  public Connection getConnection() throws SQLException {
    Connection conn = ds.getConnection();
    conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
    setNames(conn);
    return conn;
  }
  
  private boolean setNames(Connection conn) throws SQLException {
    boolean isDone = false;
    java.sql.Statement statementSetNames = null;

    try {
      statementSetNames = conn.createStatement();
      statementSetNames.execute(QueryConstants.SET_NAMES.toString());
      isDone = true;
    } finally {
      try {
        if (statementSetNames != null) {
          statementSetNames.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
      }
    }
    return isDone;
  }


  private static DataSource setUp() throws ClassNotFoundException, Exception {
    String url = null;
    String userName = null;
    String password = null;

    connectionPool = new GenericObjectPool();

    try {
      int maxConnections = Integer.parseInt(System.getProperty("ae-connection-pool.maxconn"));
      connectionPool.setMaxActive(maxConnections);
      connectionPool.setMaxIdle(maxConnections);
      connectionPool.setTestWhileIdle(true);
      connectionPool.setTestOnBorrow(true);
    } catch (NumberFormatException nfe) {
      throw new Exception("Max connections not set to Integer in config file.");
    }

    if (!EnvironmentUtil.isDevInstance()) {
      url = System.getProperty("ae-cloudsql.database-url");
      userName = System.getProperty("ae-cloudsql.database-username");
      password = System.getProperty("ae-cloudsql.database-password");
      Class.forName("com.mysql.jdbc.GoogleDriver");
    } else {
      url = System.getProperty("ae-cloudsql.local-database-url");
      userName = System.getProperty("ae-cloudsql.local-database-username");
      password = System.getProperty("ae-cloudsql.local-database-password");
    }
    KeyedObjectPoolFactory kopf = new GenericKeyedObjectPoolFactory(null);

    ConnectionFactory cf = new DriverManagerConnectionFactory(url, userName, password);
    PoolableConnectionFactory pcf = new PoolableConnectionFactory(cf, connectionPool, kopf, "select 1", false, true);
    ds = new PoolingDataSource(connectionPool);
    return ds;
  }

  private static GenericObjectPool getConnectionPool() {
    return connectionPool;
  }

  static void currentPoolStatus() {
    log.info("Max = " + getConnectionPool().getMaxActive() + " : " + "Active = " + getConnectionPool().getNumActive());
  }
}
