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

import android.content.Intent;
import com.pacoapp.paco.R;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

/**
 * The response interface that is the result of executing a {@link Request}.
 *
 */
public interface Response {
  /**
   * Returns the status of a response, as defined in {@link Status}.
   */
  Status getStatus();

  /**
   * Returns the HTTP response code if this {@link Response}'s status is
   * {@link Status#HTTP_OK} or {@link Status#HTTP_OTHER}.
   *
   * @return HTTP response code or -1 if status is not set to
   *         {@link Status#HTTP_OK} or {@link Status#HTTP_OTHER}.
   */
  int getHttpCode();

  /**
   * Returns extra message for exceptional status.
   *
   * @return Status string, <code>null</code> if none is set.
   */
  String getStatusString();

  /**
   * Returns the required action when request could not be successfully
   * executed, e.g. when authentication is needed and user had not entered
   * username/password.
   *
   * <p>
   * This intent can be used to either start an activity or wrapped inside a
   * pending intent for notification.
   */
  Intent getRequiredAction();

  /**
   * Returns the content as {@link String} object.
   *
   * @throws UnsupportedCharsetException if the bytes cannot be decoded to a
   *         {@link String} using default encoding.
   */
  String getContentAsString() throws UnsupportedCharsetException;

  /**
   * Returns the content as {@link String} object.
   *
   * @param charset The charset codec used to decode bytes into string.
   * @throws UnsupportedCharsetException if the bytes cannot be decoded to a
   *         {@link String} using <code>charset</code> encoding.
   */
  String getContentAsString(Charset charset) throws UnsupportedCharsetException;

  /**
   * Returns the content as a byte array.
   */
  byte[] getContentAsBytes();

  /**
   * Returns the content as an {@link InputStream}.
   */
  InputStream getContentAsStream();

  /**
   * Gets the header attributes associated with a given name.
   *
   * @param name The name of the header.
   * @return The header values set in this response for the given <code>name
   *         </code>, <code>null</code> if no value associated with the <code>
   * name        </code> is found.
   */
  String[] getHeaders(String name);

  /**
   * Gets the first header attribute associated with a given name.
   *
   * @param name The name of the header.
   * @return The header value set in this response for the given <code>name
   *         </code>, <code>null</code> if no value associated with the <code>
   * name        </code> is found.
   */
  String getFirstHeader(String name);

  /**
   * Represents the status of a {@link Request} execution.
   */
  public static enum Status {
    /**
     * Any socket error, timeout or couldn't reach the host, check
     * {@link Response#getStatusString()} for more details.
     */
    NETWORK_ERROR,
    /**
     * Got an ok response (in the 200 - 299 range) from http server, check
     * {@link Response#getHttpCode()} for more details.
     */
    HTTP_OK,
    /**
     * Got a response (other than OK) from http server, check
     * {@link Response#getHttpCode()} for more details.
     */
    HTTP_OTHER,
    /**
     * Failed to fetch the page because it requires authentication and the
     * credential is missing or incorrect. In the case where the 
     * {@code AccountManager}'s credential is stale and the user is required
     * to reenter the password, the intent that brings up the UI can be
     * obtained by calling {@link Response#getRequiredAction()}.
     */
    AUTH_FAILED,
    /**
     * Failed authentication but cannot find user credential from
     * AccountManager, requires user to create a new account, the action intent
     * can be obtained by calling {@link Response#getRequiredAction()}.
     */
    ACCOUNT_REQUIRED
  }
}
