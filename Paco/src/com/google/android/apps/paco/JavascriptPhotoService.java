package com.google.android.apps.paco;

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

  public void launch() {
    innerType.renderCameraOrGalleryChooser();
  }
}