// Copyright 2009 Google Inc.
package com.google.appengine.demos.mandelbrot;

/**
 * Creates integer values corresponding to the specified color.
 *
 */
public class ColorUtil {
  /**
   * Extracts the red component from {@code color}.
   */
  public static int red(int color) {
    return color & 0xff;
  }

  /**
   * Extracts the green component from {@code color}.
   */
  public static int green(int color) {
    return (color >> 8) & 0xff;
  }

  /**
   * Extracts the blue component from {@code color}.
   */
  public static int blue(int color) {
    return (color >> 16) & 0xff;
  }

  /**
   * Creates a color that corresponds to the specified values.
   *
   * @param red is an integer between 0 and 255
   * @param green is an integer between 0 and 255
   * @param blue is an integer between 0 and 255
   */
  public static int create(int red, int green, int blue) {
    int value = (red & 0xff);
    value |= (green & 0xff) << 8;
    value |= (blue & 0xff) << 16;
    return value;
  }
}
