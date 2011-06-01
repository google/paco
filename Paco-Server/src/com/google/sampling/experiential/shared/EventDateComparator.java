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
// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

public class EventDateComparator implements Comparator<EventDAO>, Serializable {

  public EventDateComparator() {

  }

  @Override
  public int compare(EventDAO o1, EventDAO o2) {
    Date when1 = o1.getWhen();
    Date when2 = o2.getWhen();
    if (when1 == null || when2 == null) {
      return 0;
    } else if (when1.after(when2)) {
      return -1;
    } else if (when2.after(when1)) {
      return 1;
    }
    return 0;
  }
}
