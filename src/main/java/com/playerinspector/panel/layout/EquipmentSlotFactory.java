package com.playerinspector.panel.layout;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.playerinspector.utils.Threads;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;

@Singleton
public class EquipmentSlotFactory {
    private final SpriteManager sprites;
    private final ItemManager items;
    private final Threads threads;

    @Inject
    EquipmentSlotFactory(SpriteManager sprites, ItemManager items, Threads threads) {
        this.sprites = sprites;
        this.items = items;
        this.threads = threads;
    }

    public EquipmentSlot create(EquipmentSlot.Type type) {
        return new EquipmentSlot(type, sprites, items, threads);
    }
}
