package com.pacoapp.paco.shared.model2;

public interface MinimumBufferable {

  Integer DEFAULT_MIN_BUFFER = 59;

  void setMinimumBuffer(Integer minBufferMinutes);

  Integer getMinimumBuffer();


}
