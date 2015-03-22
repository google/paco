package com.google.android.apps.paco;

import android.webkit.JavascriptInterface;

public class JavascriptPhotoService {

  /**
   *
   */
  private final ExperimentExecutorCustomRendering innerType;

  /**
   * @param experimentExecutorCustomRendering
   */
  JavascriptPhotoService(ExperimentExecutorCustomRendering experimentExecutorCustomRendering) {
    innerType = experimentExecutorCustomRendering;
  }

  @JavascriptInterface
  public void launch() {
    innerType.renderCameraOrGalleryChooser();
  }
}