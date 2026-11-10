package com.playerinspector.utils;

import com.playerinspector.model.*;
import com.playerinspector.panel.layout.*;

import javax.annotation.*;

public class PlayerEquipmentUtils {
    public static PlayerEquipment.PlayerEquipmentBuilder setEquipment(PlayerEquipment.PlayerEquipmentBuilder eq, EquipmentSlot.Type slot, @Nullable Integer itemId) {
        switch (slot) {
            case Head:
                return eq.head(itemId);
            case Cape:
                return eq.cape(itemId);
            case Amulet:
                return eq.amulet(itemId);
            case Ammo:
                return eq.ammo(itemId);
            case RightHand:
                return eq.rightHand(itemId);
            case Body:
                return eq.body(itemId);
            case LeftHand:
                return eq.leftHand(itemId);
            case Legs:
                return eq.legs(itemId);
            case Gloves:
                return eq.gloves(itemId);
            case Boots:
                return eq.boots(itemId);
            case Ring:
                return eq.ring(itemId);
            default:
                throw new IllegalArgumentException("Unknown slot type: " + slot);
        }
    }
}
