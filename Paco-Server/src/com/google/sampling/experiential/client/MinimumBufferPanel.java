package com.google.sampling.experiential.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.paco.shared.model2.MinimumBufferable;

public class MinimumBufferPanel extends Composite {

  private MinimumBufferable minimumBufferable;
  private HorizontalPanel mainPanel;

  public MinimumBufferPanel(final MinimumBufferable minimumBufferable) {
    MyConstants myConstants = GWT.create(MyConstants.class);
    this.minimumBufferable = minimumBufferable;
    mainPanel = new HorizontalPanel();
    mainPanel.setSpacing(2);
    initWidget(mainPanel);

    Label minimumBufferLabel = new Label(myConstants.minimumBuffer() +":");
    minimumBufferLabel.setStyleName("gwt-Label-Header");
    mainPanel.add(minimumBufferLabel);

    final TextBox textBox = new TextBox();
    textBox.setWidth("5em");
    textBox.setMaxLength(5);
    mainPanel.add(textBox);

    textBox.setText(getMinimumBuffer());

    Label minutesLabel = new Label("(" + myConstants.minutes() + ")");
    minutesLabel.setStyleName("paco-small");
    mainPanel.add(minutesLabel);

    textBox.addValueChangeHandler(new ValueChangeHandler() {
      @Override
      public void onValueChange(ValueChangeEvent arg0) {
        String text = textBox.getText();

        try {
          int minBufferMinutes = Integer.parseInt(text);
          minimumBufferable.setMinimumBuffer(minBufferMinutes);
        } catch (NumberFormatException nfe) {

        }

      }
    });

  }

  private String getMinimumBuffer() {
    if (minimumBufferable.getMinimumBuffer() != null) {
      return Integer.toString(minimumBufferable.getMinimumBuffer());
    } else {
      return Integer.toString(minimumBufferable.getDefaultMinimumBuffer());
    }

  }

}
