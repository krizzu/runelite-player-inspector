package com.playerinspector.utils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.runelite.client.callback.ClientThread;

import javax.swing.*;

@Singleton
public class Threads {
    @Inject
    ClientThread clientThread;

    public void onClient(Runnable r) {
        clientThread.invoke(r);
    }

    public void onEdt(Runnable r) {
        SwingUtilities.invokeLater(r);
    }
}
