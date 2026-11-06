package com.playerinspector.panel.layout;

import lombok.Value;

import javax.annotation.Nullable;

@Value
public class PlayerSelectedEvent {
    @Nullable
    String playerName;
}
