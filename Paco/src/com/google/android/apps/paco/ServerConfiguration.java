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

import com.pacoapp.paco.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class ServerConfiguration extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.server_configuration);
    final EditText serverAddressField = (EditText)findViewById(R.id.ServerAddressField);
    final UserPreferences userPref = new UserPreferences(this);
    String serverAddress = userPref.getServerAddress();
    serverAddressField.setText(serverAddress);
    Button saveButton = (Button)findViewById(R.id.ServerConfigurationSaveButton);
    saveButton.setOnClickListener(new OnClickListener() {

      public void onClick(View arg0) {
        String text = serverAddressField.getText().toString();
        userPref.setServerAddress(text);
        finish();
      }
    
    });
  }

  
  
}
