package com.playerinspector.panel.layout;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;

@Singleton
public class EquipmentSlotFactory {
    private final SpriteManager sprites;
    private final ItemManager items;
    private final ClientThread clientThread;

    @Inject
    EquipmentSlotFactory(SpriteManager sprites, ItemManager items, ClientThread clientThread) {
        this.sprites = sprites;
        this.items = items;
        this.clientThread = clientThread;
    }

    public EquipmentSlot create(EquipmentSlot.Type type) {
        return new EquipmentSlot(type, sprites, items, clientThread);
    }
}
