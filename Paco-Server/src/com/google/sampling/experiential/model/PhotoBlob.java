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
package com.google.sampling.experiential.model;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Key;

/**
 * Storage for a photo Input.
 * 
 * @author Bob Evans
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class PhotoBlob implements Comparable<PhotoBlob> {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key key;

  @Persistent
  private String name;

  @Persistent
  private Blob value;


  public PhotoBlob(String name, byte[] value) {
    super();
    this.name = name;
    this.value = new Blob(value);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public byte[] getValue() {
    return value.getBytes();
  }

  public void setValue(byte[] value) {
    this.value = new Blob(value);
  }

  @Override
  public int compareTo(PhotoBlob o) {
    return getName().compareTo(o.getName());
  }


}
