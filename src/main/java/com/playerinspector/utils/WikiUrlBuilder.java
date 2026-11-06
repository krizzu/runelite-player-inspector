package com.playerinspector.utils;

import net.runelite.api.*;
import okhttp3.*;

public class WikiUrlBuilder {

    private static final HttpUrl WIKI_BASE = HttpUrl.parse("https://oldschool.runescape.wiki");
    private static final String UTM_SOURCE_KEY = "utm_source";
    private static final String UTM_SOURCE_VALUE = "runelite_player_inspector_plugin";


    public static HttpUrl buildForItem(ItemComposition item) {
        HttpUrl.Builder urlBuilder = WIKI_BASE.newBuilder();
        return urlBuilder.addPathSegments("w/Special:Lookup")
                .addQueryParameter("type", "item")
                .addQueryParameter("id", "" + item.getId())
                .addQueryParameter("name", item.getName())
                .addQueryParameter(UTM_SOURCE_KEY, UTM_SOURCE_VALUE)
                .build();
    }
}
