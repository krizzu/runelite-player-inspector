package com.playerinspector;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.playerinspector.panel.PlayerInspectorPanel;
import com.playerinspector.tracker.NearbyPlayerTracker;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.Objects;


@Slf4j
@PluginDescriptor(
        name = "Player Inspector",
        description = "Inspect other players' equipment and stats",
        tags = {"players", "equipment", "gear", "inspect", "stats", "skills", "dps"}
)
public class PlayerInspectorPlugin extends Plugin {
    @Inject
    private PlayerInspectorConfig config;

    @Inject
    private MenuManager menuManager;

    @Inject
    private Client client;

    // note: do not inject this here, but in constructor
    private PlayerInspectorPanel panel;

    @Inject
    private NearbyPlayerTracker tracker;


    @Override
    protected void startUp() throws Exception {
        log.info("Player Inspector started!");
        addMenuEntry();
        tracker.init();
        tracker.seed();

        // inject here, so that Swing's EDT thread is used (layout style is applied here)
        panel = injector.getInstance(PlayerInspectorPanel.class);
        panel.init();
    }

    @Override
    protected void shutDown() throws Exception {
        log.info("Player Inspector stopped!");
        removeMenuEntry();
        panel.deinit();
        tracker.deinit();
    }

    @Provides
    PlayerInspectorConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(PlayerInspectorConfig.class);
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked clicked) {
        MenuEntry entry = clicked.getMenuEntry();

        if (!Objects.equals(entry.getOption(), Constants.MENU_ENTRY_LABEL)) {
            return;
        }

        Player target = entry.getPlayer();
        if (target == null) {
            return;
        }

        panel.openPanel(target);
    }


    private void addMenuEntry() {
        if (client == null) {
            return;
        }
        boolean added = false;
        for (String opt : client.getPlayerOptions()) {
            if (Objects.equals(opt, Constants.MENU_ENTRY_LABEL)) {
                added = true;
                break;
            }
        }
        if (!added) {
            menuManager.addPlayerMenuItem(Constants.MENU_ENTRY_LABEL);
            log.info("Menu entry added");
        }
    }

    private void removeMenuEntry() {
        menuManager.removePlayerMenuItem(Constants.MENU_ENTRY_LABEL);
        log.info("Menu entry removed");
    }
}
