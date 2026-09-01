package com.playerinspector;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PlayerInspectorTest {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(PlayerInspectorPlugin.class);
        RuneLite.main(args);
    }
}