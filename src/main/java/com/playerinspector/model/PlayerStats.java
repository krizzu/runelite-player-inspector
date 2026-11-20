package com.playerinspector.model;

import lombok.*;
import net.runelite.api.*;

import javax.annotation.*;

@Value
@Builder
public class PlayerStats {

    public static PlayerStats maxed() {
        return new PlayerStats(
                99,
                99, 99, 99, 99, 99, 99
        );
    }

    public static PlayerStats fromClient(Client client) {
        return new PlayerStats(
                client.getRealSkillLevel(Skill.ATTACK),
                client.getRealSkillLevel(Skill.STRENGTH),
                client.getRealSkillLevel(Skill.DEFENCE),
                client.getRealSkillLevel(Skill.RANGED),
                client.getRealSkillLevel(Skill.MAGIC),
                client.getRealSkillLevel(Skill.PRAYER),
                client.getRealSkillLevel(Skill.HITPOINTS)
        );
    }

    @Nullable
    Integer atk;

    @Nullable
    Integer str;

    @Nullable
    Integer def;

    @Nullable
    Integer ranged;

    @Nullable
    Integer magic;

    @Nullable
    Integer prayer;

    @Nullable
    Integer hp;
}
