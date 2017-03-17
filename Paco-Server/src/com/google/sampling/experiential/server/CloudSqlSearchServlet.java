package com.google.sampling.experiential.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTimeZone;

import com.google.appengine.api.users.User;
import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.model2.SQLQuery;
import com.pacoapp.paco.shared.util.QueryJsonParser;
import com.pacoapp.paco.shared.util.SearchUtil;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.select.PlainSelect;

@SuppressWarnings("serial")
public class CloudSqlSearchServlet extends HttpServlet {
  public static final Logger log = Logger.getLogger(CloudSqlSearchServlet.class.getName());

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    throw new ServletException("Method not supported");
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    setCharacterEncoding(req, resp);
    User user = AuthUtil.getWhoFromLogin();
    String loggedInUser = null;
    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      loggedInUser = AuthUtil.getEmailOfUser(req, user);
      SQLQuery sqlQueryObj = null;
      List<EventDAO> evtList = null;
      String aclQuery = null;
      PlainSelect ps = null;

      PrintWriter out = resp.getWriter();
      resp.setContentType("text/plain");

      CloudSQLDao impl = new CloudSQLDaoImpl();
      String reqBody = RequestProcessorUtil.getBody(req);
      sqlQueryObj = QueryJsonParser.parseSqlQueryFromJson(reqBody);

      List<Long> adminExperimentsinDB = ExperimentAccessManager.getExistingExperimentIdsForAdmin(loggedInUser, 0, null)
                                                               .getExperiments();

      try{
        ps = SearchUtil.getJoinQry(sqlQueryObj);
      } catch (JSQLParserException pe){
        throw new ServletException("Invalid Json Query");
      }

      try {
        aclQuery = ACLHelper.getModifiedQueryBasedOnACL(ps.toString(), loggedInUser, adminExperimentsinDB);
      } catch (Exception e) {
        throw new ServletException("Unauthorized acccess" + e);
      }
      
      long startTime = System.currentTimeMillis();
      DateTimeZone tzForClient = TimeUtil.getTimeZoneForClient(req);

      try {
        evtList = impl.getEvents(aclQuery, tzForClient);
      } catch (SQLException sqle) {
        throw new ServletException("SQL - " + sqle);
      }

      long diff = System.currentTimeMillis() - startTime;
      log.info("complete search qry took " + diff);
      for (EventDAO evt : evtList) {
        out.println(evt);
      }
    }

  }

  private void setCharacterEncoding(HttpServletRequest req,
                                    HttpServletResponse resp) throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }

}