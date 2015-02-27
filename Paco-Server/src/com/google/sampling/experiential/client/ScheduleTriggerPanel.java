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

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.paco.shared.model2.ScheduleTrigger;

/**
 * View trigger configuration.
 *
 */
public class ScheduleTriggerPanel extends ActionTriggerPanel {

  private ScheduleTrigger scheduleTrigger;

  public ScheduleTriggerPanel(ActionTriggerListPanel parent, ScheduleTrigger trigger) {
    super(parent, trigger);
    this.scheduleTrigger = trigger;
    init();
  }

  protected void createTriggerTypeSpecificUI(VerticalPanel verticalPanel) {
    verticalPanel.add(createScheduleListPanel());
  }

  private Widget createScheduleListPanel() {
    return new ScheduleListPanel(scheduleTrigger);
  }

}
