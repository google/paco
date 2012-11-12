package com.google.sampling.experiential.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("whitelistservice")
public interface WhitelistService extends RemoteService  {

  List<String> getWhitelist();
  
  void addUser(String email);

}
