package com.google.android.apps.paco.js.bridge;

import android.webkit.JavascriptInterface;

import com.google.android.apps.paco.ExperimentExecutorCustomRendering;

public class JavascriptPhotoService {

  /**
   *
   */
  private final ExperimentExecutorCustomRendering innerType;

  /**
   * @param experimentExecutorCustomRendering
   */
  public JavascriptPhotoService(ExperimentExecutorCustomRendering experimentExecutorCustomRendering) {
    innerType = experimentExecutorCustomRendering;
  }

  @JavascriptInterface
  public void launch() {
    innerType.renderCameraOrGalleryChooser();
  }
}