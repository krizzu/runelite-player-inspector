package com.playerinspector.panel;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.playerinspector.panel.layout.EquipmentSlot;
import com.playerinspector.panel.layout.PlayerInspectorLayout;
import com.playerinspector.tracker.NearbyPlayerTracker;
import com.playerinspector.tracker.NearbyPlayerTrackerUpdate;
import com.playerinspector.utils.Threads;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemComposition;
import net.runelite.api.Player;
import net.runelite.api.PlayerComposition;
import net.runelite.api.kit.KitType;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.PluginPanel;


@Slf4j
@Singleton
public class PlayerInspectorPanel extends PluginPanel {
    private final ClientToolbar toolbar;
    private final ItemManager itemManager;
    private final PlayerInspectorLayout layout;
    private final Threads threads;
    private final NearbyPlayerTracker tracker;
    private final EventBus bus;


    private NavigationButton navButton;

    @Inject
    public PlayerInspectorPanel(ClientToolbar toolbar, ItemManager itemManager, PlayerInspectorLayout layout, Threads threads, NearbyPlayerTracker tracker, EventBus bus) {
        this.toolbar = toolbar;
        this.itemManager = itemManager;
        this.layout = layout;
        this.threads = threads;
        this.tracker = tracker;
        this.bus = bus;
    }


    public void init() {
        bus.register(this);
        navButton = NavButtonFactory.create(this);
        toolbar.addNavigation(navButton);
        layout.init(this);
    }

    public void deinit() {
        bus.unregister(this);
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
        threads.onEdt(() -> {
            toolbar.openPanel(navButton);
            layout.setPlayers(tracker.getPlayers());
            if (player != null) {
                layout.setSelected(player.getName());
            }
        });

        threads.onClient(() ->
                showEquipment(player)
        );
    }

    @Subscribe
    private void onNearbyPlayerTrackerUpdate(NearbyPlayerTrackerUpdate update) {
        threads.onEdt(() -> {
            layout.setPlayers(update.getPlayers());
        });
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

