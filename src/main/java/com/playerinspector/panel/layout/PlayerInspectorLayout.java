package com.playerinspector.panel.layout;

import com.google.inject.Inject;
import net.runelite.api.ItemComposition;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;


public class PlayerInspectorLayout {
    private final JComboBox<String> playerSelector = new JComboBox<>();

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

    @Inject
    public PlayerInspectorLayout(EquipmentSlotFactory equipmentSlotFactory) {
        this.head = equipmentSlotFactory.create(EquipmentSlot.Type.Head);
        this.cape = equipmentSlotFactory.create(EquipmentSlot.Type.Cape);
        this.amulet = equipmentSlotFactory.create(EquipmentSlot.Type.Amulet);
        this.ammo = equipmentSlotFactory.create(EquipmentSlot.Type.Ammo);
        this.rightHand = equipmentSlotFactory.create(EquipmentSlot.Type.RightHand);
        this.body = equipmentSlotFactory.create(EquipmentSlot.Type.Body);
        this.leftHand = equipmentSlotFactory.create(EquipmentSlot.Type.LeftHand);
        this.legs = equipmentSlotFactory.create(EquipmentSlot.Type.Legs);
        this.gloves = equipmentSlotFactory.create(EquipmentSlot.Type.Gloves);
        this.boots = equipmentSlotFactory.create(EquipmentSlot.Type.Boots);
        this.ring = equipmentSlotFactory.create(EquipmentSlot.Type.Ring);
    }

    public void init(PluginPanel panel) {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        // padding of 10px
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(createSelectPanel());

        // padding
        panel.add(Box.createVerticalStrut(10));

        panel.add(createEquipmentPanel());

        // todo: add buttons that links to DPS page
    }


    public void setSelected(String playerName) {
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) playerSelector.getModel();
        int index = model.getIndexOf(playerName);
        if (index >= 0) {
            playerSelector.setSelectedItem(playerName);
        }
    }

    public void setPlayers(ArrayList<String> players) {
        String[] playersArray = new String[players.size()];
        playersArray = players.toArray(playersArray);
        String selected = (String) playerSelector.getSelectedItem();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(playersArray);

        playerSelector.setModel(model);
        if (players.contains(selected)) {
            playerSelector.setSelectedItem(selected);
        }
    }

    public void setEquipmentItem(EquipmentSlot.Type slot, ItemComposition item) {
        EquipmentSlot eq = selectEquipmentSlot(slot);
        SwingUtilities.invokeLater(() -> {
            eq.setEquipmentItem(item);
        });
    }

    public void clearEquipmentItem(EquipmentSlot.Type slot) {
        EquipmentSlot eq = selectEquipmentSlot(slot);
        SwingUtilities.invokeLater(eq::clearEquipmentItem);
    }

    private JPanel createSelectPanel() {
        JPanel container = new JPanel();
        container.setLayout(new BorderLayout(0, 0));
        container.add(playerSelector, BorderLayout.CENTER);
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
