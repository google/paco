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

import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.model2.EventBaseColumns;
import com.pacoapp.paco.shared.model2.OutputBaseColumns;

public class CloudSQLDaoImpl implements CloudSQLDao{
  public static final Logger log = Logger.getLogger(CloudSQLDaoImpl.class.getName());
  private static  Map<String, Integer> eventsOutputColumns = null;

  private static void loadColumnTableAssociationMap(){
    if (eventsOutputColumns ==null){
      eventsOutputColumns = new HashMap<String,Integer>();
      eventsOutputColumns.put("EXPERIMENT_ID", 1);
      eventsOutputColumns.put("EXPERIMENT_NAME", 2);
      eventsOutputColumns.put("EXPERIMENT_VERSION", 3);
      eventsOutputColumns.put("SCHEDULE_TIME", 4);
      eventsOutputColumns.put("RESPONSE_TIME", 5);
      eventsOutputColumns.put("GROUP_NAME", 6);
      eventsOutputColumns.put("ACTION_TRIGGER_ID",7);
      eventsOutputColumns.put("ACTION_TRIGGER_SPEC_ID", 8);
      eventsOutputColumns.put("ACTION_ID", 9);
      eventsOutputColumns.put("WHO", 10);
      eventsOutputColumns.put("PACO_VERSION", 11);
      eventsOutputColumns.put("EVENTS._ID", 12);
      eventsOutputColumns.put("_ID", 13);
      eventsOutputColumns.put("TEXT", 14);
      eventsOutputColumns.put("ANSWER", 15);
    }
  }
 
  @Override
  public boolean insertEvent(Event event) throws SQLException{
    Connection conn = null;
    PreparedStatement statementCreateEvent = null;
    PreparedStatement statementCreateEventOutput = null;
    
    if (event == null){
      log.warning("nothing to insert");
      return false;
    }
    
    try{
      log.info("inserting event->"+ event.getId());
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      String insertEventSql = "INSERT INTO events ("
              +"_ID,"
              + EventBaseColumns.EXPERIMENT_ID +","
              + EventBaseColumns.EXPERIMENT_NAME +","
              + EventBaseColumns.EXPERIMENT_VERSION +","
              + EventBaseColumns.RESPONSE_TIME +","
              + EventBaseColumns.SCHEDULE_TIME +","
              + EventBaseColumns.GROUP_NAME +","
              + EventBaseColumns.ACTION_ID +","
              + EventBaseColumns.ACTION_TRIGGER_ID +","
              + EventBaseColumns.ACTION_TRIGGER_SPEC_ID +","
              + EventBaseColumns.WHO +","
//  Since when is a keyword in mysql, we should mark it with a back tick
              + "`"+ EventBaseColumns.WHEN  +"`)"
                      + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
      statementCreateEvent = conn.prepareStatement(insertEventSql);
      statementCreateEvent.setLong(1, event.getId()==null?0L:event.getId());
      statementCreateEvent.setString(2, event.getExperimentId()==null?"":event.getExperimentId());
      statementCreateEvent.setString(3, event.getExperimentName()==null?"":event.getExperimentName());
      statementCreateEvent.setInt(4, event.getExperimentVersion()==null?0:event.getExperimentVersion());
      statementCreateEvent.setTimestamp(5, new java.sql.Timestamp(event.getResponseTime()==null?new Date().getTime():event.getResponseTime().getTime()));
      statementCreateEvent.setTimestamp(6, new java.sql.Timestamp(event.getScheduledTime()==null?new Date().getTime():event.getScheduledTime().getTime()));
      statementCreateEvent.setString(7, event.getExperimentGroupName()==null?"":event.getExperimentGroupName());
      statementCreateEvent.setLong(8, event.getActionId()==null?0L:event.getActionId());
      statementCreateEvent.setLong(9, event.getActionTriggerId()==null?0L:event.getActionTriggerId());
      statementCreateEvent.setLong(10, event.getActionTriggerSpecId()==null?0L:event.getActionTriggerSpecId());
      statementCreateEvent.setString(11, event.getWho());
      statementCreateEvent.setTimestamp(12, new java.sql.Timestamp(event.getWhen()==null?new Date().getTime():event.getWhen().getTime()));
      
      statementCreateEvent.execute();
      
      String insertEventOutputsSql = "INSERT INTO outputs ("
              + OutputBaseColumns.EVENT_ID +","
              + OutputBaseColumns.NAME +","
              + OutputBaseColumns.ANSWER 
              + ") VALUES (?, ?, ?)";
      statementCreateEventOutput = conn.prepareStatement(insertEventOutputsSql);
      for(String key : event.getWhatKeys()){
        String whatAnswer = event.getWhatByKey(key);
        statementCreateEventOutput.setLong(1, event.getId()==null?0L:event.getId());
        statementCreateEventOutput.setString(2, key);
        statementCreateEventOutput.setString(3, whatAnswer==null?"":whatAnswer);
        statementCreateEventOutput.addBatch();
      } 
      statementCreateEventOutput.executeBatch();
      return true;
    } finally {
      try {
        if(statementCreateEventOutput!=null){
          statementCreateEventOutput.close();
        }
        if(statementCreateEvent!=null){
          statementCreateEvent.close();
        }
        if (conn != null) {
          //will return to the pool
          conn.close();
          CloudSQLConnectionManager.currentPoolStatus();
        }
      } catch (SQLException ex1) {
        log.warning("sqlexception while inserting event close conn"+ex1);
      }
    }
  }

  @Override
  public List<EventDAO> getEvents(String query) throws SQLException{
    List<EventDAO> evtList = Lists.newArrayList();
    EventDAO event = null;
    Connection conn = null;
    Map<Long, EventDAO> eventMap = null;
    PreparedStatement statementSelectEvent = null;
    ResultSet rs = null;
    try{
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementSelectEvent = conn.prepareStatement(query);
      DateTime stTime = new DateTime();
      rs = statementSelectEvent.executeQuery();
      DateTime dt = new DateTime();
      log.info("step 1 query "+ query+ "took" + new Long(new DateTime().getMillis()-stTime.getMillis()));
      //to maintain the insertion order
      eventMap = Maps.newLinkedHashMap();
    
      if(rs!=null){
        evtList = Lists.newArrayList();
        while(rs.next()){
          event = createEvent(rs);
          EventDAO oldEvent = eventMap.get(event.getId()); 
          if(oldEvent == null){
            eventMap.put(event.getId(), event);
          }else{
            //Will go through following when the query contains text in ('a','b','v') or answer in(....) 
            Map<String, String> oldOut = oldEvent.getWhat();
            Map<String, String> newOut = event.getWhat();
            //add all new output to the existing event in map
            oldOut.putAll(newOut); 
          }
        }
      }
      //TODO step 2 sometimes takes 4 times longer to execute than the query. Not sure why??
      log.info("step 2 proc took"+new Long(new DateTime().getMillis()-dt.getMillis()));
    } finally {
      try {
        if(statementSelectEvent!=null){
          statementSelectEvent.close();
        }
        if (conn != null) {
          conn.close();
        }
        CloudSQLConnectionManager.currentPoolStatus();
      } catch (SQLException ex1) {
        log.warning("sqlexception while inserting event close conn"+ex1);
      }
    }
    if(eventMap!=null){
      evtList = Lists.newArrayList(eventMap.values());
    }
    return evtList;
  }
  
  private EventDAO createEvent(ResultSet rs){
    loadColumnTableAssociationMap();
    EventDAO e = new EventDAO();
    //setting an empty map for possible outputs. Even if the qry does not ask for output fields, we send an empty output map
    e.setWhat(new HashMap<String,String>());
    try{
      ResultSetMetaData rsmd = rs.getMetaData();
      for(int i=1; i<=rsmd.getColumnCount();i++){
        String tempColNameInRS = rsmd.getColumnName(i);
        String colUpper = tempColNameInRS.toUpperCase();
        Integer colIndex = eventsOutputColumns.get(colUpper);
        if (colIndex!=null){
          switch (colIndex){
            case 1:
              e.setExperimentId(rs.getLong(i));
              break;
            case 2:
              e.setExperimentName(rs.getString(i));
              break;
            case 3:
              e.setExperimentVersion(rs.getInt(i));
              break;
            case 4:
              e.setScheduledTime(rs.getTimestamp(i));
              break;
            case 5:
              e.setResponseTime(rs.getTimestamp(i));
              break;
            case 6:
              e.setExperimentGroupName(rs.getString(i));
              break;
            case 7:
              e.setActionTriggerId(rs.getLong(i));
              break;
            case 8:
              e.setActionTriggerSpecId(rs.getLong(i));
              break;
            case 9:
              e.setActionId(rs.getLong(i));
              break;
            case 10:
              e.setWho(rs.getString(i));
              break;  
            case 11:
              e.setPaco_version(rs.getString(i));
              break; 
            case 12:
            case 13:
              e.setId(rs.getLong(i));
              break; 
            case 14:
              Map<String,String> hm = e.getWhat();
              hm.put("TEXT", rs.getString(i));
              break; 
            case 15:
              Map<String,String> hma = e.getWhat();
              hma.put("ANSWER", rs.getString(i));
              break; 
          }
        }
      }
    }catch(SQLException ex){
      log.warning("sql eception "+ex);
    }
    return e;
  }

  @Override
  public String createTables() throws SQLException{
    String retString = null;
    Connection conn = null;
    PreparedStatement statementCreateEvent = null;
    PreparedStatement statementCreateOutput = null;
    try{
     
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      //TODO Sub Partition size for the experiment hash bucket
      final String createEventsTableSql = "CREATE TABLE `events` ("+
                  
      "`_id` bigint(20) NOT NULL,"+
      "`experiment_id` bigint(20) NOT NULL,"+
      "`experiment_server_id` bigint(20) DEFAULT NULL,"+
      "`experiment_name` varchar(45) DEFAULT NULL,"+
      "`experiment_version` int(11) DEFAULT NULL,"+
      "`schedule_time` datetime DEFAULT NULL,"+
      "`response_time` datetime DEFAULT NULL,"+
      "`group_name` varchar(45) DEFAULT NULL,"+
      "`action_id` bigint(20) DEFAULT NULL,"+
      "`action_trigger_id` bigint(20) DEFAULT NULL,"+
      "`action_trigger_spec_id` bigint(20) DEFAULT NULL,"+
      "`who` varchar(45) DEFAULT NULL,"+
      "`paco_version` varchar(45) DEFAULT NULL,"+
      "`when` datetime DEFAULT NULL,"+
      "`archive_flag` tinyint(4) NOT NULL DEFAULT '0',"+
      "PRIMARY KEY (`_id`),"+
      "KEY `when_index` (`when`),"+
      "KEY `exp_id_resp_time_index` (`experiment_id`,`response_time`),"+
      "KEY `exp_id_when_index` (`experiment_id`,`when`),"+
      "KEY `exp_id_who_when_index` (`experiment_id`,`who`,`when`)"+
      ") ENGINE=InnoDB DEFAULT CHARSET=latin1";

      final String createOutputsTableSql = "CREATE TABLE `outputs` ("+
          "`event_id` bigint(20) NOT NULL,"+
          "`text` varchar(45) NOT NULL,"+
          "`answer` varchar(45) DEFAULT NULL,"+
          "`archive_flag` tinyint(4) NOT NULL DEFAULT '0',"+
          "PRIMARY KEY (`event_id`,`text`),"+
          "KEY `event_id_index` (`event_id`),"+
          "KEY `text_index` (`text`)"+
        ") ENGINE=InnoDB DEFAULT CHARSET=latin1";
               
      statementCreateEvent = conn.prepareStatement(createEventsTableSql);
     
      statementCreateEvent.execute();
      log.info("created events");
      //TODO better handling
      retString = "created events table. ";
      statementCreateOutput = conn.prepareStatement(createOutputsTableSql);
      
      statementCreateOutput.execute();
      log.info("created outputs");
      //TODO better handling
      retString = retString + "Created outputs table";
      
    
    } finally {
      try {
     
        if(statementCreateEvent!=null){
          statementCreateEvent.close();
        }
        if(statementCreateOutput!=null){
          statementCreateOutput.close();
        }
        if (conn != null) {
          conn.close();
        }

      } catch (SQLException ex1) {
        log.warning("sqlexception while inserting event close conn"+ex1);
      }
    }
    return retString;
  }
}
