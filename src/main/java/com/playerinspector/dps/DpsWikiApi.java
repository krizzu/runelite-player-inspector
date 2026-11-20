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
    OkHttpClient okHttpClient;

    @Inject
    Threads threads;

    @Inject
    Gson gson;


    // note: callback is fired on EDT
    public void fetchShortLink(CurrentPlayer player, PlayerStats stats, DpsWikiApiCallback callback) {
        Request request;
        try {
            JsonObject jsonBody = buildShortlinkData(player, stats);
            request = new Request.Builder()
                    .url(SHORTLINK_ENDPOINT)
                    .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody.toString()))
                    .build();
        } catch (Exception e) {
            log.warn("Failed to build shortlink request", e);
            notifyError(callback, "Failed to build url payload: " + e.getLocalizedMessage());
            return;
        }

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.warn("Failed to create shortlink for DpsWikiApi", e);
                notifyError(callback, "Failed to call wiki api: " + e.getLocalizedMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (response) {
                    if (!response.isSuccessful() || response.body() == null) {
                        log.warn("Failed to create shortlink for DPS calculator: http status {}", response.code());
                        notifyError(callback, "Failed to create shortlink for DPS calculator: http status " + response.code());
                        return;
                    }

                    String responseData;
                    try {
                        ShortlinkResponse resp = gson.fromJson(response.body().charStream(), ShortlinkResponse.class);
                        responseData = resp == null ? null : resp.data;
                    } catch (JsonParseException e) {
                        log.warn("Could not parse response data", e);
                        notifyError(callback, "Failed to parse response: " + e.getLocalizedMessage());
                        return;
                    }

                    if (responseData == null) {
                        log.warn("response data is null");
                        notifyError(callback, "Failed to get valid response");
                        return;
                    }

                    notifySuccess(callback, UI_ENDPOINT + "?id=" + responseData);
                }
            }
        });
    }

    private void notifySuccess(DpsWikiApiCallback callback, String url) {
        threads.onEdt(() -> callback.onSuccess(url));
    }

    private void notifyError(DpsWikiApiCallback callback, String message) {
        threads.onEdt(() -> callback.onError(message));
    }

    public interface DpsWikiApiCallback {
        void onError(String errMessage);

        void onSuccess(String url);
    }


    // borrowed from WikiDpsManager.java
    // note: needs to run on client side
    private JsonObject buildShortlinkData(CurrentPlayer player, PlayerStats stats) {
        JsonObject j = new JsonObject();

        // Build the player's loadout data
        JsonArray loadouts = new JsonArray();

        JsonObject l = new JsonObject();
        JsonObject eq = new JsonObject();


        eq.add("ammo", createEqJsonObject(player.getEq().getAmmo()));

        eq.add("body", createEqJsonObject(player.getEq().getBody()));
        eq.add("cape", createEqJsonObject(player.getEq().getCape()));
        eq.add("feet", createEqJsonObject(player.getEq().getBoots()));
        eq.add("hands", createEqJsonObject(player.getEq().getGloves()));
        eq.add("head", createEqJsonObject(player.getEq().getHead()));
        eq.add("legs", createEqJsonObject(player.getEq().getLegs()));
        eq.add("neck", createEqJsonObject(player.getEq().getAmulet()));
        eq.add("ring", createEqJsonObject(player.getEq().getRing()));
        eq.add("shield", createEqJsonObject(player.getEq().getLeftHand()));
        eq.add("weapon", createEqJsonObject(player.getEq().getRightHand()));
        l.add("equipment", eq);

        JsonObject skills = new JsonObject();
        skills.addProperty("hp", stats.getHp());
        skills.addProperty("atk", stats.getAtk());
        skills.addProperty("str", stats.getStr());
        skills.addProperty("def", stats.getDef());
        skills.addProperty("magic", stats.getMagic());
        skills.addProperty("prayer", stats.getPrayer());
        skills.addProperty("ranged", stats.getRanged());
        l.add("skills", skills);

        JsonObject buffs = new JsonObject();
        buffs.addProperty("inWilderness", false);
        buffs.addProperty("kandarinDiary", false);
        buffs.addProperty("onSlayerTask", false);
        buffs.addProperty("chargeSpell", false);
        l.add("buffs", buffs);

        l.addProperty("name", player.getName());

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
