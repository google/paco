package com.google.sampling.experiential.server;

import java.util.Map;

public class PacoUrl {
  private String scheme;
  private String base;
  private String context;
  private String qParameters;
  public String getScheme() {
    return scheme;
  }
  public String getBase() {
    return base;
  }
  public String getContext() {
    return context;
  }
  public String getqParameters() {
    return qParameters;
  }
  public PacoUrl(PacoUrlBuilder urlBuild) {
    this.scheme = urlBuild.scheme;
    this.base = urlBuild.base;
    this.context = urlBuild.context;
    this.qParameters = urlBuild.qParameters;
  }
  public String toString() {
    StringBuffer purl = new StringBuffer();
    purl.append(scheme);
    purl.append("://");
    purl.append(base);
    purl.append("/");
    purl.append(context);
    if(qParameters != null)  {
      purl.append("?");
      purl.append(qParameters);
    }
    return purl.toString();
  }
  
  public static class PacoUrlBuilder {
    private String scheme;
    private String base;
    private String context;
    private String qParameters;
    
    public PacoUrlBuilder(String base) {
      this.base = base;
    }
    public PacoUrlBuilder scheme(String scheme) {
      this.scheme = scheme;
      return this;
    }
    public PacoUrlBuilder context(String context) {
      this.context = context;
      return this;
    }
    public PacoUrlBuilder qParameters(Map<String, String> qMap) {
      StringBuffer sb = new StringBuffer();
      for (Map.Entry<String, String> entry : qMap.entrySet()) {
        sb.append(entry.getKey());
        sb.append("=");
        sb.append(entry.getValue());
        sb.append("&");
      }
      String param = sb.toString();
      if (qMap.size() > 1) {
        this.qParameters = param.substring(0, param.length()-1);
      } else {
        this.qParameters = param;
      }
      return this;
    }
    public PacoUrl build() { 
      return new PacoUrl(this);
    }
  }
}
