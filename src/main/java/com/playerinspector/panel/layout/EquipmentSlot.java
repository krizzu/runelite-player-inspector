package com.playerinspector.panel.layout;

import com.playerinspector.utils.*;
import net.runelite.api.ItemComposition;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;


public class EquipmentSlot extends JPanel {

    public static enum Type {
        Head, Cape, Amulet, Ammo, RightHand, Body, LeftHand, Legs, Gloves, Boots, Ring
    }

    private final Dimension preferredDims = new Dimension(36, 36);
    public Type type;
    private JLabel icon = createLabel();

    private final SpriteManager sprites;
    private final ItemManager items;
    private final Threads threads;

    // an icon of a slot itself, when empty. Buffered after init.
    private BufferedImage emptyImage;

    public EquipmentSlot(Type type, SpriteManager sprites, ItemManager items, Threads threads) {
        this.type = type;
        this.sprites = sprites;
        this.items = items;
        this.threads = threads;

        setLayout(new OverlayLayout(this));
        setPreferredSize(preferredDims);
        setOpaque(false);

        // add icon first, so it is shown on top of the background
        add(icon);
        loadSprite(getSpriteIndex(), bufferedImage -> {
            // cache empty item
            emptyImage = bufferedImage;
            icon.setIcon(new ImageIcon(bufferedImage));
            centerLabel(icon);
        });

        JLabel background = new JLabel();
        sprites.addSpriteTo(background, 170, 0);
        centerLabel(background);
        add(background);
    }

    public void setEquipmentItem(ItemComposition item) {
        AsyncBufferedImage img = items.getImage(item.getId());

        img.onLoaded(() -> {
            img.addTo(icon);
            centerLabel(icon);
            // todo: dunno why, but eq sprites are not centered - by 4px.
            icon.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        });

        threads.onClient(() -> {
            setPopupMenuForItem(item);
        });

        threads.onClient(() -> {
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
        icon.setBorder(null);
        setPopupMenuForItem(null);
    }

    private void setPopupMenuForItem(ItemComposition item) {
        if (item == null) {
            setComponentPopupMenu(null);
            return;
        }

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem wikiItem = new JMenuItem("Wiki");

        wikiItem.addActionListener(e -> {
            LinkBrowser.browse(WikiUrlBuilder.buildForItem(item).toString());
        });

        popupMenu.add(wikiItem);

        setComponentPopupMenu(popupMenu);
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
                threads.onEdt(() -> consumer.accept(img)));
    }

    private JLabel createLabel() {
        JLabel label = new JLabel();
        return centerLabel(label);
    }

    private JLabel centerLabel(JLabel label) {
        label.setPreferredSize(preferredDims);
        label.setMinimumSize(preferredDims);
        label.setMaximumSize(preferredDims);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        return label;
    }
}