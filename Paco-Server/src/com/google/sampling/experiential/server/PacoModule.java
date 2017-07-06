package com.google.sampling.experiential.server;

import com.google.appengine.api.modules.ModulesService;
import com.google.appengine.api.modules.ModulesServiceFactory;

public class PacoModule {
  String name;
  String address;
  ModulesService modulesApi = ModulesServiceFactory.getModulesService();
  
  public PacoModule(String name) {
    this.name = name;
    this.address = modulesApi.getVersionHostname(name, modulesApi.getDefaultVersion(name));
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
