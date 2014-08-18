/*
* Copyright 2011 Google Inc. All Rights Reserved.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance  with the License.  
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package com.google.corp.productivity.specialprojects.android.comm;

import com.pacoapp.paco.R;
import java.nio.charset.UnsupportedCharsetException;

/**
 * The request interface to fetch data from a url or to post data to a server.
 *
 */
public interface Request {

  /**
   * Sets the url to perform action againts.
   *
   * @param url
   * @return this {@link Request} object.
   * @throws IllegalArgumentException if the url is malformed.
   */
  Request setUrl(String url) throws IllegalArgumentException;
  
  /**
   * Returns the current url (as a string), or null if not set.
   */
  
  String getUrl();

  /**
   * Adds a header to the request.
   *
   * @param headerKey Key of the header.
   * @param headerValue Value of the header.
   * @return this {@link Request} object.
   */
  Request addHeader(String headerKey, String headerValue);
  
  /**
   * Gets value of a header given a key.
   * 
   * @param headerKey Key of the header.
   * @return Value of the header.
   */
  String getHeaderValue(String headerKey);
  
  /**
   * Remove the first header from this request.  Only headers
   * created with addHeader() can be removed.
   * @param headerKey   key of the header
   * @return            The value of the header removed, or null
   */
  String removeFirstHeader(String headerKey);
  
  /**
   * Sets the data to post. The string data will be converted into entities suitable
   * for transportation using HTTP's default ISO-8859-1 encoding.
   *
   * @param postData Data to post.
   * @return this {@link Request} object.
   */
  Request setPostData(String postData);

  /**
   * Sets the data to post.
   *
   * @param postData Data to post.
   * @param contentType The content type of the data.
   * @return this {@link Request} object.
   * @throws UnsupportedCharsetException if the post data cannot be encoded
   *         using the charset specified in <code>contentType</code>.
   */
  Request setPostData(String postData, String contentType) throws UnsupportedCharsetException;

  /**
   * Sets the data to post.
   *
   * @param postData Data to post.
   * @param contentType The content type of the data.
   * @return this {@link Request} object.
   */
  Request setPostData(byte[] postData, String contentType);

  /**
   * Executes the request.
   *
   * @return A {@link Response} for this request.
   * @throws IllegalStateException if the minimum required fields are not set.
   */
  Response execute() throws IllegalStateException;
}
