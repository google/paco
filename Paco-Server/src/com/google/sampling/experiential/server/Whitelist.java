package com.google.sampling.experiential.server;

import java.util.ArrayList;

public class Whitelist {

  
  private ArrayList<String> allowedEmails;

  public Whitelist() {
    allowedEmails = new ArrayList<String>();
 }

  public boolean allowed(String email) {
    if (email == null || email.isEmpty()) {
      return false;
    }

    return allowedEmails.contains(email.toLowerCase());
  }
}
