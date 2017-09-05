package com.google.sampling.experiential.server;

import java.util.Set;

import com.google.appengine.api.modules.ModulesService;
import com.google.appengine.api.modules.ModulesServiceFactory;

public class PacoModule {
  String name;
  String address;
  ModulesService modulesApi = ModulesServiceFactory.getModulesService();
  private static final String DOT = "-dot-";
  
  public PacoModule(String moduleName, String serverName) {
    this.name = moduleName;
    Set<String> allModules = modulesApi.getModules();
    String version = null;
    String serverNameInParts[] = null;
    String finalUrl = null;
    for (String eachModuleName : allModules) {
      if (eachModuleName != null && eachModuleName.equalsIgnoreCase(name)) { 
        // Eg: If server name has -dot- in its request url
        if (serverName != null && serverName.contains(DOT)) {
          serverNameInParts = serverName.split(DOT);
          // Eg: 221-dot-quantifiedself.appspot.com
          if (serverNameInParts != null && serverNameInParts.length == 2) {
            finalUrl = serverNameInParts[0] + DOT + name + DOT + serverNameInParts[1];
          } else if(serverNameInParts.length == 3) {
            //Eg: 221-dot-default-dot-quantifiedself.appspot.com
            serverNameInParts = serverName.split(DOT);
            finalUrl =  serverNameInParts[0] + DOT + name + DOT + serverNameInParts[2];
          } else {
            finalUrl = "";
          }
        } else {
          //Eg: quantifiedself.com
          version = modulesApi.getCurrentVersion();
          finalUrl = version + DOT + name + DOT + serverName;
        }
        break;
      } else {
        finalUrl = "";
      }
    }
    this.address = finalUrl;
  }
  
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getAddress() {
    return address;
  }
  public void setAddress(String address) {
    this.address = address;
  }
  
}
