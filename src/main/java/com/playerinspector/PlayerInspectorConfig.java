package com.playerinspector;

import net.runelite.client.config.*;

@ConfigGroup("player-inspector")
public interface PlayerInspectorConfig extends Config {

    @ConfigItem(
            keyName = "wikiSelfDpsButton",
            name = "Wiki DPS calculator",
            description = "Adds a button that opens the equipment in the OSRS Wiki DPS calculator",
            warning = "This feature submits your IP address to a 3rd-party server not controlled or verified by RuneLite developers"
    )
    default boolean wikiSelfDpsButton() {
        return false;
    }
}
