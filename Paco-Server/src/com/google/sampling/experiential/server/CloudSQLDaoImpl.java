package com.google.sampling.experiential.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.Output;
import com.pacoapp.paco.shared.model2.EventBaseColumns;
import com.pacoapp.paco.shared.model2.OutputBaseColumns;
import com.pacoapp.paco.shared.model2.SQLQuery;
import com.pacoapp.paco.shared.util.SearchUtil;

public class CloudSQLDaoImpl implements CloudSQLDao{
  public static final Logger log = Logger.getLogger(CloudSQLDaoImpl.class.getName());
//  private static  Map<EventTableColumns, EventTableColumns> eventsColumns = null;
  private static  Map<String, Integer> eventsColumns = null;
  
  private static void loadColumnTableAssociationMap(){
    if (eventsColumns ==null){
      eventsColumns = new HashMap<String,Integer>();
      eventsColumns.put("EXPERIMENT_ID", 1);
      eventsColumns.put("EXPERIMENT_SERVER_ID", 2);
      eventsColumns.put("EXPERIMENT_NAME", 3);
      eventsColumns.put("EXPERIMENT_VERSION", 4);
      eventsColumns.put("SCHEDULE_TIME", 5);
      eventsColumns.put("RESPONSE_TIME", 6);
      eventsColumns.put("UPLOADED", 7);
      eventsColumns.put("GROUP_NAME", 8);
      eventsColumns.put("ACTION_TRIGGER_ID",9);
      eventsColumns.put("ACTION_TRIGGER_SPEC_ID", 10);
      eventsColumns.put("ACTION_ID", 11);
      eventsColumns.put("WHO", 12);
      eventsColumns.put("LAT", 13);
      eventsColumns.put("LON", 14);
      eventsColumns.put("PACO_VERSION", 15);
      eventsColumns.put("EVENTS._ID", 16);
      eventsColumns.put("_ID", 17);
       
//      eventsColumns = new EnumMap<EventTableColumns,EventTableColumns>(EventTableColumns.class);
//      eventsColumns.put(EventTableColumns.EXPERIMENT_ID, EventTableColumns.EXPERIMENT_ID);
//      eventsColumns.put(EventTableColumns.EXPERIMENT_SERVER_ID, EventTableColumns.EXPERIMENT_SERVER_ID);
//      eventsColumns.put(EventTableColumns.EXPERIMENT_NAME, EventTableColumns.EXPERIMENT_NAME);
//      eventsColumns.put(EventTableColumns.EXPERIMENT_VERSION, EventTableColumns.EXPERIMENT_VERSION);
//      eventsColumns.put(EventTableColumns.SCHEDULE_TIME, EventTableColumns.SCHEDULE_TIME);
//      eventsColumns.put(EventTableColumns.RESPONSE_TIME, EventTableColumns.RESPONSE_TIME);
//      eventsColumns.put(EventTableColumns.UPLOADED, EventTableColumns.UPLOADED);
//      eventsColumns.put(EventTableColumns.GROUP_NAME, EventTableColumns.GROUP_NAME);
//      eventsColumns.put(EventTableColumns.ACTION_TRIGGER_ID, EventTableColumns.ACTION_TRIGGER_ID);
//      eventsColumns.put(EventTableColumns.ACTION_TRIGGER_SPEC_ID, EventTableColumns.ACTION_TRIGGER_SPEC_ID);
//      eventsColumns.put(EventTableColumns.ACTION_ID, EventTableColumns.ACTION_ID);
//      eventsColumns.put(EventTableColumns.WHO, EventTableColumns.WHO);
//      eventsColumns.put(EventTableColumns.LAT, EventTableColumns.LAT);
//      eventsColumns.put(EventTableColumns.LON, EventTableColumns.LON);
//      eventsColumns.put(EventTableColumns.PACO_VERSION,EventTableColumns.PACO_VERSION);
//      eventsColumns.put(EventTableColumns.EVENTS_ID, EventTableColumns.EVENTS_ID);
//      eventsColumns.put(EventTableColumns._ID,EventTableColumns._ID);
    }
  }
 
  @Override
  public void insertEvent(EventDAO event) {
    if (event == null){
      log.severe("nothing to insert");
      return;
    }
    try{
      Connection conn = null;
      conn = CloudSQLConnectionManager.getConnection();
       
      String insertEventSql = "INSERT INTO events ("
              +"_ID,"
              + EventBaseColumns.ACTION_ID +","
              + EventBaseColumns.ACTION_TRIGGER_ID +","
              + EventBaseColumns.ACTION_TRIGGER_SPEC_ID +","
              + EventBaseColumns.GROUP_NAME +","
              + EventBaseColumns.EXPERIMENT_SERVER_ID +","
              + EventBaseColumns.EXPERIMENT_NAME +","
              + EventBaseColumns.EXPERIMENT_VERSION +","
              + EventBaseColumns.LAT +","
              + EventBaseColumns.LON +","
              + EventBaseColumns.RESPONSE_TIME +","
              + EventBaseColumns.SCHEDULE_TIME +","
//              + "`when`, "
              + EventBaseColumns.WHO  +")"
                      + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
      PreparedStatement statementCreateEvent = conn.prepareStatement(insertEventSql);
      statementCreateEvent.setLong(1, event.getId()==null?0L:event.getId());
      statementCreateEvent.setLong(2, event.getActionId()==null?0L:event.getActionId());
      statementCreateEvent.setLong(3, event.getActionTriggerId()==null?0L:event.getActionTriggerId());
      statementCreateEvent.setLong(4, event.getActionTriggerSpecId()==null?0L:event.getActionTriggerSpecId());
      
      statementCreateEvent.setString(5, event.getExperimentGroupName()==null?"":event.getExperimentGroupName());
      statementCreateEvent.setLong(6, event.getExperimentId()==null?0L:event.getExperimentId());
      statementCreateEvent.setString(7, event.getExperimentName()==null?"":event.getExperimentName());
      statementCreateEvent.setInt(8, event.getExperimentVersion()==null?0:event.getExperimentVersion());
      
      statementCreateEvent.setString(9, event.getLat()==null?"":event.getLat());
      statementCreateEvent.setString(10, event.getLon()==null?"":event.getLon());
      statementCreateEvent.setTimestamp(11, new java.sql.Timestamp(event.getResponseTime()==null?new Date().getTime():event.getResponseTime().getTime()));
      statementCreateEvent.setTimestamp(12, new java.sql.Timestamp(event.getScheduledTime()==null?new Date().getTime():event.getScheduledTime().getTime()));
      statementCreateEvent.setString(13, event.getWho());
      statementCreateEvent.execute();
     
    
    } catch (SQLException e) {
      log.info("sqlexception while inserting event"+e);
    }
  }
  
  @Override
  public void insertOutputs(EventDAO event) {
    if (event == null){
      log.severe("nothing to insert");
      return;
    }
    try{
      Connection conn = null;
      conn = CloudSQLConnectionManager.getConnection();
     
      String insertEventOutputsSql = "INSERT INTO outputs ("
              + OutputBaseColumns.EVENT_ID +","
              + OutputBaseColumns.NAME +","
              + OutputBaseColumns.ANSWER 
              + ") VALUES (?, ?, ?)";
      PreparedStatement statementCreateEventOutput = conn.prepareStatement(insertEventOutputsSql);
        
      for(String key : event.getWhat().keySet()){
        String whatAnswer = event.getWhatByKey(key);
        statementCreateEventOutput.setLong(1, event.getId()==null?0L:event.getId());
        statementCreateEventOutput.setString(2, key);
        statementCreateEventOutput.setString(3, whatAnswer==null?"":whatAnswer);
        statementCreateEventOutput.execute();
      }    
    } catch (SQLException e) {
      log.info("sqlexception while inserting output"+e);
      
    }
  }
  
  @Override
  public List<Output> getOutputs(Long eventId) {
    List<Output> outputLst = Lists.newArrayList();
    Output output = null;
    String question = null;
    String answer = null;
    try{
      Connection conn = null;
      conn = CloudSQLConnectionManager.getConnection();
     
      String selectOutputsSql = "Select * from outputs where event_id =?";
      PreparedStatement statementSelectOutput = conn.prepareStatement(selectOutputsSql);
      
      statementSelectOutput.setLong(1, eventId);

      log.info(eventId + "sql qry is"+statementSelectOutput.toString() );
      ResultSet rs = statementSelectOutput.executeQuery();
      while(rs.next()){
        
        question = rs.getString("text");
        answer = rs.getString("answer");
        output = new Output(question, answer);
        outputLst.add(output);
      }    
    } catch (SQLException e) {
      log.info("sqlexception2"+e);
      
    }
    return outputLst;
  }

  @Override
  public String getPlainSql(SQLQuery sqlQuery) {
    loadColumnTableAssociationMap();
    String tableIndicator = SearchUtil.identifyTablesInvolved(eventsColumns, sqlQuery);
    
    //where group having order, limit
    StringBuffer sqlString = new StringBuffer("");
    
    sqlString.append("Select ");
  
    if(sqlQuery.getProjection()!=null){
      String proj = StringUtils.join(sqlQuery.getProjection(),",");
      sqlString.append(proj);
    }
    
    if (tableIndicator.equals("events")){
      sqlString.append( " from events ");
    } else {
      sqlString.append( " from events join outputs on events._id = outputs.event_Id ");   
    }

    if(sqlQuery.getCriteriaQuery()!=null){
      String[] repl = sqlQuery.getCriteriaValue();
      String x= sqlQuery.getCriteriaQuery();
      int i=0;
      while (x.contains("?")){
        x = x.replaceFirst("\\?", repl[i++]);
      }
          
      sqlString.append( " where ");
      sqlString.append(x);
    }
    
    if(sqlQuery.getGroupBy()!=null){
      sqlString.append( " group by ");
      sqlString.append(sqlQuery.getGroupBy());

      if(sqlQuery.getHaving()!=null){
        sqlString.append( " having ");
        sqlString.append(sqlQuery.getHaving());
      }
      
    }
    
    if(sqlQuery.getSortOrder()!=null){
      sqlString.append( " order by ");
      sqlString.append(sqlQuery.getSortOrder());
    }
//    
//    if(sqlQuery.getLimit()!=null){
//      sqlString.append( " limit ");
//      sqlString.append(sqlQuery.getLimit());
//    }
    
    System.out.println(sqlString);
    
    return sqlString.toString();
  }
  private Map<String, String> convertOutputListToMap(List<Output> outList){
    Map<String, String> outMap = Maps.newHashMap();
    for(Output out : outList){
      outMap.put(out.getName(), out.getValue());
    }
    return outMap;
  }

  @Override
  public List<EventDAO> getEvents(String query, String whereClause, String[] criValues) {
    List<EventDAO> evtList = Lists.newArrayList();
   
    EventDAO event = null;
   
    Map<Long, EventDAO> eventMap = null;
    try{
      Connection conn = null;
      conn = CloudSQLConnectionManager.getConnection();
      PreparedStatement statementSelectEvent = conn.prepareStatement(query);
      
      ResultSet rs = statementSelectEvent.executeQuery();
      //to maintain the insertion order
      eventMap = Maps.newLinkedHashMap();
      if(rs!=null){
        evtList = Lists.newArrayList();
        while(rs.next()){
          
          //no need to coalesce, we just add it to the list and send the collection to the client.
          event = createEvent(rs);
          EventDAO oldEvent = eventMap.get(event.getId()); 
          if(oldEvent == null){
            List<Output> outLst = getOutputs(event.getId());
            Map<String, String> outputMap = convertOutputListToMap(outLst);
            event.setWhat(outputMap);
            eventMap.put(event.getId(), event);
          }
//          rs.next();
        }
      }
    } catch (SQLException e) {
      log.info("sqlexception while selecting data from cloud sql"+e);
    }
    evtList = Lists.newArrayList(eventMap.values());
    return evtList;
  }
  
  private EventDAO createEvent(ResultSet rs){
    EventDAO e = new EventDAO();
    try{
      ResultSetMetaData rsmd = rs.getMetaData();
      for(int i=1; i<=rsmd.getColumnCount();i++){
        String tempColNameInRS = rsmd.getColumnName(i);
        String colUpper = tempColNameInRS.toUpperCase();
        Integer colIndex = eventsColumns.get(colUpper);
        if (colIndex!=null){
          switch (colIndex){
            case 1:
              e.setExperimentId(rs.getLong(i));
              break;
            case 2:
              e.setExperimentId(rs.getLong(i));
              break;
            case 3:
              e.setExperimentName(rs.getString(i));
              break;
            case 4:
              e.setExperimentVersion(rs.getInt(i));
              break;
            case 5:
              e.setScheduledTime(rs.getTimestamp(i));
              break;
            case 6:
              e.setResponseTime(rs.getTimestamp(i));
              break;
//            case 7:
////              (rs.getLong(i));
//              break;
            case 8:
              e.setExperimentGroupName(rs.getString(i));
              break;
            case 9:
              e.setActionTriggerId(rs.getLong(i));
              break;
            case 10:
              e.setActionTriggerSpecId(rs.getLong(i));
              break;
            case 11:
              e.setActionId(rs.getLong(i));
              break;
            case 12:
              e.setWho(rs.getString(i));
              break;
            case 13:
              e.setLat(rs.getString(i));
              break;
            case 14:
              e.setLon(rs.getString(i));
              break;
            case 15:
              e.setPaco_version(rs.getString(i));
              break; 
            case 16:
            case 17:
              e.setId(rs.getLong(i));
              break; 

//        String colLower = tempColNameInRS.toLowerCase();
//        EventTableColumns colIndex = eventsColumns.get(colLower);
//        if (colIndex!=null){
//          switch (colIndex){
//            case EXPERIMENT_ID:
//            case EXPERIMENT_SERVER_ID:
//              e.setExperimentId(rs.getLong(i));
//              break;
//            case EXPERIMENT_NAME:
//              e.setExperimentName(rs.getString(i));
//              break;
//            case EXPERIMENT_VERSION:
//              e.setExperimentVersion(rs.getInt(i));
//              break;
//            case SCHEDULE_TIME:
//              e.setScheduledTime(rs.getDate(i));
//              break;
//            case RESPONSE_TIME:
//              e.setResponseTime(rs.getDate(i));
//              break;
////            case 7:
//////              (rs.getLong(i));
////              break;
//            case GROUP_NAME:
//              e.setExperimentGroupName(rs.getString(i));
//              break;
//            case ACTION_TRIGGER_ID:
//              e.setActionTriggerId(rs.getLong(i));
//              break;
//            case ACTION_TRIGGER_SPEC_ID:
//              e.setActionTriggerSpecId(rs.getLong(i));
//              break;
//            case ACTION_ID:
//              e.setActionId(rs.getLong(i));
//              break;
//            case WHO:
//              e.setWho(rs.getString(i));
//              break;
//            case LAT:
//              e.setLat(rs.getString(i));
//              break;
//            case LON:
//              e.setLon(rs.getString(i));
//              break;
//            case PACO_VERSION:
//              e.setPaco_version(rs.getString(i));
//              break; 
//            case EVENTS_ID:
//            case _ID:
//              e.setId(rs.getLong(i));
//              break; 
          }
        }
      }
    }catch(SQLException ex){
      log.info("sql eception s"+ex);
    }
    return e;
  }
}
