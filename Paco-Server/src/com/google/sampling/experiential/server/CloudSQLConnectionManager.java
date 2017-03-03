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
  
  private CloudSQLConnectionManager(){
    
  }
  
  public static CloudSQLConnectionManager getInstance() throws SQLException{
    if (instance == null){
      try {
        ds = setUp();
        instance = new CloudSQLConnectionManager();
      } catch (ClassNotFoundException e) {
        throw new SQLException("DataSourceSetUp",e);
      }
    }
    return instance;
  }

  public  Connection getConnection() throws SQLException{
    Connection conn = ds.getConnection();
    return conn;
  }
  
  public static DataSource setUp() throws ClassNotFoundException {
    String url = null;
    String userName = null;
    String pwd = null;

    connectionPool = new GenericObjectPool();
    connectionPool.setMaxActive(100);
    
    if (System.getProperty("com.google.appengine.runtime.version").startsWith("Google App Engine/")) {
      url = "jdbc:google:mysql://quantifiedself-staging2:us-central1:quantifiedself-staging2-sql/test";
      userName = "imeyyappan";
      pwd = "password2";
      Class.forName("com.mysql.jdbc.GoogleDriver");
    } else {
      url = "jdbc:mysql://localhost:3306/pacodb?verifyServerCertificate=false&useSSL=false";
//        url = "jdbc:mysql://127.0.0.1:4040/pacodb?verifyServerCertificate=false&useSSL=false";
      userName = "root";
      pwd ="mira@2008";
    }

    ConnectionFactory cf = new DriverManagerConnectionFactory(url,userName,pwd);
    PoolableConnectionFactory pcf = new PoolableConnectionFactory(cf, connectionPool,null, null, false, true);
    ds = new PoolingDataSource(connectionPool);
    return ds;
    
  }
  
  public static  GenericObjectPool getConnectionPool() {
    return connectionPool;
  }
  
  static void currentPoolStatus() {
    System.out.println("Max = " + getConnectionPool().getMaxActive() + " : " +
            "Active = " + getConnectionPool().getNumActive() );
            
  }
}
