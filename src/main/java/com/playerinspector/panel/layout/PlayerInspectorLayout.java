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

        panel.add(Box.createVerticalStrut(10));

        panel.add(createEquipmentPanel());
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

    private JPanel createEquipmentPanel() {
        JPanel container = new JPanel();
        container.setLayout(new GridBagLayout());

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            JPanel slotPanel = createEquipmentSlot(slot);
            GridBagConstraints constraints = createEquipmentBoxConstraint(slot);
            container.add(slotPanel, constraints);
        }


        return container;
    }

    private JPanel createEquipmentSlot(EquipmentSlot slot) {
        JPanel slotPanel = new JPanel();
        slotPanel.setPreferredSize(new Dimension(32, 32));
        slotPanel.setBackground(new Color(77, 77, 73));
        slotPanel.setOpaque(true);
        slotPanel.setName(slot.name());
        return slotPanel;
    }

    private GridBagConstraints createEquipmentBoxConstraint(EquipmentSlot slot) {
        GridBagConstraints c = new GridBagConstraints();
        int x = -1;
        int y = -1;

        switch (slot) {
            case Head:
                x = 1;
                y = 0;
                break;
            case Cape:
                x = 0;
                y = 1;
                break;
            case Amulet:
                x = 1;
                y = 1;
                break;
            case Ammo:
                x = 2;
                y = 1;
                break;
            case RightHand:
                x = 0;
                y = 2;
                break;
            case Body:
                x = 1;
                y = 2;
                break;
            case LeftHand:
                x = 2;
                y = 2;
                break;
            case Leg:
                x = 1;
                y = 3;
                break;
            case Glove:
                x = 0;
                y = 4;
                break;
            case Boots:
                x = 1;
                y = 4;
                break;
            case Ring:
                x = 2;
                y = 4;
                break;
            default:
                throw new IllegalArgumentException("Unmapped equipment slot: " + slot);
        }

        c.gridx = x;
        c.gridy = y;
        c.insets = new Insets(3, 3, 3, 3);
        return c;
    }


    private enum EquipmentSlot {
        Head, Cape, Amulet, Ammo, RightHand, Body, LeftHand, Leg, Glove, Boots, Ring
    }
}
