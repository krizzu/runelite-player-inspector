package com.playerinspector.panel;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.playerinspector.panel.layout.EquipmentSlot;
import com.playerinspector.panel.layout.PlayerInspectorLayout;
import com.playerinspector.panel.layout.PlayerSelectedEvent;
import com.playerinspector.tracker.NearbyPlayerTracker;
import com.playerinspector.tracker.NearbyPlayerTrackerUpdate;
import com.playerinspector.utils.Threads;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
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
public class PlayerInspectorPanel extends PluginPanel {
    private final ClientToolbar toolbar;
    private final ItemManager itemManager;
    private final PlayerInspectorLayout layout;
    private final Threads threads;
    private final NearbyPlayerTracker tracker;
    private final EventBus bus;
    private final Client client;

    private NavigationButton navButton;

    @Inject
    public PlayerInspectorPanel(ClientToolbar toolbar, ItemManager itemManager, Client client, PlayerInspectorLayout layout, Threads threads, NearbyPlayerTracker tracker, EventBus bus) {
        this.toolbar = toolbar;
        this.itemManager = itemManager;
        this.layout = layout;
        this.threads = threads;
        this.tracker = tracker;
        this.bus = bus;
        this.client = client;
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
        Player localPlayer = client.getLocalPlayer();
        boolean isLocal = localPlayer == player;

        if (isLocal) {
            showLocalPlayerEquipment();
        } else {
            showPlayerEquipment(player);
        }
    }

    public void openPanel(Player player) {
        threads.onEdt(() -> {
            toolbar.openPanel(navButton);
            layout.updateAvailablePlayerList(tracker.getPlayers());
            if (player != null) {
                layout.setSelected(player.getName());
            }
        });
    }


    // note: think about supporting Stackable items (arrows, etc) for better display
    private void showLocalPlayerEquipment() {
        ItemContainer worn = client.getItemContainer(InventoryID.WORN);

        if (worn == null) {
            // clear just in case
            clearAllSlots();
            return;
        }

        long total = 0;
        for (EquipmentInventorySlot inv : EquipmentInventorySlot.values()) {
            EquipmentSlot.Type slotType = equipmentInventoryIdToSlot(inv);
            if (slotType == null) {
                continue;
            }

            Item item = worn.getItem(inv.getSlotIdx());
            if (item == null) {
                layout.clearEquipmentItem(slotType);
            } else {
                ItemComposition comp = itemManager.getItemComposition(item.getId());
                layout.setEquipmentItem(slotType, comp);
                total += itemManager.getItemPrice(item.getId());
            }
        }

        layout.setTotalGeValue(total);
    }

    private void showPlayerEquipment(Player player) {
        PlayerComposition comp = player.getPlayerComposition();

        // clear local player's ring and arrows, non-existing for other players
        layout.clearEquipmentItem(EquipmentSlot.Type.Ammo);
        layout.clearEquipmentItem(EquipmentSlot.Type.Ring);

        long total = 0;
        for (KitType kitType : KitType.values()) {
            EquipmentSlot.Type slotType = kitTypeToSlot(kitType);
            if (slotType == null) {
                continue;
            }

            int eqId = comp.getEquipmentId(kitType);
            boolean isItem = eqId > 0;

            if (!isItem) {
                layout.clearEquipmentItem(slotType);
            } else {
                ItemComposition item = itemManager.getItemComposition(eqId);
                total += itemManager.getItemPrice(eqId);
                layout.setEquipmentItem(slotType, item);
            }
        }

        layout.setTotalGeValue(total);
    }

    private void clearAllSlots() {
        for (EquipmentSlot.Type type : EquipmentSlot.Type.values()) {
            layout.clearEquipmentItem(type);
        }
        layout.setTotalGeValue(0);
    }

    @Subscribe
    private void onPlayerSelectedEvent(PlayerSelectedEvent selected) {
        String name = selected.getPlayerName();
        if (name == null) {
            return;
        }

        threads.onClient(() -> {
            Player player = findPlayerByName(name);
            if (player != null) {
                showEquipment(player);
            }
        });

    }

    @Subscribe
    private void onNearbyPlayerTrackerUpdate(NearbyPlayerTrackerUpdate update) {
        threads.onEdt(() -> {
            layout.updateAvailablePlayerList(update.getPlayers());
        });
    }

    private Player findPlayerByName(String playerName) {
        if (client.getGameState() != GameState.LOGGED_IN) {
            return null;
        }

        Player local = client.getLocalPlayer();
        if (local == null) {
            return null;
        }

        if (playerName.equals(local.getName())) {
            return local;
        }

        for (Player player : client.getTopLevelWorldView().players()) {
            if (playerName.equals(player.getName())) {
                return player;
            }
        }

        return null;
    }

    private EquipmentSlot.Type equipmentInventoryIdToSlot(EquipmentInventorySlot inv) {
        switch (inv) {
            case HEAD:
                return EquipmentSlot.Type.Head;
            case CAPE:
                return EquipmentSlot.Type.Cape;
            case AMULET:
                return EquipmentSlot.Type.Amulet;
            case WEAPON:
                return EquipmentSlot.Type.RightHand;
            case BODY:
                return EquipmentSlot.Type.Body;
            case SHIELD:
                return EquipmentSlot.Type.LeftHand;
            case LEGS:
                return EquipmentSlot.Type.Legs;
            case GLOVES:
                return EquipmentSlot.Type.Gloves;
            case BOOTS:
                return EquipmentSlot.Type.Boots;
            case AMMO:
                return EquipmentSlot.Type.Ammo;
            case RING:
                return EquipmentSlot.Type.Ring;
            default:
                return null;
        }
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

