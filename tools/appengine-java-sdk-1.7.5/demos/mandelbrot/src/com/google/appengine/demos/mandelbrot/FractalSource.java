// Copyright 2009 Google Inc.
package com.google.appengine.demos.mandelbrot;

/**
 * A {@code FractalSource} represents a fractal image of arbitrary
 * size and resolution.
 *
 */
public interface FractalSource {
  /**
   * The left-most boundary of the fractal image.
   */
  final double XMIN = -1.0;

  /**
   * The right-most boundary of the fractal image.
   */
  final double XMAX = 1.0;

  /**
   * The bottom-most boundary of the fractal image.
   */
  final double YMIN = -1.0;

  /**
   * The top-most boundary of the fractal image.
   */
  final double YMAX = 1.0;

  /**
   * The horizontal size of the fractal image.
   */
  final double XRANGE = XMAX - XMIN;

  /**
   * The vertical size of the fractal image.
   */
  final double YRANGE = YMAX - YMIN;

  /**
   * Chooses a color for the specified point.
   *
   * @param x is a value between {@code XMIN} and {@code XMAX}
   * @param y is a value between {@code YMIN} and {@code YMAX}
   *
   * @return an integer reresenting a color as produced by {@link ColorUtil}.
   */
  int getValue(double x, double y);
}
