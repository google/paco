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

public class Binding {

	private String varName;
	private Class responseType;
	private Object value;
	
	public Binding(String varName, Class responseType, Object value) {
		this.varName = varName;
		this.responseType = responseType;
		this.value = value;
	}
	
	public String getVarName() {
		return varName;
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}

	public void setResponseType(Class responseType) {
		this.responseType = responseType;
	}

	public Class responseType() {
		// TODO Auto-generated method stub
		return responseType;
	}

	public Object value() {
//		if (value instanceof Integer) {
//			return (Integer) value;
//		} 
		return value;
	}

}
