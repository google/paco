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
    mainPanel.add(new HelpPageLocaleHelper().getLocalizedResource());
  }
  

  class HelpPageLocaleHelper extends GWTLocaleHelper<HTML> {

    protected HTML getEnVersion() {
      return new HTML(parent.resources.helpHtml().getText());
    }
  
    protected HTML getJaVersion() {
      return new HTML(parent.resources.helpHtml_ja().getText());
    }
    
    protected HTML getFiVersion() {
      return new HTML(parent.resources.helpHtml_fi().getText());
    }

    protected HTML getPtVersion() {
      return new HTML(parent.resources.helpHtml_pt().getText());
    }
  };


}
