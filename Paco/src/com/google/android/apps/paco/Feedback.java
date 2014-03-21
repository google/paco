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

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import android.os.Parcel;
import android.os.Parcelable;

public class Feedback implements Parcelable {

  public static class Creator implements Parcelable.Creator<Feedback> {

    public Feedback createFromParcel(Parcel source) {
      Feedback input = new Feedback();
      input.id = source.readLong();
      input.experimentId = source.readLong();
      input.serverId = source.readLong();
      input.text = source.readString();
      return input;
    }

    public Feedback[] newArray(int size) {
      return new Feedback[size];
    }
  }

  public static final Creator CREATOR = new Creator();

  @JsonIgnore
  private Long id;
  private Long experimentId;

  @JsonProperty("id")
  private Long serverId;
  private String text = "";

  @JsonIgnore
  public Long getId() {
    return id;
  }

  @JsonIgnore
  public void setId(Long id) {
    this.id = id;
  }

  public Long getExperimentId() {
    return experimentId;
  }

  public void setExperimentId(Long experimentId) {
    this.experimentId = experimentId;
  }

  @JsonProperty("id")
  public Long getServerId() {
    return serverId;
  }

  @JsonProperty("id")
  public void setServerId(Long serverId) {
    this.serverId = serverId;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public int describeContents() {
    return 0;
  }

  public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(id);
    dest.writeLong(experimentId);
    dest.writeLong(serverId);
    dest.writeString(text);
  }

  public String getDisplay(Event latestEvent, Experiment experiment) {
    StringBuilder buf = new StringBuilder("<html><body style='text-align:center;color:#4272db';><h2>");
    buf.append(getText());
    buf.append("</h2>");
    buf.append("<div style='text-align:left;line-height:1.7;font-size:28;font-weight:bold;'>Your responses:<br/></div><div>");
    for (Output output : latestEvent.getResponses()) {
      if (output.getAnswer() == null || output.getAnswer().length() == 0) {
        continue;
      }
      Input input = experiment.getInputById(output.getInputServerId());
      buf.append("<div><div style='text-align:left;line-height:1.5;font-size:20;'>");
      //appendElidedText(buf, output.getText(), 25);
      String textOfInputForOutput = getTextOfInputForOutput(experiment, output);
      buf.append(textOfInputForOutput);
      buf.append("</div><br/><div style='color:#333333;text-align:center;line-height:1.5;font-size:18;'>");
      //appendElidedText(buf, output.getAnswer(), 25);
      if (textOfInputForOutput.equals(Input.PHOTO)) {
        buf.append("<img src=\"data:image/jpg;base64,");
        buf.append(output.getAnswer());
        buf.append("\" width=150>");
      } else{
        buf.append(output.getDisplayOfAnswer(input));
        buf.append("<a href='file:///android_asset/time.html?" + input.getId() + "'>Chart</a>");
      }
      buf.append("</div></div>");
    }

    buf.append("</div></body></html>");
    return buf.toString();
  }

  String getTextOfInputForOutput(Experiment experiment, Output output) {
    for (Input input : experiment.getInputs()) {
      if (input.getServerId().equals(output.getInputServerId())) {
        if (!input.isInvisible()) {
          return input.getText();
        } else {
          return input.getResponseType();
        }
      }
    }
    return output.getName();
  }

  private void appendElidedText(StringBuilder buf, String text, int maxLength) {
    if (text.length() > maxLength) {
      buf.append(text.substring(0, Math.min(text.length(), maxLength - 3)));
      buf.append("...");
    } else {
      buf.append(text);
    }
  }


}
