package com.playerinspector.panel.layout;

import net.runelite.client.game.SpriteManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;


public class EquipmentSlot extends JPanel {

    public static enum Type {
        Head, Cape, Amulet, Ammo, RightHand, Body, LeftHand, Legs, Gloves, Boots, Ring
    }

    public Type type;
    public JPanel panel;

    private Dimension preferredDims = new Dimension(36, 36);
    private SpriteManager sprites;

    public EquipmentSlot(Type type, SpriteManager sprites) {
        this.type = type;
        this.sprites = sprites;

        setLayout(new OverlayLayout(this));
        setPreferredSize(preferredDims);
        setOpaque(false);

        // add icon first, so it is shown on top of the background
        JLabel icon = new JLabel();
        add(icon);
        loadSprite(getSpriteIndex(), bufferedImage -> {
            // centers the icon
            icon.setIcon(new ImageIcon(bufferedImage));
            icon.setPreferredSize(preferredDims);
            icon.setMinimumSize(preferredDims);
            icon.setMaximumSize(preferredDims);
            icon.setHorizontalAlignment(SwingConstants.CENTER);
            icon.setVerticalAlignment(SwingConstants.CENTER);
        });


        JLabel background = new JLabel();
        sprites.addSpriteTo(background, 170, 0);
        add(background);
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
    public void loadSprite(int index, Consumer<BufferedImage> consumer) {
        this.sprites.getSpriteAsync(index, 0, img ->
        {
            SwingUtilities.invokeLater(() ->
            {
                consumer.accept(img);
            });
        });
    }
}