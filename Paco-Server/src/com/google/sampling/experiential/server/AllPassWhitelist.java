package com.google.sampling.experiential.server;

import com.google.sampling.experiential.shared.Whitelist;

public class AllPassWhitelist extends Whitelist {

  @Override
  public boolean allowed(String email) {
    return true;
  }

}
