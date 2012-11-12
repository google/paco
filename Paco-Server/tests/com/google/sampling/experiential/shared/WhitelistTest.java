package com.google.sampling.experiential.shared;

import static org.junit.Assert.*;

import org.junit.Test;

public class WhitelistTest {

  @Test
  public void testBlank() {
    assertFalse(new Whitelist().allowed(null));
  }
  
  @Test
  public void testEmpty() {
    assertFalse(new Whitelist().allowed(""));
  }

  @Test
  public void testNameNoDomain() {
    assertFalse(new Whitelist().allowed("bob@"));
  }
  
  @Test
  public void testGooglerDomain() {
    assertFalse(new Whitelist().allowed("google.com"));
  }

  @Test
  public void testGoogler() {
    assert(new Whitelist().allowed("bob@google.com"));
  }
  
  @Test
  public void testGooglerFakeDouble() {
    assertFalse(new Whitelist().allowed("bob@evil.com@google.com"));
  }

  @Test
  public void testGooglerFakeBlank() {    
    assertFalse(new Whitelist().allowed("@google.com"));
  }


}
