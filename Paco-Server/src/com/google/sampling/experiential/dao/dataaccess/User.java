package com.google.sampling.experiential.dao.dataaccess;

import com.google.sampling.experiential.server.PacoId;

public class User {
  private String who;
  private PacoId userId;
  public String getWho() {
    return who;
  }
  public void setWho(String who) {
    this.who = who;
  }
  public PacoId getUserId() {
    return userId;
  }
  public void setUserId(PacoId userId) {
    this.userId = userId;
  }
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((userId == null) ? 0 : userId.hashCode());
    result = prime * result + ((who == null) ? 0 : who.hashCode());
    return result;
  }
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    User other = (User) obj;
    if (userId == null) {
      if (other.userId != null)
        return false;
    } else if (!userId.equals(other.userId))
      return false;
    if (who == null) {
      if (other.who != null)
        return false;
    } else if (!who.equals(other.who))
      return false;
    return true;
  }

}
