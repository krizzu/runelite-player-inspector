package com.playerinspector.panel.layout;

import net.runelite.client.game.SpriteManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;


public class EquipmentSlot extends JPanel {

    public static enum Type {
        Head, Cape, Amulet, Ammo, RightHand, Body, LeftHand, Legs, Gloves, Boots, Ring
    }

    public Type type;
    public JPanel panel;

    private SpriteManager sprites;

    public EquipmentSlot(Type type, SpriteManager sprites) {
        this.type = type;
        this.sprites = sprites;

        Dimension dims = new Dimension(36, 36);

        setLayout(new OverlayLayout(this));
        setPreferredSize(dims);
        setOpaque(false);

        // add icon first, so it is shown on top of the background
        JLabel icon = new JLabel();
        icon.setHorizontalAlignment(SwingConstants.RIGHT);
        sprites.addSpriteTo(icon, getSpriteIndex(), 0);
        add(icon);

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

}