package com.google.sampling.experiential.shared;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.sampling.experiential.server.DBWhitelist;
import com.google.sampling.experiential.server.Whitelist;

public class WhitelistTest {

  @Test
  public void testBlank() {
    assertFalse(new DBWhitelist().allowed(null));
  }
  
  @Test
  public void testEmpty() {
    assertFalse(new DBWhitelist().allowed(""));
  }

  @Test
  public void testNameNoDomain() {
    assertFalse(new DBWhitelist().allowed("bob@"));
  }
  
  @Test
  public void testGooglerDomain() {
    assertFalse(new DBWhitelist().allowed("google.com"));
  }

  @Test
  public void testGoogler() {
    assert(new DBWhitelist().allowed("bob@google.com"));
  }
  
  @Test
  public void testGooglerFakeDouble() {
    assertFalse(new DBWhitelist().allowed("bob@fake.com@google.com"));
  }

  @Test
  public void testGooglerFakeBlank() {    
    assertFalse(new DBWhitelist().allowed("@google.com"));
  }


}
