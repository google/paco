package com.pacoapp.paco.ui;

import java.util.List;

public interface SpeechRecognitionListener {

  public void speechRetrieved(List<String> guesses);

}
