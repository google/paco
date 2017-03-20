package com.google.sampling.experiential.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

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
        throw new SQLException("DataSourceSetUp", e);
      } catch (Exception ex) {
        throw new SQLException("DataSourceSetUp", ex);
      }
    }
    return instance;
  }

  public Connection getConnection() throws SQLException {
    Connection conn = ds.getConnection();
    return conn;
  }

  private static DataSource setUp() throws ClassNotFoundException, Exception {
    String url = null;
    String userName = null;
    String password = null;

    connectionPool = new GenericObjectPool();

    try {
      int maxConnections = Integer.parseInt(System.getProperty("ae-connection-pool.maxconn"));
      connectionPool.setMaxActive(maxConnections);
    } catch (NumberFormatException nfe) {
      throw new Exception("Max connections not set to Integer in config file.");
    }

    if (System.getProperty("com.google.appengine.runtime.version").startsWith("Google App Engine/")) {
      url = System.getProperty("ae-cloudsql.database-url");
      userName = System.getProperty("ae-cloudsql.database-username");
      password = System.getProperty("ae-cloudsql.database-password");
      Class.forName("com.mysql.jdbc.GoogleDriver");
    } else {
      url = System.getProperty("ae-cloudsql.local-database-url");
      userName = System.getProperty("ae-cloudsql.local-database-username");
      password = System.getProperty("ae-cloudsql.local-database-password");
    }

    ConnectionFactory cf = new DriverManagerConnectionFactory(url, userName, password);
    PoolableConnectionFactory pcf = new PoolableConnectionFactory(cf, connectionPool, null, null, false, true);
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
