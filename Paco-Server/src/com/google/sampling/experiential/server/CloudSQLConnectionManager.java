package com.google.sampling.experiential.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

public class CloudSQLConnectionManager {
  static String url;
  static BasicDataSource ds;
  public static final Logger log = Logger.getLogger(CloudSQLConnectionManager.class.getName());

  public CloudSQLConnectionManager(){
  
  }
  
  public static Connection getConnection() {
    Connection conn = null;
    try {
      conn = getDataSource().getConnection();
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return conn;
    
  }
  
  /**
   *  
   * @return
   * @throws ClassNotFoundException
   */
  
  private static DataSource getDataSource() throws ClassNotFoundException {
    if(ds==null){
     ds = new BasicDataSource();
    }
    if (System
        .getProperty("com.google.appengine.runtime.version").startsWith("Google App Engine/")) {
//       Check the System properties to determine if we are running on appengine or not
//       Google App Engine sets a few system properties that will reliably be present on a remote
//       instance.     
      ds.setUrl("jdbc:google:mysql://quantifiedself-staging2:us-central1:quantifiedself-staging2-sql/test");
      ds.setUsername("imey****");
      ds.setPassword("*****");
   
      Class.forName("com.mysql.jdbc.GoogleDriver");
    } else {
//       Set the url with the local MySQL database connection url when running locally
//      ds.setDriver("org.gjt.mm.mysql.Driver");
      ds.setUrl("jdbc:mysql://localhost:3306/pacodb");
      ds.setUsername("root");
      ds.setPassword("*****");
    }
    log.log(Level.FINE,"connecting to: " + ds.getUrl());
//   common properties
    ds.setMinIdle(5);
    ds.setMaxIdle(20);
    ds.setMaxOpenPreparedStatements(180);
    return ds;
  }
}
