package com.google.sampling.experiential.server;

public class AllPassWhitelist extends Whitelist {

  @Override
  public boolean allowed(String email) {
    return true;
  }

}
