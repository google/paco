// Copyright 2009 Google Inc.
package com.google.appengine.demos.mandelbrot;

/**
 * {@code PixelSource} represents a physical image of a specific resolution.
 *
 */
public interface PixelSource {
  /**
   * Returns the width of the image, in pixels.
   */
  int getWidth();

  /**
   * Returns the height of the image, in pixels.
   */
  int getHeight();

  /**
   * Returns a color for the specified pixel, as produced by {@link ColorUtil}.
   */
  int getPixel(int x, int y);
}
