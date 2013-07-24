package com.google.sampling.experiential.client;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.paco.shared.model.ExperimentDAO;

public class InputsListPanelTest extends GWTTestCase {
  
  InputsListPanel inputsListPanel;
  InputsPanel inputsPanel0;
  InputsPanel inputsPanel1;
  InputsPanel inputsPanel2;
  
  @Override
  public String getModuleName() {
    return "com.google.sampling.experiential.PacoEventserver";
  }
  
  protected void gwtSetUp() {
    inputsListPanel = new InputsListPanel(new ExperimentDAO());
    inputsPanel0 = inputsListPanel.getInputsPanels().get(0);
    inputsListPanel.addInput(inputsPanel0);
    inputsListPanel.addInput(inputsPanel0);
    List<InputsPanel> panelsList = inputsListPanel.getInputsPanels();
    checkPreconditions(panelsList);
    inputsPanel1 = panelsList.get(1);
    inputsPanel2 = panelsList.get(2);
  }

  private void checkPreconditions(List<InputsPanel> panelsList) {
    Preconditions.checkArgument(panelsList.size() == 3);
    assertSame(panelsList.get(0), inputsPanel0);
  }
  
  public void testPanelReordering() {
    VerticalPanel contentPanel = inputsListPanel.getContentPanel();
    contentPanel.remove(inputsPanel1);
    contentPanel.add(inputsPanel1);    inputsListPanel.updateInputPanelsList();
    List<InputsPanel> panelsList = inputsListPanel.getInputsPanels();
    assertSame(panelsList.get(0), inputsPanel0);
    assertSame(panelsList.get(1), inputsPanel2);
    assertSame(panelsList.get(2), inputsPanel1);
  }

}
