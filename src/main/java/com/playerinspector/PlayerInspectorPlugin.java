package com.playerinspector;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;

@Slf4j
@PluginDescriptor(
        name = "Player Inspector",
        description = "Inspect other players' equipment and stats",
        tags = {"players", "equipment", "gear", "inspect", "stats", "skills"}
)
public class PlayerInspectorPlugin extends Plugin {
    @Inject
    private PlayerInspectorConfig config;

    @Override
    protected void startUp() throws Exception {
        log.debug("Player Inspector started!");
    }

    @Override
    protected void shutDown() throws Exception {
        log.debug("Player Inspector stopped!");
    }

    @Provides
    PlayerInspectorConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(PlayerInspectorConfig.class);
    }
}
