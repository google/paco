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
package com.google.android.apps.paco;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.text.TextUtils.StringSplitter;

public class Output implements Parcelable {

  public static class Creator implements Parcelable.Creator<Output> {

    public Output createFromParcel(Parcel source) {
      Output input = new Output();
      input.id = source.readLong();
      input.eventId = source.readLong();
      input.input_server_id = source.readLong();
      input.name = source.readString();
      input.answer = source.readString();
      
      return input;
    }

    public Output[] newArray(int size) {
      return new Output[size];
    }
  }
  
  public static final Creator CREATOR = new Creator();

  @JsonIgnore
  private long id = -1;
  
  @JsonIgnore
  private long eventId = -1;
  
  @JsonProperty("inputId")
  private long input_server_id = -1;
  private String name;
  private String answer;

  public Output() {
    
  }

  @JsonIgnore
  public long getId() {
    return id;
  }

  @JsonIgnore
  public void setId(long id) {
    this.id = id;
  }

  @JsonIgnore
  public long getEventId() {
    return eventId;
  }

  @JsonIgnore
  public void setEventId(long eventId) {
    this.eventId = eventId;
  }

  @JsonProperty("inputId")
  public long getInputServerId() {
    return input_server_id;
  }

  @JsonProperty("inputId")
  public void setInputServerId(long serverId) {
    this.input_server_id = serverId;
  }

  public String getName() {
    return name;
  }

  public void setName(String text) {
    this.name = text;
  }

  public String getAnswer() {
    return answer;
  }

  public void setAnswer(String answer) {
    this.answer = answer;
  }

  @JsonIgnore
  public int describeContents() {
    return 0;
  }

  @JsonIgnore
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(id);
    dest.writeLong(eventId);
    dest.writeLong(input_server_id);
    
    dest.writeString(name);
    dest.writeString(answer);
  }  

  /**
   * @param input
   * @return
   */
  String getDisplayForList(Input input) {
    List<String> listChoices = input.getListChoices();
    String answer = getAnswer();
    if (answer == null) {
      return "";
    }

    if (!input.isMultiselect()) {      
      int index = Integer.parseInt(answer) - 1;
      if (index < listChoices.size()) {
        return listChoices.get(index);
      } else {
        return "error: index value too large for list choices: " + index;
      }
    }
    // split answer, then retrieve list choice for each and return an array!?
    StringSplitter stringSplitter = new TextUtils.SimpleStringSplitter(',');
    stringSplitter.setString(getAnswer());
    boolean first = true;
    StringBuilder buf = new StringBuilder();
    for (String piece : stringSplitter) {
      if (first) {
        first = false;
      } else {
        buf.append(",");
      }
      int index = Integer.parseInt(piece) - 1;
      if (index < listChoices.size()) {
        buf.append(listChoices.get(index));
      } else {
        buf.append("error: index value too large for list choices: " + index);
      }
    }
    return buf.toString();
  }

  /**
   * @param output
   * @return
   */
  String getDisplayForLikert() {
    return getAnswer();
  }

  public String getDisplayOfAnswer(Input input) {
    if (input.getResponseType().equals(Input.LIST)) {
      return getDisplayForList(input);
    }
    if (input.getResponseType().equals(Input.LIKERT) && getAnswer() != null) {
      return getDisplayForLikert();
    }
    return getAnswer();
  }

}
