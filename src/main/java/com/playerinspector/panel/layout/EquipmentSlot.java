package com.playerinspector.panel.layout;

import com.playerinspector.utils.*;
import net.runelite.api.ItemComposition;
import net.runelite.api.gameval.SpriteID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;


public class EquipmentSlot extends JPanel {

    public enum Type {
        Head, Cape, Amulet, Ammo, RightHand, Body, LeftHand, Legs, Gloves, Boots, Ring
    }

    private final Dimension preferredDims = new Dimension(36, 36);
    private final Type type;
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
        sprites.addSpriteTo(background, SpriteID.Miscgraphics.EQUIPMENT_SLOT_TILE, 0);
        centerLabel(background);
        add(background);
    }

    public void setEquipmentItem(ItemComposition item) {
        AsyncBufferedImage img = items.getImage(item.getId());

        // note: onLoaded fires on the client thread, swing needs edt
        img.onLoaded(() -> threads.onEdt(() -> {
            img.addTo(icon);
            centerLabel(icon);
            // todo: dunno why, but eq sprites are not centered - offset by 4px.
            icon.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        }));

        setPopupMenuForItem(item);

        // prices are read off the client
        threads.onClient(() -> {
            long ge = items.getItemPrice(item.getId());
            long ha = item.getHaPrice();
            String gePrice = QuantityFormatter.formatNumber(ge);
            String haPrice = QuantityFormatter.formatNumber(ha);

            String tooltip = new StringBuilder()
                    .append("<html>")
                    .append(item.getName()).append("<br>")
                    .append("GE: ").append("<font color='" + ColorUtils.getColorForValueHex(ge) + "'>").append(gePrice).append("gp</font>").append("<br>")
                    .append("HA: ").append("<font color='" + ColorUtils.getColorForValueHex(ha) + "'>").append(haPrice).append("gp</font>").append("<br>")
                    .append("</html>")
                    .toString();

            threads.onEdt(() -> icon.setToolTipText(tooltip));
        });
    }

    public void clearEquipmentItem() {
        icon.setToolTipText("");
        icon.setBorder(null);
        setPopupMenuForItem(null);
        if (emptyImage != null) {
            icon.setIcon(new ImageIcon(emptyImage));
        }
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

    private int getSpriteIndex() {
        switch (this.type) {
            case Head:
                return SpriteID.Wornicons.HEAD;
            case Cape:
                return SpriteID.Wornicons.CAPE;
            case Amulet:
                return SpriteID.Wornicons.NECK;
            case Ammo:
                return SpriteID.Wornicons.AMMUNITION;
            case RightHand:
                return SpriteID.Wornicons.WEAPON;
            case Body:
                return SpriteID.Wornicons.TORSO;
            case LeftHand:
                return SpriteID.Wornicons.SHIELD;
            case Legs:
                return SpriteID.Wornicons.LEGS;
            case Gloves:
                return SpriteID.Wornicons.HANDS;
            case Boots:
                return SpriteID.Wornicons.FEET;
            case Ring:
                return SpriteID.Wornicons.RING;
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