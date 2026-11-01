package com.playerinspector.panel.layout;

import net.runelite.client.game.SpriteManager;
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

    private final EquipmentSlot head;
    private final EquipmentSlot cape;
    private final EquipmentSlot amulet;
    private final EquipmentSlot ammo;
    private final EquipmentSlot rightHand;
    private final EquipmentSlot body;
    private final EquipmentSlot leftHand;
    private final EquipmentSlot legs;
    private final EquipmentSlot gloves;
    private final EquipmentSlot boots;
    private final EquipmentSlot ring;

    public PlayerInspectorLayout(SpriteManager spriteManager) {
        this.head = new EquipmentSlot(EquipmentSlot.Type.Head, spriteManager);
        this.cape = new EquipmentSlot(EquipmentSlot.Type.Cape, spriteManager);
        this.amulet = new EquipmentSlot(EquipmentSlot.Type.Amulet, spriteManager);
        this.ammo = new EquipmentSlot(EquipmentSlot.Type.Ammo, spriteManager);
        this.rightHand = new EquipmentSlot(EquipmentSlot.Type.RightHand, spriteManager);
        this.body = new EquipmentSlot(EquipmentSlot.Type.Body, spriteManager);
        this.leftHand = new EquipmentSlot(EquipmentSlot.Type.LeftHand, spriteManager);
        this.legs = new EquipmentSlot(EquipmentSlot.Type.Legs, spriteManager);
        this.gloves = new EquipmentSlot(EquipmentSlot.Type.Gloves, spriteManager);
        this.boots = new EquipmentSlot(EquipmentSlot.Type.Boots, spriteManager);
        this.ring = new EquipmentSlot(EquipmentSlot.Type.Ring, spriteManager);
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

        for (EquipmentSlot.Type slot : EquipmentSlot.Type.values()) {
            EquipmentSlot eq = selectEquipmentSlot(slot);
            GridBagConstraints constraints = createEquipmentBoxConstraint(slot);
            container.add(eq, constraints);
        }

        return container;
    }

    private EquipmentSlot selectEquipmentSlot(EquipmentSlot.Type slot) {
        switch (slot) {
            case Head:
                return head;
            case Cape:
                return cape;
            case Amulet:
                return amulet;
            case Ammo:
                return ammo;
            case RightHand:
                return rightHand;
            case Body:
                return body;
            case LeftHand:
                return leftHand;
            case Legs:
                return legs;
            case Gloves:
                return gloves;
            case Boots:
                return boots;
            case Ring:
                return ring;
            default:
                throw new IllegalArgumentException("Unknown equipment slot: " + slot);
        }
    }

    private GridBagConstraints createEquipmentBoxConstraint(EquipmentSlot.Type slot) {
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
            case Legs:
                x = 1;
                y = 3;
                break;
            case Gloves:
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
                throw new IllegalArgumentException("Unknown equipment slot: " + slot);
        }

        c.gridx = x;
        c.gridy = y;
        c.insets = new Insets(4, 4, 4, 4);
        return c;
    }
}
