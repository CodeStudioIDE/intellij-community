package com.intellij.openapi.wm.impl.status;

import com.intellij.diagnostic.IdeMessagePanel;
import com.intellij.diagnostic.MessagePool;
import com.intellij.ide.ui.UISettings;
import com.intellij.ide.ui.UISettingsListener;
import com.intellij.openapi.application.impl.LaterInvocator;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ex.ProcessInfo;
import com.intellij.openapi.wm.ex.ProgressIndicatorEx;
import com.intellij.openapi.wm.ex.StatusBarEx;
import com.intellij.ui.EdgeBorder;
import com.intellij.ui.UIBundle;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.ui.popup.NotificationPopup;
import com.intellij.util.ui.EmptyIcon;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StatusBarImpl extends JPanel implements StatusBarEx {

  protected final TextPanel myInfoPanel = new TextPanel(new String[]{"#"},true);
  protected final PositionPanel myPositionPanel = new PositionPanel();
  protected final ToggleReadOnlyAttributePanel myToggleReadOnlyAttributePanel = new ToggleReadOnlyAttributePanel();
  protected final MemoryUsagePanel myMemoryUsagePanel = new MemoryUsagePanel();
  protected final TextPanel myStatusPanel = new TextPanel(new String[]{UIBundle.message("status.bar.insert.status.text"),
    UIBundle.message("status.bar.overwrite.status.text")},false);
  protected final TogglePopupHintsPanel myEditorHighlightingPanel;
  protected final IdeMessagePanel myMessagePanel = new IdeMessagePanel(MessagePool.getInstance());
  private final JPanel myCustomIndicationsPanel = new JPanel(new GridBagLayout());
  protected String myInfo = "";
  private final Icon myLockedIcon = IconLoader.getIcon("/nodes/lockedSingle.png");
  private final Icon myUnlockedIcon = myLockedIcon != null ? new EmptyIcon(myLockedIcon.getIconWidth(), myLockedIcon.getIconHeight()) : null;

  protected final MyUISettingsListener myUISettingsListener;
  protected JPanel myInfoAndProgressPanel;

  private UISettings myUISettings;

  public StatusBarImpl(UISettings uiSettings) {
    super();
    myEditorHighlightingPanel = new TogglePopupHintsPanel();
    myUISettings = uiSettings;
    constructUI();

    myUISettingsListener=new MyUISettingsListener();
  }

  protected void constructUI() {
    setLayout(new BorderLayout());

    final Border lineBorder = new EdgeBorder(EdgeBorder.EDGE_RIGHT);
    final Border emptyBorder = BorderFactory.createEmptyBorder(3, 2, 2, 2);
    final Border compoundBorder = BorderFactory.createCompoundBorder(emptyBorder, lineBorder);

    myInfoPanel.setBorder(emptyBorder);
    myInfoPanel.setOpaque(false);

    myInfoAndProgressPanel = new JPanel();
    myInfoAndProgressPanel.setBorder(compoundBorder);
    myInfoAndProgressPanel.setOpaque(false);
    removeAllIndicators();

    add(myInfoAndProgressPanel, BorderLayout.CENTER);

    final GridBagConstraints gbConstraints = new GridBagConstraints();
    gbConstraints.fill = GridBagConstraints.BOTH;
    gbConstraints.weightx = 1;

    final JPanel rightPanel = new JPanel(new GridBagLayout());
    rightPanel.setOpaque(false);

    gbConstraints.fill = GridBagConstraints.VERTICAL;
    gbConstraints.weightx = 0;
    gbConstraints.weighty = 1;

    myPositionPanel.setBorder(compoundBorder);
    myPositionPanel.setOpaque(false);
    rightPanel.add(myPositionPanel, gbConstraints);

    myToggleReadOnlyAttributePanel.setBorder(compoundBorder);
    myToggleReadOnlyAttributePanel.setOpaque(false);
    setWriteStatus(false);
    rightPanel.add(myToggleReadOnlyAttributePanel, gbConstraints);

    myStatusPanel.setBorder(compoundBorder);
    myStatusPanel.setOpaque(false);
    rightPanel.add(myStatusPanel, gbConstraints);

    myEditorHighlightingPanel.setBorder(compoundBorder);
    myEditorHighlightingPanel.setOpaque(false);
    rightPanel.add(myEditorHighlightingPanel, gbConstraints);

    myCustomIndicationsPanel.setVisible(false); // Will become visible when any of indications really adds.
    myCustomIndicationsPanel.setBorder(compoundBorder);
    myCustomIndicationsPanel.setOpaque(false);
    rightPanel.add(myCustomIndicationsPanel, gbConstraints);

    myMessagePanel.setOpaque(false);
    rightPanel.add(myMessagePanel, gbConstraints);

    //  myMemoryUsagePanel.setOpaque(false);
    myMemoryUsagePanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.anchor = GridBagConstraints.WEST;
    rightPanel.add(myMemoryUsagePanel, gbConstraints);

    add(rightPanel, BorderLayout.EAST);
  }

  public void add(final ProgressIndicatorEx indicator, ProcessInfo info) {
    final InlineProgressIndicator inline = new InlineProgressIndicator(true, info) {
      public void cancel() {
        super.cancel();
        removeAllIndicators();
      }

      protected void cancelRequest() {
        indicator.cancel();
      }

      public void stop() {
        super.stop();
        queueRemoveAllIndicators();
      }
    };
    myInfoAndProgressPanel.removeAll();
    myInfoAndProgressPanel.setLayout(new GridLayout(1, 2));
    myInfoAndProgressPanel.add(myInfoPanel);

    final Wrapper inlineComponent = new Wrapper(inline.getComponent());
    inlineComponent.setBorder(BorderFactory.createCompoundBorder(new EdgeBorder(EdgeBorder.EDGE_LEFT), new EmptyBorder(0, 2, 0, 0)));
    myInfoAndProgressPanel.add(inlineComponent);

    indicator.setStateDelegate(inline);
    
    myInfoPanel.revalidate();
    myInfoPanel.repaint();
  }

  private void queueRemoveAllIndicators() {
    LaterInvocator.invokeLater(new Runnable() {
      public void run() {
        removeAllIndicators();
      }
    });
  }

  public void removeAllIndicators() {
    myInfoAndProgressPanel.removeAll();
    myInfoAndProgressPanel.setLayout(new GridLayout(1, 1));
    myInfoAndProgressPanel.add(myInfoPanel);
    myInfoPanel.revalidate();
    myInfoPanel.repaint();
  }

  protected final void paintComponent(final Graphics g) {
    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());

    GradientPaint paint = new GradientPaint(getWidth()/2, 0, getBackground().darker(), getWidth()/2, getHeight()/10, getBackground());
    final Graphics2D g2 = (Graphics2D) g;
    g2.setPaint(paint);
    g.fillRect(0, 0, getWidth(), getHeight()/10);

    paint = new GradientPaint(getWidth()/2, getHeight() - getHeight()/7, getBackground(), getWidth()/2, getHeight() - 1, getBackground().darker());
    g2.setPaint(paint);
    g.fillRect(0, getHeight() - getHeight()/7, getWidth(), getHeight());
  }

  public final void addNotify() {
    super.addNotify();
    setMemoryIndicatorVisible(myUISettings.SHOW_MEMORY_INDICATOR);
    myUISettings.addUISettingsListener(myUISettingsListener);
  }

  public final void removeNotify() {
    UISettings.getInstance().removeUISettingsListener(myUISettingsListener);
    super.removeNotify();
  }

  public final void setInfo(String s) {
    myInfo = s;
    if (s == null){
      s = " ";
    }
    myInfoPanel.setText(s);
  }

  public void fireNotificationPopup(JComponent content, final Color backgroundColor) {
    new NotificationPopup(this, content, backgroundColor);
  }

  public final String getInfo() {
    return myInfo;
  }

  public final void setPosition(String s) {
    if (s == null){
      s = " ";
    }
    myPositionPanel.setText(s);
  }

  public final void setStatus(String s) {
    if (s == null){
      s = " ";
    }
    myStatusPanel.setText(s);
  }

  public final void addCustomIndicationComponent(JComponent c) {
    final GridBagConstraints gbConstraints = new GridBagConstraints();
    gbConstraints.fill = GridBagConstraints.BOTH;
    gbConstraints.weightx = 0;
    gbConstraints.weighty = 1;

    myCustomIndicationsPanel.setVisible(true);
    myCustomIndicationsPanel.add(c, gbConstraints);
  }

  public final void setStatusEnabled(final boolean enabled) {
    myStatusPanel.setEnabled(enabled);
  }

  public final void setWriteStatus(final boolean locked) {
    myToggleReadOnlyAttributePanel.setIcon(
      locked ? myLockedIcon : myUnlockedIcon
    );
  }

  /**
   * Clears all sections in status bar
   */
  public final void clear(){
    setStatus(null);
    setStatusEnabled(false);
    setWriteStatus(false);
    setPosition(null);
    updateEditorHighlightingStatus(true);
  }

  public final void updateEditorHighlightingStatus(final boolean isClear) {
    myEditorHighlightingPanel.updateStatus(isClear);
  }

  public void cleanupCustomComponents() {
    myCustomIndicationsPanel.removeAll();
  }

  public final Dimension getMinimumSize() {
    final Dimension p = super.getPreferredSize();
    final Dimension m = super.getMinimumSize();
    return new Dimension(m.width, p.height);
  }

  public final Dimension getMaximumSize() {
    final Dimension p = super.getPreferredSize();
    final Dimension m = super.getMaximumSize();
    return new Dimension(m.width, p.height);
  }

  private void setMemoryIndicatorVisible(final boolean state) {
    if (myMemoryUsagePanel != null) {
      myMemoryUsagePanel.setVisible(state);
    }
  }

  public void disposeListeners() {
    myEditorHighlightingPanel.dispose();
  }

  private final class MyUISettingsListener implements UISettingsListener{
    public void uiSettingsChanged(final UISettings uiSettings) {
      setMemoryIndicatorVisible(uiSettings.SHOW_MEMORY_INDICATOR);
    }
  }

}
