package com.playerinspector.utils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.runelite.client.callback.ClientThread;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

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

    public void onEdtAwait(Runnable r) throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(r);
    }

}
