package com.google.sampling.experiential.datastore;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.google.appengine.api.datastore.Entity;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.PhotoBlob;
import com.google.sampling.experiential.model.What;

public class EventEntityConverter {

  public static Event convertEntityToEvent(Entity entity) {
    Integer experimentVersion  =null;
    Long id = (Long) entity.getProperty("id");
    String who = (String) entity.getProperty("who");
    String lat = (String) entity.getProperty("lat");
    String lon = (String) entity.getProperty("lon");
    Date when = (Date) entity.getProperty("when");
    String appId = (String) entity.getProperty("appId");
    String pacoVersion = (String) entity.getProperty("pacoVersion");
    String experimentName = (String) entity.getProperty("experimentName");
    String experimentId = (String) entity.getProperty("experimentId");
    if(entity.getProperty("experimentVersion")!=null) {
      experimentVersion = (int)(long)entity.getProperty("experimentVersion");
    }
    Date scheduledTime = (Date) entity.getProperty("scheduledTime");
    Date responseTime = (Date) entity.getProperty("responseTime");
    boolean shared = (Boolean) entity.getProperty("shared");
    String timeZone = (String) entity.getProperty("timeZone");
    String groupName = (String)entity.getProperty("experimentGroupName");
    Long actionId = (Long) entity.getProperty("actionId");
    Long actionTriggerId = (Long) entity.getProperty("actionTriggerId");
    Long actionTriggerSpecId = (Long) entity.getProperty("actionTriggerSpecId");

    Set<What> whats = Sets.newHashSet();
    List<String> keysList = (List<String>) entity.getProperty("keysList");
    List<String> valuesList = (List<String>) entity.getProperty("valuesList");
    
    if (keysList != null && valuesList != null) {
      for (int i = 0; i < keysList.size(); i++) {
        whats.add(new What(keysList.get(i), valuesList.get(i)));
      }
    }
    
    //List<PhotoBlob> blobs = (List<PhotoBlob>) entity.getProperty("blobs");
    List<PhotoBlob> blobs = Lists.newArrayList();

    Event event = new Event(who, lat, lon, when, appId, pacoVersion, whats, shared, experimentId, experimentName, experimentVersion,
                           responseTime, scheduledTime, blobs, timeZone, groupName, actionTriggerId, actionTriggerSpecId, actionId);
        
    event.setId(id);

    return event;
  }


}
