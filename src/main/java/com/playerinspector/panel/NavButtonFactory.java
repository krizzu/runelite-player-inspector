package com.playerinspector.panel;

import com.google.inject.Singleton;
import com.playerinspector.Constants;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;

@Singleton
public class NavButtonFactory {

    private static final BufferedImage icon = ImageUtil.loadImageResource(NavButtonFactory.class, "worn_equipment.png");

    public static NavigationButton create(PlayerInspectorPanel panel) {
        return NavigationButton.builder()
                .tooltip(Constants.PANEL_ICON_LABEL)
                .icon(icon)
                .priority(5)
                .panel(panel)
                .build();
    }

}
