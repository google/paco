package com.google.sampling.experiential.client;

import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

class PanelPair {

  Panel container;
  Widget valueHolder;
  
  
  public PanelPair(Panel container, Widget widget) {
    this.container = container;
    this.valueHolder = widget;
        
  }


}