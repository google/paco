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
package com.google.sampling.experiential.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.TextResource;

/**
 * The image bundle for Paco.
 *
 * @author Bob Evans
 */
public interface Images extends ClientBundle {

  @Source(value = "PACO256.png")
  ImageResource pacoFaceLogo();

  @Source(value = "PACO64.png")
  ImageResource pacoSmallLogo();

  @Source(value = "question-b42.png")
  ImageResource question();

  @Source(value = "qrcode.png")
  ImageResource qrcode();

  @Source(value = "helptext.html")
  TextResource helpHtml();

  @Source(value = "helptext_ja.html")
  TextResource helpHtml_ja();

  @Source(value = "helptext_fi.html")
  TextResource helpHtml_fi();

  @Source(value = "helptext_pt.html")
  TextResource helpHtml_pt();

  @Source(value = "index2.html")
  TextResource indexHtml();

  @Source(value = "index2_ja.html")
  TextResource indexHtml_ja();

  @Source(value = "index2_fi.html")
  TextResource indexHtml_fi();

  @Source(value = "index2_pt.html")
  TextResource indexHtml_pt();

}

