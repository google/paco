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

import org.joda.time.DateTime;

import com.google.appengine.api.users.User;
import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.model2.SQLQuery;
import com.pacoapp.paco.shared.util.JsUtil;
import com.pacoapp.paco.shared.util.SearchUtil;



@SuppressWarnings("serial")
public class CloudSqlSearchServlet extends HttpServlet {
  public static final Logger log = Logger.getLogger(CloudSqlSearchServlet.class.getName());

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException,
  ServletException {
    throw new ServletException("Method not supported");
  }
  
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException,
      ServletException {
    setCharacterEncoding(req, resp);
    User user = AuthUtil.getWhoFromLogin();
    String loggedInUser = null;
    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      loggedInUser = AuthUtil.getEmailOfUser(req, user);
      SQLQuery sqlQueryObj = null;
      DateTime startTime = null;
      List<EventDAO> evtList = null;
      
      PrintWriter out = resp.getWriter();
      resp.setContentType("text/plain");
      
      CloudSQLDao impl = new CloudSQLDaoImpl();
      String reqBody = RequestProcessorUtil.getBody(req);    
      sqlQueryObj = JsUtil.convertJSONToPOJO(reqBody);    
      String selectSql = SearchUtil.getPlainSql(sqlQueryObj);
      
      List<String> userSpecifiedWhoValues = SearchUtil.retrieveUserSpecifiedConditions(selectSql, "who");
      List<String> userSpecifiedExpIdValues = SearchUtil.retrieveUserSpecifiedConditions(selectSql, "experiment_id");
      List<Long> adminExperimentsinDB = ExperimentAccessManager.getExistingExperimentIdsForAdmin(loggedInUser, 0, null).getExperiments();
      
      //if loggedinUser is admin
      if(adminExperimentsinDB.size()>0){
        //if expid in qry filter
        if(userSpecifiedExpIdValues.size()>0){
          //chk if exp list in query filter is all present in db as admin
          if(adminExperimentsinDB.containsAll(userSpecifiedExpIdValues)){
            //no change 
          }else{
            //chk who in qry filter
            if(userSpecifiedWhoValues.size()>0){
              //if who equals loggedinuser
              for(String s : userSpecifiedWhoValues){
                if(s.equalsIgnoreCase(loggedInUser)){
                  //do nothing
                }else{
                  //he is admin of some expt, but he is querying some expt where he is not admin, and with a who value which is not his id.
                  throw new ServletException("Unauthorized access");
                }
              }
            }else{
              //add who=loggedinuser
              if(selectSql.contains(" where ")){
                selectSql = selectSql.replace(" where "," where who = '"+loggedInUser+"' and ");
              }else if(selectSql.contains(" events ")){
                selectSql = selectSql.replace(" events ", " events where who ='"+loggedInUser+"'");
              }  
            }
          }
          
        }else{
          //add all adminExperimentsinDB in expid filter to qry
          if(selectSql.contains(" where ")){
            selectSql = selectSql.replace(" where "," where experiment_id in( "+adminExperimentsinDB+") and ");
          }else if(selectSql.contains(" events ")){
            selectSql = selectSql.replace(" events ", " events where experiment_id in("+adminExperimentsinDB+")");
          }
        }
        
      }else{
        //add who=loggedinuser
        if(selectSql.contains(" where ")){
          selectSql = selectSql.replace(" where "," where who = '"+loggedInUser+"' and ");
        }else if(selectSql.contains(" events ")){
          selectSql = selectSql.replace(" events ", " events where who ='"+loggedInUser+"'");
        }
      }
      
      log.info("after acl"+selectSql);
      
      List<String> colNamesInQuery = SearchUtil.getAllColNamesInQuery(selectSql);
      String tablesInvolved = SearchUtil.identifyTablesInvolved(colNamesInQuery);
      
      if (tablesInvolved!=null && tablesInvolved.equals("eventsoutputs")){
        selectSql = selectSql.replace(" from events ", " from events join outputs on events._id = outputs.event_Id ");
      }
      
      log.info("send query "+ selectSql);
      startTime = new DateTime();
      try{
        evtList = impl.getEvents(selectSql);
      }catch(SQLException sqle){
        throw new ServletException("SQL - "+ sqle);
      }
  
      long diff = new DateTime().getMillis()-startTime.getMillis();
      log.info("complete search qry took "+ diff);
      for(EventDAO evt : evtList){
          out.print("Id:"+evt.getId());
          out.print("expId:"+evt.getExperimentId());
          out.print("expName:"+evt.getExperimentName());
          out.print("out size:"+evt.getResponses().size());
          out.print("expGrpName:"+evt.getExperimentGroupName());
          out.print("who:"+evt.getWho());
          out.print("sch time:"+new java.util.Date(evt.getScheduledTime().getTime()));
          out.print("pacoversion:"+evt.getPacoVersion());
          out.print("appid:"+evt.getAppId());
          out.println();
  
      }
    }
  
  }
  private void setCharacterEncoding(HttpServletRequest req, HttpServletResponse resp)
          throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }

}