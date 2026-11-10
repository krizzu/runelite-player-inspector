package com.playerinspector.panel.layout;

import com.google.inject.Inject;
import com.playerinspector.utils.*;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemComposition;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.*;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

@Slf4j
public class PlayerInspectorLayout {
    private final DefaultComboBoxModel<String> playerSelectorModel = new DefaultComboBoxModel<>();
    private final JComboBox<String> playerSelector = new JComboBox<>(playerSelectorModel);
    private final JLabel totalGe = new JLabel("-");

    /**
     * cached list of users that meant to be updated, but popup was visible
     */
    private List<String> pendingUpdate = null;

    /**
     * Controls if PlayerSelectedEvent should be fired or not.
     * A "Selected" event coming from Combobox is the one triggering the update event
     * but is being surpressed if manual selection is performed
     */
    private boolean suppressEventPost = false;


    private final EventBus bus;
    private final Threads threads;

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
    public PlayerInspectorLayout(EquipmentSlotFactory equipmentSlotFactory, EventBus bus, Threads threads) {
        this.bus = bus;
        this.threads = threads;

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


        // padding
        panel.add(Box.createVerticalStrut(10));
        panel.add(createGeTotalPanel());


        // setup dps buttons


        setUpPlayerSelectorListeners();
    }

    public void setEquipmentItem(EquipmentSlot.Type slot, ItemComposition item) {
        EquipmentSlot eq = selectEquipmentSlot(slot);
        threads.onEdt(() ->
                eq.setEquipmentItem(item)
        );
    }

    public void clearEquipmentItem(EquipmentSlot.Type slot) {
        EquipmentSlot eq = selectEquipmentSlot(slot);
        threads.onEdt(eq::clearEquipmentItem);
    }

    public void setTotalGeValue(long value) {
        threads.onEdt(() -> {
            if (value <= 0) {
                totalGe.setForeground(Color.GRAY);
                totalGe.setText("-");
                return;
            }

            totalGe.setForeground(ColorUtils.getColorForValue(value));
            totalGe.setText(QuantityFormatter.formatNumber(value) + "gp");
        });
    }

    public void setSelected(String playerName) {
        boolean present = playerName != null && playerSelectorModel.getIndexOf(playerName) >= 0;

        if (present) {
            playerSelector.setSelectedItem(playerName);
        } else {
            playerSelector.setSelectedItem(null);
        }

    }

    public void updateAvailablePlayerList(List<String> players) {
        // do not set players when popup is visible
        if (playerSelector.isPopupVisible()) {
            pendingUpdate = players;
            return;
        }

        updatePlayerModel(players);
    }

    private void updatePlayerModel(List<String> players) {
        // avoid endless loop
        suppressEventPost = true;

        try {
            String selected = (String) playerSelector.getSelectedItem();
            playerSelector.setSelectedItem(null);
            playerSelectorModel.removeAllElements();
            playerSelectorModel.addAll(players);

            // note: do not send event about selection here - this is internal swap of player list
            if (selected != null && players.contains(selected)) {
                playerSelector.setSelectedItem(selected);
            }
        } finally {
            suppressEventPost = false;
        }
    }

    private void setUpPlayerSelectorListeners() {

        // using actionListener instead of addItemListener makes reselecting same player "refresh" it
        playerSelector.addActionListener(e -> {
            if (suppressEventPost) {
                return;
            }
            String selected = (String) playerSelector.getSelectedItem();
            bus.post(new PlayerSelectedEvent(selected));
        });

        playerSelector.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                if (pendingUpdate != null) {
                    List<String> update = pendingUpdate;
                    pendingUpdate = null;

                    threads.onEdt(() ->
                            updatePlayerModel(update)
                    );
                }
            }

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                // noop
            }


            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                // noop
            }
        });
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

    private JPanel createGeTotalPanel() {
        JPanel container = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        container.add(new JLabel(("Total GE:")));
        container.add(totalGe);
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
        int x;
        int y;

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
