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

    /**
     * note: needs to run on client thread side
     */
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


    int atk;
    int str;
    int def;
    int ranged;
    int magic;
    int prayer;
    int hp;
}
