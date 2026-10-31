package com.playerinspector.panel.layout;

import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import java.awt.*;

/**
 * todo: in one row, add search + button to search
 *  - button should be in loading state, once loading
 *  - on Enter press, loading state
 *  - when input.length >= 1, show X to clear
 */
public class PlayerInspectorLayout {
    private final JTextField searchInput = new JTextField();
    private final JButton searchButton = new JButton("search");

    public PlayerInspectorLayout() {
    }

    public void init(PluginPanel panel) {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        // padding of 10px
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(createSearchPanel());
    }


    public void setSearch(String text) {
        searchInput.setText(text);
    }

    private JPanel createSearchPanel() {
        JPanel container = new JPanel();
        container.setLayout(new BorderLayout(5, 0));
        container.add(searchInput, BorderLayout.CENTER);
        container.add(searchButton, BorderLayout.EAST);
        return container;
    }
}
