// Copyright 2009 Google Inc.
package com.google.appengine.demos.mandelbrot;

import java.io.IOException;

/**
 * An {@code ImageWriter} is capable of converting a {@link}
 * PixelSource} to a specific stream of bytes in some specific image
 * format.
 *
 */
public interface ImageWriter {
  /**
   * Returns the content type for the image format that is used.
   */
  String getContentType();

  /**
   * Generate the image produced by {@code source}.
   */
  byte[] generateImage(PixelSource source) throws IOException;
}
