// Copyright 2009 Google Inc.
package com.google.appengine.demos.mandelbrot;

/**
 * {@code MandelbrotSource} is a {@link FractalSource} that draws the
 * Mandelbrot Series.
 *
 */
public class MandelbrotSource implements FractalSource {
  private static final int ESCAPE = 10000;
  private static final int LIMIT = 1000;

  private static final int PALETTE_STEP = 4;

  private static final double MB_XMAX = 1;
  private static final double MB_XMIN = -2;
  private static final double MB_YMAX = 1.5;
  private static final double MB_YMIN = -1.5;

  private static final double M_LN2 = Math.log(2.0);
  private static final double LOGESCAPE = Math.log(2 * Math.log(ESCAPE));

  private final Palette palette;

  public MandelbrotSource(Palette palette) {
    this.palette = palette;
  }

  public int getValue(double x, double y) {
    x = (((x - XMIN) / XRANGE * (MB_XMAX - MB_XMIN)) + MB_XMIN);
    y = (((y - XMIN) / YRANGE * (MB_YMAX - MB_YMIN)) + MB_YMIN);

    Complex z = new Complex(0, 0);
    for (int i = 0; i < LIMIT; i++) {
      z.modify(x, y);

      if (z.getValue() >= ESCAPE) {
        return getValueInternal(i, x, y, z);
      }
    }
    return 0;
  }

  private int getValueInternal(int i, double ca, double cb, Complex z) {
    for (int j = 0; j < 2; j++) {
      z.modify(ca, cb);
    }
    double modulus = Math.sqrt(z.getValue());
    double value = i + 2 + (LOGESCAPE - Math.log(Math.log(modulus))) / M_LN2;
    return palette.getColor(value * PALETTE_STEP);
  }

  private static class Complex {
    private double a;
    private double b;

    public Complex(double a, double b) {
      this.a = a;
      this.b = b;
    }

    public void modify(double x, double y) {
      double na = x + a * a - b * b;
      double nb = y + 2 * a * b;
      a = na;
      b = nb;
    }

    public double getValue() {
      return a * a + b * b;
    }
  }
}
