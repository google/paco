package com.google.sampling.experiential.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@SuppressWarnings("serial")
public class CloudSqlServlet extends HttpServlet {
  public static final Logger log = Logger.getLogger(ExperimentServlet.class.getName());

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException,
  ServletException {

    doGet(req, resp);
  }
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException,
      ServletException {
    log.info("in cloudsql");
    String path = req.getRequestURI();
    StringBuilder json = new StringBuilder("");
    String line=null;
//    if(req.getInputStream()!=null){
      while((line = req.getReader().readLine())!=null){
        json.append(line);
      }
      log.info("reqbody is"+ json.toString());
//    }
      
//    }
//    if (path.startsWith("/favicon.ico")) {
//      return; // ignore the request for favicon.ico
//    }
//    // store only the first two octets of a users ip address
//    String userIp = req.getRemoteAddr();
//    InetAddress address = InetAddress.getByName(userIp);
//    if (address instanceof Inet6Address) {
//      // nest indexOf calls to find the second occurrence of a character in a string
//      // an alternative is to use Apache Commons Lang: StringUtils.ordinalIndexOf()
//      userIp = userIp.substring(0, userIp.indexOf(":", userIp.indexOf(":") + 1)) + ":*:*:*:*:*:*";
//    } else if (address instanceof Inet4Address) {
//      userIp = userIp.substring(0, userIp.indexOf(".", userIp.indexOf(".") + 1)) + ".*.*";
//    }
//
//    final String createTableSql = "CREATE TABLE IF NOT EXISTS visits ( visit_id INT NOT NULL "
//        + "AUTO_INCREMENT, user_ip VARCHAR(46) NOT NULL, timestamp DATETIME NOT NULL, "
//        + "PRIMARY KEY (visit_id) )";
//    final String createVisitSql = "INSERT INTO visits (user_ip, timestamp) VALUES (?, ?)";
//    final String selectSql = "SELECT user_ip, timestamp FROM visits ORDER BY timestamp DESC "
//        + "LIMIT 10";
    final String selectSql = "SELECT * FROM event "
          + "LIMIT 10";


    PrintWriter out = resp.getWriter();
    resp.setContentType("text/plain");
    String url;

    if (System
        .getProperty("com.google.appengine.runtime.version").startsWith("Google App Engine/")) {
      // Check the System properties to determine if we are running on appengine or not
      // Google App Engine sets a few system properties that will reliably be present on a remote
      // instance.
      url = System.getProperty("ae-cloudsql.cloudsql-database-url");
      try {
        // Load the class that provides the new "jdbc:google:mysql://" prefix.
        Class.forName("com.mysql.jdbc.GoogleDriver");
      } catch (ClassNotFoundException e) {
        throw new ServletException("Error loading Google JDBC Driver", e);
      }
    } else {
      // Set the url with the local MySQL database connection url when running locally
      url = System.getProperty("ae-cloudsql.local-database-url");
    }
    log.info("connecting to: " + url);
    
//    try (Connection conn = DriverManager.getConnection(url);
//        PreparedStatement statementCreateVisit = conn.prepareStatement(createVisitSql)) {
//      conn.createStatement().executeUpdate(createTableSql);
//      statementCreateVisit.setString(1, userIp);
//      statementCreateVisit.setTimestamp(2, new Timestamp(new Date().getTime()));
//      statementCreateVisit.executeUpdate();

      try{
        Connection conn = DriverManager.getConnection(url);
      
        ResultSet rs = conn.prepareStatement(selectSql).executeQuery();
      
        out.print("Last 10 records:\n");
        while (rs.next()) {
          String savedIp = rs.getString("idName");
          String timeStamp = rs.getString("entryDate");
          out.print("Time: " + timeStamp + " Addr: " + savedIp + "\n");
        }
      
      } catch (SQLException e) {
        throw new ServletException("SQL error", e);
      }
  }
}