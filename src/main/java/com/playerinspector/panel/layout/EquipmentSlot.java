package com.playerinspector.panel.layout;

import net.runelite.api.ItemComposition;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.AsyncBufferedImage;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;


public class EquipmentSlot extends JPanel {

    public static enum Type {
        Head, Cape, Amulet, Ammo, RightHand, Body, LeftHand, Legs, Gloves, Boots, Ring
    }

    public Type type;
    private JLabel icon = new JLabel();

    private Dimension preferredDims = new Dimension(36, 36);
    private SpriteManager sprites;
    private ItemManager items;

    // an icon of a slot itself, when empty. Buffered after init.
    private BufferedImage emptyImage;

    public EquipmentSlot(Type type, SpriteManager sprites, ItemManager items) {
        this.type = type;
        this.sprites = sprites;
        this.items = items;

        setLayout(new OverlayLayout(this));
        setPreferredSize(preferredDims);
        setOpaque(false);

        // add icon first, so it is shown on top of the background
        add(icon);
        loadSprite(getSpriteIndex(), bufferedImage -> {
            // centers the icon
            emptyImage = bufferedImage;
            icon.setIcon(new ImageIcon(bufferedImage));
            icon.setPreferredSize(preferredDims);
            icon.setMinimumSize(preferredDims);
            icon.setMaximumSize(preferredDims);
            icon.setHorizontalAlignment(SwingConstants.CENTER);
            icon.setVerticalAlignment(SwingConstants.CENTER);
        });

        JLabel background = new JLabel();
        sprites.addSpriteTo(background, 170, 0);
        background.setPreferredSize(preferredDims);
        background.setMinimumSize(preferredDims);
        background.setMaximumSize(preferredDims);
        background.setHorizontalAlignment(SwingConstants.CENTER);
        background.setVerticalAlignment(SwingConstants.CENTER);
        add(background);
    }

    public void setEquipmentItem(ItemComposition item) {
        AsyncBufferedImage img = items.getImage(item.getId());

        // todo: items are not centered for some strange reason
        img.onLoaded(() -> {
            icon.setIcon(new ImageIcon(img));
            icon.setPreferredSize(preferredDims);
            icon.setMinimumSize(preferredDims);
            icon.setMaximumSize(preferredDims);
            icon.setHorizontalAlignment(SwingConstants.CENTER);
            icon.setVerticalAlignment(SwingConstants.CENTER);
        });

        // todo: format values to be easier to read
        String text = item.getName() + "\n" + "GE Price: " + item.getPrice() + "\n" + "HA Price: " + item.getHaPrice();

        icon.setToolTipText(text);
    }

    public void clearEquipmentItem() {
        if (emptyImage == null) {
            throw new RuntimeException("Empty image used too soon, should not be null!");
        }

        icon.setIcon(new ImageIcon(emptyImage));
        icon.setToolTipText("");
    }


    // https://abextm.github.io/cache2/#/viewer/sprite
    private int getSpriteIndex() {
        switch (this.type) {
            case Head:
                return 156;
            case Cape:
                return 157;
            case Amulet:
                return 158;
            case Ammo:
                return 166;
            case RightHand:
                return 159;
            case Body:
                return 161;
            case LeftHand:
                return 162;
            case Legs:
                return 163;
            case Gloves:
                return 164;
            case Boots:
                return 165;
            case Ring:
                return 160;
            default:
                throw new RuntimeException("Unknown slot type: " + this.type);
        }
    }

    // copy-paste from addSpriteTo, but allows to handle loaded image differently
    private void loadSprite(int index, Consumer<BufferedImage> consumer) {
        this.sprites.getSpriteAsync(index, 0, img ->
        {
            SwingUtilities.invokeLater(() ->
            {
                consumer.accept(img);
            });
        });
    }
}