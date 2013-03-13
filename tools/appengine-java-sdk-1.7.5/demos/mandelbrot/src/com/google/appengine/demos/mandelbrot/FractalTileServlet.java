// Copyright 2009 Google Inc.
package com.google.appengine.demos.mandelbrot;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;

/**
 * {@code FractalTileServlet} generates and serves image tiles that
 * make up a fractal image of arbitrary size and resolution.
 *
 * <p>This servlet expects to receive a tile level as well as x and y
 * coordinates for the requested tile via {@link
 * HttpServletRequest#getPathInfo()}.  For example, if this servlet
 * were mapped to the URI pattern {@code /tiles/*"}, it would expect
 * to receive requests such as {@code /tiles/1/0_0.png}, which would
 * correspond to a single tile that encompassed all of the image at
 * level 1.  At higher levels, more tiles would be possible
 * (e.g. {@code /tiles/10/9_7.png}.
 *
 * <p>Generated tiles will be stored in {@link MemcacheService} in an
 * attempt to serve commonly-viewed tiles more quickly.  However, note
 * that the amount of imagery that this servlet can serve is
 * practically limitless, so the percentage of the image that can be
 * stored in the cache at any given time is extraordinarily low.
 *
 */
public class FractalTileServlet extends HttpServlet {
  private static final Logger logger = Logger.getLogger(FractalTileServlet.class.getName());

  /**
   * Expect requests of the form {@code level/x_y.ext}, where {@code
   * level}, {@code x}, and {@code y} are integers and {@code ext} is
   * a file extension.
   */
  private static final Pattern PATH_INFO_PATTERN = Pattern.compile("^/(\\d+)/(\\d+)_(\\d+)\\..*$");

  /**
   * Maximum amount of time that clients are allowed to cache tiles.
   * This is just an arbitrary amount of time, so we'll just use one
   * year.
   */
  private static final long MAX_AGE = 365 * 24 * 60 * 60;

  /**
   * Store a single {@link Cache} reference for use across requests.
   */
  private Cache cache;

  /**
   * This {@link TileFactory} will be used to generate {@link
   * PixelSource} objects for each request.
   */
  private TileFactory tileFactory;

  /**
   * This {@link ImageWriter} will be used to generate the byte stream
   * that we return to the client.
   */
  private ImageWriter imageWriter;

  /**
   * Initialize the above fields.
   */
  @Override
  public void init() throws ServletException {
    cache = createCache();
    tileFactory = new TileFactory(new MandelbrotSource(new Palette()));
    imageWriter = new PngWriter();
  }

  /**
   * Look up the request in the distributed cache and, if found, serve
   * the image directly.  If it is not found, call {@ink
   * generateImage} and insert the results into the cache.
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String cacheKey = getCacheKey(request);

    logger.info("Retrieving " + cacheKey + " from cache...");
    long start = System.nanoTime();
    byte[] image = (byte[]) cache.get(cacheKey);
    if (image == null) {
      logger.info("Not found, generating...");
      image = generateImage(request);
      logger.info("Generated.  Adding to cache...");
      cache.put(cacheKey, image);
      logger.info("Added.");
    } else {
      logger.info("Found.");
    }

    response.setContentType(imageWriter.getContentType());
    response.setDateHeader("Expires", System.currentTimeMillis() + MAX_AGE * 1000);
    response.setHeader("Cache-Control", "max-age=" + MAX_AGE + ", public");
    response.getOutputStream().write(image);
  }

  /**
   * Extract the tile level and coodinates from {@code request} and
   * generate an image that corresponds to the requested tile.
   */
  private byte[] generateImage(HttpServletRequest request)
      throws IOException, ServletException {
    Matcher match = PATH_INFO_PATTERN.matcher(request.getPathInfo());
    if (!match.matches()) {
      throw new ServletException("Could not match: " + request.getPathInfo());
    }
    int level = Integer.parseInt(match.group(1));
    int tileX = Integer.parseInt(match.group(2));
    int tileY = Integer.parseInt(match.group(3));

    return imageWriter.generateImage(tileFactory.createTile(level, tileX, tileY));
  }

  /**
   * Extract a key from {@code request} suitable for use in a cache.
   * This includes not only the request URI, but also the version
   * identifier of the current application.
   */
  private String getCacheKey(HttpServletRequest request) {
    return request.getRequestURI();
  }

  /**
   * Create a {@link Cache} from the default {@code net.sf.jsr107cache}
   * implementation with no custom properties.
   */
  private Cache createCache() throws ServletException {
    Map<String, Object> props = Collections.emptyMap();
    try {
      return CacheManager.getInstance().getCacheFactory().createCache(props);
    } catch (CacheException ex) {
      throw new ServletException("Could not initialize cache:", ex);
    }
  }
}
