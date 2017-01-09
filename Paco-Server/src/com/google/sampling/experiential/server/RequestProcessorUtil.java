package com.google.sampling.experiential.server;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.SerializationUtils;
import org.mortbay.log.Log;

import com.google.sampling.experiential.shared.EventDAO;

public class RequestProcessorUtil {
  public static EventDAO getEventDAO(HttpServletRequest req){
    ServletInputStream sis = null;
    EventDAO event = null;
    
    try {
      sis = req.getInputStream();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    event = (EventDAO)SerializationUtils.deserialize(sis);
    return event;
  }
  
  public static String getBody(HttpServletRequest req){
    StringBuilder buffer = new StringBuilder();
    String reqBody = null;
    try{
      BufferedReader reader = req.getReader();
      String line;
      while ((line = reader.readLine()) != null) {
          buffer.append(line);
      }
      reqBody = buffer.toString();
    }catch(Exception e){
      Log.info("while retrieving req body"+e);
    }
    return reqBody;
  }
  

}
