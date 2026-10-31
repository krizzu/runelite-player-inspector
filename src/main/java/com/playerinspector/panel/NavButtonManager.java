package com.playerinspector.panel;

import com.playerinspector.Constants;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.inject.Singleton;
import java.awt.image.BufferedImage;

@Singleton
public class NavButtonManager {

    private final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "magnifying_glass.png");

    public NavigationButton create(PlayerInspectorPanel panel) {
        return NavigationButton.builder()
                .tooltip(Constants.PANEL_ICON_LABEL)
                .icon(icon)
                .priority(5)
                .panel(panel)
                .build();
    }

}
