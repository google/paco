package com.google.sampling.experiential.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class CloudSQLConnectionManager {
  static String url;
  public static final Logger log = Logger.getLogger(ExperimentServlet.class.getName());

  public CloudSQLConnectionManager(){
  
  }
  
  public static Connection getConnection() {
    Connection conn = null;
    try{
      if (System.getProperty("com.google.appengine.runtime.version").startsWith("Google App Engine/")) {
        // Check the System properties to determine if we are running on appengine or not
        // Google App Engine sets a few system properties that will reliably be present on a remote
        // instance.
        url = System.getProperty("ae-cloudsql.cloudsql-database-url");
        
          // Load the class that provides the new "jdbc:google:mysql://" prefix.
          Class.forName("com.mysql.jdbc.GoogleDriver");
  
      } else {
        // Set the url with the local MySQL database connection url when running locally
        url = System.getProperty("ae-cloudsql.local-database-url");
      }
      log.info("connecting to: " + url);
      
       conn = DriverManager.getConnection(url);
    }catch (ClassNotFoundException c){
      
    }catch (SQLException s){
      
    }
    return conn;
    
  }
}
