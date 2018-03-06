package com.google.sampling.experiential.server;

public class PacoUser {
  Long id;
  int anonId;
  Character type;
  String email;
  public PacoUser() {
    
  }
  
  public PacoUser(Long id, int anonId, Character type, String email) {
    this.id =id;
    this.anonId = anonId;
    this.type = type;
    this.email = email;
  }
  
  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }
  public int getAnonId() {
    return anonId;
  }
  public void setAnonId(int anonId) {
    this.anonId = anonId;
  }
  public Character getType() {
    return type;
  }
  public void setType(Character type) {
    this.type = type;
  }
  public String getEmail() {
    return email;
  }
  public void setEmail(String email) {
    this.email = email;
  }
}
