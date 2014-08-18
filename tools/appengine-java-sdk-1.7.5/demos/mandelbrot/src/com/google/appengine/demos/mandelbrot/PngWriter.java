// Copyright 2009 Google Inc.
package com.google.appengine.demos.mandelbrot;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * {@code PngWriter} can write compressed PNG images from a {@link
 * PixelSource}.
 *
 * <p>Note: this class generates PNGs directly, without the use of any
 * third-party libraries, to avoid any external dependencies.  Google
 * App Engine applications are not allowed to use Java 2D, but the
 * usage of a pure-Java image libary like Sanselan
 * (http://incubator.apache.org/sanselan/site/index.html) might be
 * preferable in a production application.
 *
 */
public class PngWriter implements ImageWriter {
  private static final byte[] HEADER = new byte[] {
    -119, 80, 78, 71, 13, 10, 26, 10,
  };

  private static final byte[] IDAT = stringToBytes("IDAT");
  private static final byte[] IEND = stringToBytes("IEND");
  private static final byte[] IHDR = stringToBytes("IHDR");

  private static final int BYTES_PER_PIXEL = 3;
  private static final String CONTENT_TYPE = "image/png";

  public String getContentType() {
    return CONTENT_TYPE;
  }

  public byte[] generateImage(PixelSource source) throws IOException {
    int width = source.getWidth();
    int height = source.getHeight();

    CRC32 crc = new CRC32();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(HEADER);

    writeUnsignedInt(out, 13);
    ByteArrayOutputStream header = new ByteArrayOutputStream();
    header.write(IHDR);
    writeUnsignedInt(header, width);
    writeUnsignedInt(header, height);
    header.write(8);
    header.write(2);
    header.write(0);
    header.write(0);
    header.write(0);
    crc.reset();
    crc.update(header.toByteArray());
    out.write(header.toByteArray());
    writeUnsignedInt(out, crc.getValue());

    Deflater deflator = new Deflater(5);
    ByteArrayOutputStream zippedByteStream = new ByteArrayOutputStream(1024);
    BufferedOutputStream zipStream = new BufferedOutputStream(
        new DeflaterOutputStream(zippedByteStream, deflator));

    byte[] scanLines = new byte[width * height * BYTES_PER_PIXEL + height];
    for (int i = 0; i < width * height; i++) {
      if (i % width == 0) {
        zipStream.write(0);
      }

      int pixel = source.getPixel(i % width, i / width);
      zipStream.write((pixel >> 16) & 0xff);
      zipStream.write((pixel >> 8) & 0xff);
      zipStream.write(pixel & 0xff);
    }

    zipStream.close();

    byte[] zippedBytes = zippedByteStream.toByteArray();
    writeUnsignedInt(out, zippedBytes.length);
    out.write(IDAT);
    crc.reset();
    crc.update(IDAT);
    out.write(zippedBytes);
    crc.update(zippedBytes);
    writeUnsignedInt(out, crc.getValue());
    deflator.finish();

    writeUnsignedInt(out, 0);
    out.write(IEND);
    crc.reset();
    crc.update(IEND);
    writeUnsignedInt(out, crc.getValue());

    return out.toByteArray();
  }

  private static byte[] stringToBytes(String value) {
    try {
      return value.getBytes("US-ASCII");
    } catch (UnsupportedEncodingException ex) {
      throw new RuntimeException(ex);
    }
  }

  private void writeUnsignedInt(OutputStream out, long value) throws IOException {
    out.write((int) ((value >> 24) & 0xff));
    out.write((int) ((value >> 16) & 0xff));
    out.write((int) ((value >> 8) & 0xff));
    out.write((int) (value & 0xff));
  }
}
