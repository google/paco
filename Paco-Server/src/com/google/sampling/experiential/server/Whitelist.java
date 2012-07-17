package com.google.sampling.experiential.server;

import java.util.ArrayList;

public class Whitelist {

  
  private ArrayList<String> allowedEmails;

  public Whitelist() {
    allowedEmails = new ArrayList<String>();
    
//    allowedEmails.add("");

  }

  public boolean allowed(String email) {
    return allowedEmails.contains(email.toLowerCase());
  }
}
