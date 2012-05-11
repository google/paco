package com.google.sampling.experiential.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;

public class HelpPage extends Composite {

  private Main parent;
  private VerticalPanel mainPanel;

  public HelpPage(Main parent) {
    this.parent = parent;
    mainPanel = new VerticalPanel();
    mainPanel.setSpacing(2);
    mainPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    initWidget(mainPanel);
    // mainPanel.setWidth("258px");
    createLayout();
  }

  private void createLayout() {
    HTML page = new HTML(parent.resources.helpHtml().getText());
    mainPanel.add(page);
  }
  
}
