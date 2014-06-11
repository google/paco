package com.google.android.apps.paco;

import java.util.List;

public interface SpeechRecognitionListener {

  public void speechRetrieved(List<String> guesses);

}
