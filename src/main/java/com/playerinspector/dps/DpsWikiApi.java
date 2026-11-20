/*
 * Copyright (c) 2024 LlemonDuck
 * Copyright (c) 2024, Jayden Bailey <jayden@weirdgloop.org>
 * Copyright (c) 2024, Adam <Adam@sigterm.info>
 * Copyright (c) 2026, Krzysztof Borowy <contact@kborowy.com>
 * All rights reserved.
 *
 * The code has been derivied from code written by previous authors ~ Krzysztof Borowy.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.playerinspector.dps;

import com.google.gson.*;
import com.google.inject.*;
import com.playerinspector.model.*;
import com.playerinspector.utils.*;
import lombok.extern.slf4j.*;
import net.runelite.api.*;
import net.runelite.api.gameval.*;
import net.runelite.client.util.*;
import okhttp3.*;

import javax.annotation.*;
import java.io.*;


@Slf4j(topic = "DpsWikiApi")
@Singleton
public class DpsWikiApi {
    private static final String SHORTLINK_ENDPOINT = "https://tools.runescape.wiki/osrs-dps/shortlink";
    private static final String UI_ENDPOINT = "https://tools.runescape.wiki/osrs-dps/";

    @Inject
    Client client;

    @Inject
    OkHttpClient okHttpClient;

    @Inject
    Threads threads;

    @Inject
    Gson gson;


    // note: callback is fired on EDT
    public void fetchShortLink(CurrentPlayer player, DpsWikiApiCallback callback) {

        // run on client, due to [buildShortlinkData] reading data from client
        threads.onClient(() -> {
            try {
                JsonObject jsonBody;
                try {
                    jsonBody = buildShortlinkData(player.getEq());
                } catch (Exception e) {
                    threads.onEdt(() -> {
                        callback.onError("Failed to build url payload: " + e.getLocalizedMessage());
                    });
                    return;
                }

                Request request = new Request.Builder()
                        .url(SHORTLINK_ENDPOINT)
                        .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody.toString()))
                        .build();

                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        log.warn("Failed to create shortlink for DpsWikiApi", e);
                        threads.onEdt(() -> {
                            callback.onError("Failed to call wiki api: " + e.getLocalizedMessage());
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) {
                        try (response) {
                            if (response.isSuccessful() && response.body() != null) {
                                String responseData;
                                try {
                                    ShortlinkResponse resp = gson.fromJson(response.body().charStream(), ShortlinkResponse.class);
                                    responseData = resp.data;
                                } catch (JsonIOException e) {
                                    log.warn("Could not parse response data: " + e.getLocalizedMessage());
                                    threads.onEdt(() -> {
                                        callback.onError("Failed to parse response: " + e.getLocalizedMessage());
                                    });
                                    return;
                                }

                                if (responseData == null) {
                                    log.warn("response data is null");
                                    threads.onEdt(() -> {
                                        callback.onError("Failed to get valid response");
                                    });
                                    return;
                                }

                                String link = UI_ENDPOINT + "?id=" + responseData;
                                threads.onEdt(() -> {
                                    callback.onSuccess(link);
                                });
                            } else {
                                log.warn("Failed to create shortlink for DPS calculator: http status {}", response.code());
                                threads.onEdt(() -> {
                                    callback.onError("Failed to create shortlink for DPS calculator: http status " + response.code());
                                });
                            }
                        }
                    }
                });
            } catch (Exception e) {
                threads.onEdt(() -> {
                    callback.onError("Something went wrong building request: " + e.getLocalizedMessage());
                });
            }
        });
    }

    public interface DpsWikiApiCallback {
        void onError(String errMessage);

        void onSuccess(String url);
    }


    // borrowed from WikiDpsManager.java
    // note: needs to run on client side
    private JsonObject buildShortlinkData(PlayerEquipment eqp) {
        JsonObject j = new JsonObject();

        // Build the player's loadout data
        JsonArray loadouts = new JsonArray();

        JsonObject l = new JsonObject();
        JsonObject eq = new JsonObject();


        eq.add("ammo", createEqJsonObject(eqp.getAmmo()));

        eq.add("body", createEqJsonObject(eqp.getBody()));
        eq.add("cape", createEqJsonObject(eqp.getCape()));
        eq.add("feet", createEqJsonObject(eqp.getBoots()));
        eq.add("hands", createEqJsonObject(eqp.getGloves()));
        eq.add("head", createEqJsonObject(eqp.getHead()));
        eq.add("legs", createEqJsonObject(eqp.getLegs()));
        eq.add("neck", createEqJsonObject(eqp.getAmulet()));
        eq.add("ring", createEqJsonObject(eqp.getRing()));
        eq.add("shield", createEqJsonObject(eqp.getLeftHand()));
        eq.add("weapon", createEqJsonObject(eqp.getRightHand()));
        l.add("equipment", eq);

        JsonObject skills = new JsonObject();
        skills.addProperty("atk", client.getRealSkillLevel(Skill.ATTACK));
        skills.addProperty("def", client.getRealSkillLevel(Skill.DEFENCE));
        skills.addProperty("hp", client.getRealSkillLevel(Skill.HITPOINTS));
        skills.addProperty("magic", client.getRealSkillLevel(Skill.MAGIC));
        skills.addProperty("mining", client.getRealSkillLevel(Skill.MINING));
        skills.addProperty("prayer", client.getRealSkillLevel(Skill.PRAYER));
        skills.addProperty("ranged", client.getRealSkillLevel(Skill.RANGED));
        skills.addProperty("str", client.getRealSkillLevel(Skill.STRENGTH));
        l.add("skills", skills);

        JsonObject buffs = new JsonObject();
        buffs.addProperty("inWilderness", client.getVarbitValue(VarbitID.INSIDE_WILDERNESS) == 1);
        buffs.addProperty("kandarinDiary", client.getVarbitValue(VarbitID.KANDARIN_DIARY_HARD_COMPLETE) == 1);
        buffs.addProperty("onSlayerTask", client.getVarpValue(VarPlayerID.SLAYER_COUNT) > 0);
        buffs.addProperty("chargeSpell", client.getVarpValue(VarPlayerID.MAGEARENA_CHARGE) > 0);
        l.add("buffs", buffs);

        l.addProperty("name", client.getLocalPlayer().getName());

        loadouts.add(l);
        j.add("loadouts", loadouts);

        return j;
    }

    private JsonObject createEqJsonObject(Integer id) {
        if (id == null) {
            return null;
        }

        JsonObject o = new JsonObject();
        o.addProperty("id", id);
        return o;
    }

    private static class ShortlinkResponse {
        String data;
    }
}
