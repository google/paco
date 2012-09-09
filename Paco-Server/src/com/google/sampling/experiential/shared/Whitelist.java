package com.google.sampling.experiential.shared;

import java.util.ArrayList;

public class Whitelist {

  
  private ArrayList<String> allowedEmails;

  public Whitelist() {
    allowedEmails = new ArrayList<String>();
  }

  public boolean allowed(String email) {
    return allowedEmails.contains(email.toLowerCase());
  }
}
