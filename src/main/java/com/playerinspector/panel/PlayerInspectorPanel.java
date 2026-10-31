package com.playerinspector.panel;

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

    private NavigationButton navButton;


    public PlayerInspectorPanel() {
        // todo: build ui
    }

    public void init() {
        navButton = navManager.create(this);
        toolbar.addNavigation(navButton);
    }

    public void deinit() {
        toolbar.removeNavigation(navButton);
    }


    public void openPanel() {
        try {
            SwingUtilities.invokeAndWait(() -> toolbar.openPanel(navButton));
        } catch (InterruptedException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
