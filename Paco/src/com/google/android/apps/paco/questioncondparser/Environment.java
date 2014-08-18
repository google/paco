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
package com.google.android.apps.paco.questioncondparser;
import com.pacoapp.paco.R;
import java.util.HashMap;


public class Environment {

	private HashMap<String, Binding> knownQuestions;

	public Environment() {
		this.knownQuestions = new HashMap<String, Binding>();
	}
	
	public boolean exists(String id) {
		return knownQuestions.containsKey(id);		
	}
	
	public boolean correctType(String id) {
		Binding input = knownQuestions.get(id);
		return input != null && input.responseType().equals(Integer.class);
	}

	public void addInput(Binding input) {
	  if (input != null) {
	    //if (input.value() == null) {
	    //  knownQuestions.remove(input.getVarName());
	    //} else {
	      knownQuestions.put(input.getVarName(), input);
	    //}
	  } 
	}
	
	public Object getValue(String id) {
		return knownQuestions.get(id).value();
	}
}
