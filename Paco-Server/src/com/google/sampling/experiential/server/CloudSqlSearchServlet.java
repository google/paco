package com.google.sampling.experiential.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.model2.SQLQuery;
import com.pacoapp.paco.shared.util.JsUtil;
import com.pacoapp.paco.shared.util.SearchUtil;



@SuppressWarnings("serial")
public class CloudSqlSearchServlet extends HttpServlet {
  public static final Logger log = Logger.getLogger(CloudSqlSearchServlet.class.getName());

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException,
  ServletException {

    doGet(req, resp);
  }
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException,
      ServletException {
    
    log.info("in cloudsql search");
    SQLQuery sqlQueryObj =null;
    CloudSQLDao impl = new CloudSQLDaoImpl();

    String reqBody = RequestProcessorUtil.getBody(req);
    
    sqlQueryObj = JsUtil.convertJSONToPOJO(reqBody);
    
    String selectSql = SearchUtil.getPlainSql(sqlQueryObj);
    
    List<String> colNamesInQuery = SearchUtil.getAllColNamesInQuery(selectSql);
   
    String tablesInvolved = SearchUtil.identifyTablesInvolved(colNamesInQuery);
    if (tablesInvolved!=null && tablesInvolved.equals("eventsoutputs")){
      System.out.println("all cols new approach contains answer/text");
      selectSql = selectSql.replace(" from events ", " from events join outputs on events._id = outputs.event_Id ");
    }
    
    PrintWriter out = resp.getWriter();
    resp.setContentType("text/plain");
    List<EventDAO> evtList = impl.getEvents(selectSql);
   
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