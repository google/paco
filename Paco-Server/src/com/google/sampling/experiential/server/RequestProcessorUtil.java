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
  

}
