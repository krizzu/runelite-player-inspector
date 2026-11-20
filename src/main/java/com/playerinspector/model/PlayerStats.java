package com.playerinspector.model;

import lombok.*;
import net.runelite.api.*;

@Value
@Builder
public class PlayerStats {

    private static final int MAX_LEVEL = 99;

    public static PlayerStats maxed() {
        return PlayerStats.builder()
                .atk(MAX_LEVEL)
                .str(MAX_LEVEL)
                .def(MAX_LEVEL)
                .ranged(MAX_LEVEL)
                .magic(MAX_LEVEL)
                .prayer(MAX_LEVEL)
                .hp(MAX_LEVEL)
                .build();
    }

    /**
     * note: needs to run on client thread side
     */
    public static PlayerStats fromClient(Client client) {
        return PlayerStats.builder()
                .atk(client.getRealSkillLevel(Skill.ATTACK))
                .str(client.getRealSkillLevel(Skill.STRENGTH))
                .def(client.getRealSkillLevel(Skill.DEFENCE))
                .ranged(client.getRealSkillLevel(Skill.RANGED))
                .magic(client.getRealSkillLevel(Skill.MAGIC))
                .prayer(client.getRealSkillLevel(Skill.PRAYER))
                .hp(client.getRealSkillLevel(Skill.HITPOINTS))
                .build();
    }


    int atk;
    int str;
    int def;
    int ranged;
    int magic;
    int prayer;
    int hp;
}
