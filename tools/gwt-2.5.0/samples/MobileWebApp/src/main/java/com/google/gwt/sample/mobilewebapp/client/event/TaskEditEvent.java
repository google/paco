/*
 * Copyright 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.gwt.sample.mobilewebapp.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.sample.mobilewebapp.shared.TaskProxy;

/**
 * Fired when the user wants to edit a task.
 */
public class TaskEditEvent extends GwtEvent<TaskEditEvent.Handler> {
  /**
   * Implemented by objects that handle {@link TaskEditEvent}.
   */
  public interface Handler extends EventHandler {
    void onTaskEdit(TaskEditEvent event);
  }

  /**
   * The event type.
   */
  public static final Type<TaskEditEvent.Handler> TYPE = new Type<TaskEditEvent.Handler>();

  private final TaskProxy task;

  public TaskEditEvent(TaskProxy task) {
    this.task = task;
  }

  @Override
  public final Type<TaskEditEvent.Handler> getAssociatedType() {
    return TYPE;
  }

  public TaskProxy getReadOnlyTask() {
    return task;
  }

  @Override
  protected void dispatch(TaskEditEvent.Handler handler) {
    handler.onTaskEdit(this);
  }
}
