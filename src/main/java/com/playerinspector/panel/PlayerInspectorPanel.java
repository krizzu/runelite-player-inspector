package com.playerinspector.panel;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.playerinspector.panel.layout.EquipmentSlot;
import com.playerinspector.panel.layout.PlayerInspectorLayout;
import com.playerinspector.utils.Threads;
import net.runelite.api.ItemComposition;
import net.runelite.api.Player;
import net.runelite.api.PlayerComposition;
import net.runelite.api.kit.KitType;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;


public class PlayerInspectorPanel extends PluginPanel {

    @Inject
    private ClientToolbar toolbar;

    @Inject
    private NavButtonManager navManager;

    @Inject
    private SpriteManager spriteManager;

    @Inject
    private ItemManager itemManager;

    @Inject
    private PlayerInspectorLayout layout;

    @Inject
    private Threads threads;

    private NavigationButton navButton;


    public PlayerInspectorPanel() {
    }

    public void init() {
        navButton = navManager.create(this);
        layout.init(this);
        toolbar.addNavigation(navButton);
    }

    public void deinit() {
        toolbar.removeNavigation(navButton);
    }

    public void showEquipment(Player player) {
        PlayerComposition comp = player.getPlayerComposition();
        for (KitType kitType : KitType.values()) {
            EquipmentSlot.Type slotType = kitTypeToSlot(kitType);
            // todo: for local player, need to map ring and ammo!
            if (slotType == null) {
                continue;
            }

            int eqId = comp.getEquipmentId(kitType);
            ItemComposition item = itemManager.getItemComposition(eqId);

            if (item == null) {
                continue;
            }

            boolean isItem = eqId > 0;
            if (!isItem) {
                layout.clearEquipmentItem(slotType);
            } else {
                layout.setEquipmentItem(slotType, item);
            }
        }

    }

    public void openPanel(Player player) {
        try {
            threads.onEdtAwait(() -> {
                toolbar.openPanel(navButton);
                if (player != null) {
                    layout.setSelected(player.getName());
                }
            });
            // needs to run on client thread
        } catch (InterruptedException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        showEquipment(player);
    }

    private EquipmentSlot.Type kitTypeToSlot(KitType kit) {
        switch (kit) {
            case HEAD:
                return EquipmentSlot.Type.Head;
            case CAPE:
                return EquipmentSlot.Type.Cape;
            case AMULET:
                return EquipmentSlot.Type.Amulet;
            case WEAPON:
                return EquipmentSlot.Type.RightHand;
            case TORSO:
                return EquipmentSlot.Type.Body;
            case SHIELD:
                return EquipmentSlot.Type.LeftHand;
            case LEGS:
                return EquipmentSlot.Type.Legs;
            case HANDS:
                return EquipmentSlot.Type.Gloves;
            case BOOTS:
                return EquipmentSlot.Type.Boots;
            case ARMS:
            case HAIR:
            case JAW:
                // pure cosmetic kit pieces, no corresponding equipment slot
                return null;
            default:
                throw new IllegalArgumentException("Unknown KitType: " + kit);
        }
    }

}

