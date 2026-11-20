package com.playerinspector.panel.layout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DpsSectionLayout extends JPanel {

    private final String dpsButtonText = "View my DPS";
    private final JButton dpsButton = new JButton(dpsButtonText);
    public JPanel container = new JPanel(new BorderLayout(5, 0));

    public DpsSectionLayout(boolean visible) {
        add(container);
        container.add(dpsButton);
        dpsButton.setVisible(visible);
    }


    public void setActionListener(ActionListener act) {
        dpsButton.addActionListener(act);
    }

    public void setVisibility(boolean visible) {
        dpsButton.setVisible(visible);
    }

    public void setLoading() {
        dpsButton.setEnabled(false);
        dpsButton.setText("Loading...");
    }

    public void setActive() {
        dpsButton.setEnabled(true);
        dpsButton.setText(dpsButtonText);
    }
}
