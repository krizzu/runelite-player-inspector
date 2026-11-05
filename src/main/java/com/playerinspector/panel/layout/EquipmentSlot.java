package com.playerinspector.panel.layout;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.runelite.api.ItemComposition;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.QuantityFormatter;

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
    private ClientThread clientThread;

    // an icon of a slot itself, when empty. Buffered after init.
    private BufferedImage emptyImage;

    public EquipmentSlot(Type type, SpriteManager sprites, ItemManager items, ClientThread clientThread) {
        this.type = type;
        this.sprites = sprites;
        this.items = items;
        this.clientThread = clientThread;

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

    // todo:
    // - format GE and HE prices (number format)
    // - try to center items - they are left aligned again
    // - show total GE value from an item (diff panel)
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


        clientThread.invoke(() -> {
            String gePrice = QuantityFormatter.formatNumber(items.getItemPrice(item.getId()));
            String haPrice = QuantityFormatter.formatNumber(item.getHaPrice());

            String tooltip = new StringBuilder()
                    .append(item.getName())
                    .append("\n")
                    .append("GE: ").append(gePrice)
                    .append("\n")
                    .append("HA: ").append(haPrice)
                    .toString();

            icon.setToolTipText(tooltip);
        });


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