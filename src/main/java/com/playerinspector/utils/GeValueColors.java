package com.playerinspector.utils;

import java.awt.*;

public class GeValueColors {

    private static final Color defaultColor = Color.decode("#FFFFFF");
    private static final Color low = Color.decode("#4EF5FF");
    private static final Color medium = Color.decode("#99FF99");
    private static final Color high = Color.decode("#FF9600");
    private static final Color insane = Color.decode("#FF66B2");

    private static final long lowAmount = 20_000;
    private static final long mediumAmount = 100_000;
    private static final long highAmount = 1_000_000;
    private static final long insaneAmount = 10_000_000;


    public static Color getColorForValue(long value) {
        if (value >= insaneAmount) {
            return insane;
        } else if (value >= highAmount) {
            return high;
        } else if (value >= mediumAmount) {
            return medium;
        } else if (value >= lowAmount) {
            return low;
        } else {
            return defaultColor;
        }
    }

}
