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
import java.util.concurrent.atomic.*;


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

    private volatile Call inFlightCall;

    // track requests, stale ones are discarded
    private final AtomicInteger requestCounter = new AtomicInteger();


    // note: callback is fired on EDT
    public void fetchShortLink(PlayerEquipment equipment, PlayerStats stats, DpsWikiApiCallback callback) {
        final int requestId = requestCounter.incrementAndGet();
        cancelInFlightCall();

        Request request;
        try {
            JsonObject jsonBody = buildShortlinkData(equipment, stats);
            request = new Request.Builder()
                    .url(SHORTLINK_ENDPOINT)
                    .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody.toString()))
                    .build();
        } catch (Exception e) {
            log.warn("Failed to build shortlink request", e);
            notifyError(requestId, callback, "Failed to build url payload: " + e.getLocalizedMessage());
            return;
        }

        Call call = okHttpClient.newCall(request);
        inFlightCall = call;

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                clearInFlightCall(call);
                log.warn("Failed to create shortlink for DpsWikiApi", e);
                notifyError(requestId, callback, "Failed to call wiki api: " + e.getLocalizedMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                clearInFlightCall(call);

                try (response) {
                    if (!response.isSuccessful() || response.body() == null) {
                        log.warn("Failed to create shortlink for DPS calculator: http status {}", response.code());
                        notifyError(requestId, callback, "Failed to create shortlink for DPS calculator: http status " + response.code());
                        return;
                    }

                    String responseData;
                    try {
                        ShortlinkResponse resp = gson.fromJson(response.body().charStream(), ShortlinkResponse.class);
                        responseData = resp == null ? null : resp.data;
                    } catch (JsonParseException e) {
                        log.warn("Could not parse response data", e);
                        notifyError(requestId, callback, "Failed to parse response: " + e.getLocalizedMessage());
                        return;
                    }

                    if (responseData == null) {
                        log.warn("response data is null");
                        notifyError(requestId, callback, "Failed to get valid response");
                        return;
                    }

                    notifySuccess(requestId, callback, UI_ENDPOINT + "?id=" + responseData);
                }
            }
        });
    }

    public void cancel() {
        // bumping the counter marks whatever is in flight as stale
        requestCounter.incrementAndGet();
        cancelInFlightCall();
    }

    private void cancelInFlightCall() {
        Call call = inFlightCall;
        inFlightCall = null;
        if (call != null) {
            call.cancel();
        }
    }

    private void clearInFlightCall(Call call) {
        if (inFlightCall == call) {
            inFlightCall = null;
        }
    }

    private void notifySuccess(int requestId, DpsWikiApiCallback callback, String url) {
        if (isStale(requestId)) {
            log.debug("Dropping stale shortlink response");
            return;
        }
        threads.onEdt(() -> callback.onSuccess(url));
    }

    private void notifyError(int requestId, DpsWikiApiCallback callback, String message) {
        if (isStale(requestId)) {
            log.debug("Dropping stale shortlink error: {}", message);
            return;
        }
        threads.onEdt(() -> callback.onError(message));
    }

    private boolean isStale(int requestId) {
        return requestId != requestCounter.get();
    }

    public interface DpsWikiApiCallback {
        void onError(String errMessage);

        void onSuccess(String url);
    }


    // borrowed from WikiDpsManager.java
    private JsonObject buildShortlinkData(PlayerEquipment equipment, PlayerStats stats) {
        JsonObject j = new JsonObject();

        // Build the player's loadout data
        JsonArray loadouts = new JsonArray();

        JsonObject l = new JsonObject();
        JsonObject eq = new JsonObject();


        eq.add("ammo", createEqJsonObject(equipment.getAmmo()));

        eq.add("body", createEqJsonObject(equipment.getBody()));
        eq.add("cape", createEqJsonObject(equipment.getCape()));
        eq.add("feet", createEqJsonObject(equipment.getBoots()));
        eq.add("hands", createEqJsonObject(equipment.getGloves()));
        eq.add("head", createEqJsonObject(equipment.getHead()));
        eq.add("legs", createEqJsonObject(equipment.getLegs()));
        eq.add("neck", createEqJsonObject(equipment.getAmulet()));
        eq.add("ring", createEqJsonObject(equipment.getRing()));
        eq.add("shield", createEqJsonObject(equipment.getLeftHand()));
        eq.add("weapon", createEqJsonObject(equipment.getRightHand()));
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

        l.addProperty("name", "Inspected loadout");

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
