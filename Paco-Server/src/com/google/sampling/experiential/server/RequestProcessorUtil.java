package com.google.sampling.experiential.server;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

public class RequestProcessorUtil {

  private static final Logger log = Logger.getLogger(RequestProcessorUtil.class.getName());

  public static String getBody(HttpServletRequest req) throws IOException{
    String postBodyString = null;
    try {
      postBodyString = org.apache.commons.io.IOUtils.toString(req.getInputStream(), "UTF-8");
    } catch (IOException e) {
      log.info("IO Exception reading post data stream: " + e.getMessage());
      throw e;
    }
    if (postBodyString.equals("")) {
      throw new IllegalArgumentException("Empty Post body");
    } 
    return postBodyString;
  }
  
  public static void logPacoClientVersion(HttpServletRequest req) {
    String pacoVersion = req.getHeader("paco.version");
    if (pacoVersion != null) {
      log.info("Paco version of request = " + pacoVersion);
    }
  }
  
  public static String getPacoProtocolVersionAsStr(HttpServletRequest req) {
    String pacoProtocol = req.getHeader("pacoProtocol");
    if (pacoProtocol == null) {
      pacoProtocol = req.getParameter("pacoProtocol");
    } 
    return pacoProtocol;
  }
  
  public static Float getPacoProtocolVersionAsFloat(HttpServletRequest req) {
    Float pacoProtocolVersion = null;
    try {
      String pacoProtocol = getPacoProtocolVersionAsStr(req);
      if (pacoProtocol != null) {
        pacoProtocolVersion = Float.parseFloat(pacoProtocol);
      }
    }  catch(NumberFormatException nfe) {
      log.warning("Paco protocol version is null" + nfe);
    }
    return pacoProtocolVersion;
  }
}
