package com.playerinspector.panel;

import com.playerinspector.panel.layout.PlayerInspectorLayout;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

@Singleton
public class PlayerInspectorPanel extends PluginPanel {

    @Inject
    private ClientToolbar toolbar;

    @Inject
    private NavButtonManager navManager;

    private PlayerInspectorLayout layout;

    private NavigationButton navButton;

    public PlayerInspectorPanel() {}

    public void init() {
        layout = new PlayerInspectorLayout();
        navButton = navManager.create(this);
        layout.init(this);
        toolbar.addNavigation(navButton);
    }

    public void deinit() {
        toolbar.removeNavigation(navButton);
    }

    public void openPanel() {
        openPanel("");
    }

    public void openPanel(String playerName) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                        toolbar.openPanel(navButton);

                        if (playerName != null && !playerName.isEmpty()) {
                            layout.setSearch(playerName);
                        }
                    }
            );
        } catch (InterruptedException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
