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

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Implementation of the {@link Response} interface.
 *
 */
class ResponseImpl implements Response {
  private static final int BUFFER_SIZE = 2048;

  private final Status status;
  private String statusString;
  private byte[] content;
  private String contentType;
  private Intent action;
  private Map<String, ArrayList<String>> headers;
  private int httpCode = -1;

  protected ResponseImpl(Status status) {
    this.status = status;
  }

  protected ResponseImpl(HttpResponse response) {
    int statusCode = response.getStatusLine().getStatusCode();
    httpCode = statusCode;
    status = (statusCode >= 200 && statusCode < 300)
        ? handleHttpOk(response)
        : handleHttpOther(response);
    initHeaders(response);
  }
  
  @Override
  public byte[] getContentAsBytes() {
    return content;
  }

  @Override
  public InputStream getContentAsStream() {
    return new ByteArrayInputStream(content);
  }

  @Override
  public String getContentAsString() throws UnsupportedCharsetException {
    String charset = contentType != null
        ? UrlContentManager.getCharSetEncoding(contentType) 
        : Constants.DEFAULT_CHARSET_NAME;
    return getContentAsString(Charset.forName(charset));
  }

  @Override
  public String getContentAsString(Charset charset) throws UnsupportedCharsetException {
    if (content != null) {
      try {
        return new String(content, charset.name());
      } catch (UnsupportedEncodingException e) {
        UnsupportedCharsetException exception = new UnsupportedCharsetException(charset.name());
        exception.initCause(e);
        throw exception;
      }
    }
    return null;
  }

  @Override
  public int getHttpCode() {
    return httpCode;
  }

  @Override
  public Intent getRequiredAction() {
    return action;
  }

  @Override
  public Status getStatus() {
    return status;
  }

  @Override
  public String[] getHeaders(String name) {
    if (headers != null && headers.containsKey(name)) {
      ArrayList<String> arrayList = headers.get(name);
      return arrayList.toArray(new String[arrayList.size()]);
    }
    return null;
  }

  @Override
  public String getFirstHeader(String name) {
    String[] headers = getHeaders(name);
    return headers == null ? null : headers.length > 0 ? headers[0] : null;
  }

  @Override
  public String getStatusString() {
    return statusString;
  }

  /**
   * Note: this method should be used for debugging only.
   */
  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder("Response {")
        .append("\n  status=")
        .append(status)
        .append("\n  http-code=")
        .append(httpCode)
        .append("\n  statusString=")
        .append(statusString)
        .append("\n  required-action=")
        .append(action);
    if (headers != null) {
      stringBuilder.append("\n  headers=");
      for (Entry<String, ArrayList<String>> entry : headers.entrySet()) {
        stringBuilder
            .append("\n    ")
            .append(entry.getKey())
            .append(" : ")
            .append(entry.getValue());
      }
    }
    if (content != null) {
      stringBuilder.append("\n  content=<<<");
      if (contentType != null && contentType.startsWith("text")) {
        stringBuilder.append(getContentAsString());
      } else {
        stringBuilder.append("*bytes*");
      }
      stringBuilder.append("\n  >>>");
    }
    stringBuilder.append("\n}");
    return stringBuilder.toString();
  }

  protected ResponseImpl setRequiredAction(Intent action) {
    this.action = action;
    return this;
  }

  protected ResponseImpl addStatusMessage(String statusString) {
    if (statusString != null) {
      if (this.statusString == null) {
        this.statusString = statusString;
      } else {
        this.statusString += ('\n' + statusString);
      }
    }
    return this;
  }

  protected ResponseImpl clearStatusMessage() {
    this.statusString = null;
    return this;
  }

  private void initHeaders(HttpResponse response) {
    Header[] allHeaders = response.getAllHeaders();
    if (allHeaders != null && allHeaders.length > 0) {
      headers = new HashMap<String, ArrayList<String>>();
      for (Header header : allHeaders) {
        String key = header.getName();
        ArrayList<String> values = headers.get(key);
        if (values == null) {
          values = new ArrayList<String>();
          headers.put(key, values);
        }
        values.add(header.getValue());
      }
    }
  }

  private Status handleHttpOk(HttpResponse response) {
    HttpEntity entity = response.getEntity();
    if (entity != null) {
      try {
        content = fromStream(entity.getContent());
        Header contentTypeHeader = entity.getContentType();
        contentType = contentTypeHeader == null ? null : contentTypeHeader.getValue();
      } catch (Exception e) {
        statusString = e.getMessage();
        httpCode = -1;
        return Status.NETWORK_ERROR;
      }
    }
    return Status.HTTP_OK;
  }

  private Status handleHttpOther(HttpResponse response) {
    addStatusMessage(response.getStatusLine().getReasonPhrase());
    String moreStatus = null;
    HttpEntity entity = response.getEntity();
    if (entity != null) {
      try {
        String charset = Constants.DEFAULT_CHARSET_NAME;
        Header contentTypeHeader = entity.getContentType();
        if (contentTypeHeader != null) {
          String contentType = contentTypeHeader.getValue();
          charset = UrlContentManager.getCharSetEncoding(contentType);
        }
        moreStatus = new String(fromStream(entity.getContent()), charset);
      } catch (Exception exc) {
        // Cannot read more status, ignore.
      }
    }
    addStatusMessage(moreStatus);
    return Status.HTTP_OTHER;
  }
  
  private static byte[] fromStream(InputStream in) throws IOException {
    BufferedInputStream bufferedInput = null;
    try {
      bufferedInput = new BufferedInputStream(in, BUFFER_SIZE);
      int len = -1;
      byte[] buf = new byte[BUFFER_SIZE];
      ByteArrayOutputStream bytesBuilder = new ByteArrayOutputStream(BUFFER_SIZE);
      while ((len = bufferedInput.read(buf)) > -1) {
        bytesBuilder.write(buf, 0, len);
      }
      return bytesBuilder.toByteArray();
    } finally {
      if (bufferedInput != null) {
        bufferedInput.close();
      }
    }
  }
}
