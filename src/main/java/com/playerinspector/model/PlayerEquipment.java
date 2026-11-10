package com.playerinspector.model;

import lombok.Builder;
import lombok.Value;

import javax.annotation.*;

@Value
@Builder
public class PlayerEquipment {
    @Nullable
    Integer head;

    @Nullable
    Integer cape;

    @Nullable
    Integer amulet;

    @Nullable
    Integer ammo;

    @Nullable
    Integer rightHand;

    @Nullable
    Integer body;

    @Nullable
    Integer leftHand;

    @Nullable
    Integer legs;

    @Nullable
    Integer gloves;

    @Nullable
    Integer boots;

    @Nullable
    Integer ring;
}
