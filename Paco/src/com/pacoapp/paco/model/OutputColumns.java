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
/**
 * 
 */
package com.pacoapp.paco.model;

import com.pacoapp.paco.shared.model2.OutputBaseColumns;

import android.net.Uri;
import android.provider.BaseColumns;

public class OutputColumns extends OutputBaseColumns implements BaseColumns {
  
  
  public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.paco.output";
  public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.paco.output";

  public static final Uri CONTENT_URI = Uri.parse("content://"+ExperimentProviderUtil.AUTHORITY+"/outputs");
  
  
}