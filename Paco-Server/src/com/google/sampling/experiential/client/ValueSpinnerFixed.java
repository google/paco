package com.google.sampling.experiential.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.widgetideas.client.Spinner;
import com.google.gwt.widgetideas.client.Spinner.SpinnerResources;
import com.google.gwt.widgetideas.client.SpinnerListener;
import com.google.gwt.widgetideas.client.ValueSpinner;

/**
 * A {@link ValueSpinner} is a combination of a {@link TextBox} and a
 * {@link Spinner} to allow spinning <h3>CSS Style Rules</h3> <ul class='css'>
 * <li>.gwt-ValueSpinner { primary style }</li> <li>.gwt-ValueSpinner .textBox {
 * the textbox }</li> <li>.gwt-ValueSpinner .arrows { the spinner arrows }</li>
 * </ul>
 */
public class ValueSpinnerFixed extends HorizontalPanel {
  /**
   * Resources used.
   */
  public interface ValueSpinnerResources extends ClientBundle {
    @Source(value = "bg_textbox.png")
    ImageResource background();

    @Source(value="ValueSpinner.css")
    com.google.gwt.resources.client.CssResource css();
  }
  private static ValueSpinnerResources defaultResources;

  private static final String STYLENAME_DEFAULT = "gwt-ValueSpinner";

  private Spinner spinner;
  private final TextBox valueBox = new TextBox();

  private SpinnerListener spinnerListener = new SpinnerListener() {
    public void onSpinning(long value) {
      if (getSpinner() != null) {
        getSpinner().setValue(value, false);
      }
      valueBox.setText(formatValue(value));
    }
  };

  private KeyPressHandler keyPressHandler = new KeyPressHandler() {

    public void onKeyPress(KeyPressEvent event) {
      int index = valueBox.getCursorPos();
      String previousText = valueBox.getText();
      String newText;
      if (valueBox.getSelectionLength() > 0) {
        newText = previousText.substring(0, valueBox.getCursorPos())
            + event.getCharCode()
            + previousText.substring(valueBox.getCursorPos()
                + valueBox.getSelectionLength(), previousText.length());
      } else {
        newText = previousText.substring(0, index) + event.getCharCode()
            + previousText.substring(index, previousText.length());
      }
      valueBox.cancelKey();
      try {
        long newValue = parseValue(newText);
        if (spinner.isConstrained()
            && (newValue > spinner.getMax() || newValue < spinner.getMin())) {
          return;
        }
        spinner.setValue(newValue, true);
      } catch (Exception e) {
        // valueBox.cancelKey();
      }
    }
  };

  /**
   * @param value initial value
   */
  public ValueSpinnerFixed(long value) {
    this(value, 0, 0, 1, 99, false);
  }

  /**
   * @param value initial value
   * @param styles the styles and images used by this widget
   * @param images the images used by the spinner
   */
  public ValueSpinnerFixed(long value, ValueSpinnerResources styles,
      SpinnerResources images) {
    this(value, 0, 0, 1, 99, false, styles, images);
  }

  /**
   * @param value initial value
   * @param min min value
   * @param max max value
   */
  public ValueSpinnerFixed(long value, int min, int max) {
    this(value, min, max, 1, 99, true);
  }

  /**
   * @param value initial value
   * @param min min value
   * @param max max value
   * @param minStep min value for stepping
   * @param maxStep max value for stepping
   */
  public ValueSpinnerFixed(long value, int min, int max, int minStep, int maxStep) {
    this(value, min, max, minStep, maxStep, true);
  }

  /**
   * @param value initial value
   * @param min min value
   * @param max max value
   * @param minStep min value for stepping
   * @param maxStep max value for stepping
   * @param constrained if set to false min and max value will not have any
   *          effect
   */
  public ValueSpinnerFixed(long value, int min, int max, int minStep, int maxStep,
      boolean constrained) {
    this(value, min, max, minStep, maxStep, constrained, null);
  }

  /**
   * @param value initial value
   * @param min min value
   * @param max max value
   * @param minStep min value for stepping
   * @param maxStep max value for stepping
   * @param constrained if set to false min and max value will not have any
   *          effect
   * @param resources the styles and images used by this widget
   */
  public ValueSpinnerFixed(long value, int min, int max, int minStep, int maxStep,
      boolean constrained, ValueSpinnerResources resources) {
    this(value, min, max, minStep, maxStep, constrained, resources, null);
  }

  private static void injectStylesheet(String contents,
      ValueSpinnerResources resources) {
    if (resources != null) {
//      for (ResourcePrototype p : resources.getClass()) {
//        if (p instanceof DataResource) {
//          contents = contents.replaceAll("%" + p.getName() + "%",
//                                         ((DataResource) p).getUrl());
//        }
//      }
    }
    StyleInjector.injectStylesheet(contents);
  }

  /**
   * @param value initial value
   * @param min min value
   * @param max max value
   * @param minStep min value for stepping
   * @param maxStep max value for stepping
   * @param constrained if set to false min and max value will not have any
   *          effect
   * @param resources the styles and images used by this widget
   * @param images the images used by the spinner
   */
  public ValueSpinnerFixed(long value, int min, int max, int minStep, int maxStep,
      boolean constrained, ValueSpinnerResources resources,
      SpinnerResources images) {
    super();
    if (resources != null) {
      injectStylesheet(resources.css().getText(), resources);
    } else if (defaultResources == null) {
      defaultResources = GWT.create(ValueSpinnerResources.class);
      StyleInjector.injectStylesheet(defaultResources.css().getText());
    }
    setStylePrimaryName(STYLENAME_DEFAULT);
    if (images == null) {
      spinner = new Spinner(spinnerListener, value, min, max, minStep, maxStep,
          constrained);
    } else {
      spinner = new Spinner(spinnerListener, value, min, max, minStep, maxStep,
          constrained, images);
    }
    valueBox.setStyleName("textBox");
    valueBox.addKeyPressHandler(keyPressHandler);
    setVerticalAlignment(ALIGN_MIDDLE);
    add(valueBox);
    VerticalPanel arrowsPanel = new VerticalPanel();
    arrowsPanel.setStylePrimaryName("arrows");
    arrowsPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
    arrowsPanel.add(spinner.getIncrementArrow());
    arrowsPanel.add(spinner.getDecrementArrow());
    add(arrowsPanel);
  }

  /**
   * @return the Spinner used by this widget
   */
  public Spinner getSpinner() {
    return spinner;
  }

  /**
   * @return the SpinnerListener used to listen to the {@link Spinner} events
   */
  public SpinnerListener getSpinnerListener() {
    return spinnerListener;
  }

  /**
   * @return the TextBox used by this widget
   */
  public TextBox getTextBox() {
    return valueBox;
  }

  /**
   * @return whether this widget is enabled.
   */
  public boolean isEnabled() {
    return spinner.isEnabled();
  }

  /**
   * Sets whether this widget is enabled.
   *
   * @param enabled true to enable the widget, false to disable it
   */
  public void setEnabled(boolean enabled) {
    spinner.setEnabled(enabled);
    valueBox.setEnabled(enabled);
  }

  /**
   * @param value the value to format
   * @return the formatted value
   */
  protected String formatValue(long value) {
    return String.valueOf(value);
  }

  /**
   * @param value the value to parse
   * @return the parsed value
   */
  protected long parseValue(String value) {
    return Long.valueOf(value);
  }
}
