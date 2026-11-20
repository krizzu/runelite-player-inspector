package com.playerinspector.model;

import lombok.*;

@Value
public class CurrentPlayer {
    String name;
    PlayerEquipment eq;
    PlayerStats stats;
}
