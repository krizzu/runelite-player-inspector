package com.playerinspector.panel.layout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DpsSectionLayout extends JPanel {

    public enum StatsSource {
        LocalPlayer("My stats"),
        Maxed("Maxed stats");

        private final String label;

        StatsSource(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final String dpsButtonText = "Open DPS calculator";
    private final JButton dpsButton = new JButton(dpsButtonText);
    private final JComboBox<StatsSource> dropdown = new JComboBox<>(StatsSource.values());

    // false while logged out - setActive() must not re-enable the button then
    private boolean interactive = true;

    public DpsSectionLayout(boolean visible) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(createSelectRow());
        add(Box.createVerticalStrut(5));
        add(createButtonRow());

        setVisible(visible);
    }

    // set one listener
    public void setActionListener(ActionListener act) {
        for (ActionListener l : dpsButton.getActionListeners()) {
            dpsButton.removeActionListener(l);
        }
        dpsButton.addActionListener(act);
    }

    public StatsSource getStatsSource() {
        return (StatsSource) dropdown.getSelectedItem();
    }

    public void setVisibility(boolean visible) {
        setVisible(visible);
    }

    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
        dropdown.setEnabled(interactive);
        dpsButton.setEnabled(interactive);
    }

    public void setLoading() {
        dpsButton.setEnabled(false);
        dpsButton.setText("Loading...");
    }

    public void setActive() {
        dpsButton.setEnabled(interactive);
        dpsButton.setText(dpsButtonText);
    }

    private JPanel createSelectRow() {
        JPanel container = new JPanel(new BorderLayout(5, 0));
        container.add(new JLabel("Select:"), BorderLayout.WEST);
        container.add(dropdown, BorderLayout.CENTER);
        return capHeight(container);
    }

    private JPanel createButtonRow() {
        JPanel container = new JPanel(new BorderLayout(0, 0));
        container.add(dpsButton, BorderLayout.CENTER);
        return capHeight(container);
    }

    // cap the height so we can have nice aligmnent
    private JPanel capHeight(JPanel row) {
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }
}
