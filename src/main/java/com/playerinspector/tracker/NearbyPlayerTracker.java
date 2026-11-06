package com.playerinspector.tracker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Singleton
public class NearbyPlayerTracker {

    private final Client client;

    private List<String> players = new ArrayList<>();

    @Inject
    public NearbyPlayerTracker(Client client, EventBus bus) {
        this.client = client;
        bus.register(this);
    }

    // reset currently tracked list to players currently in vicinity
    public List<String> seed() {
        Stream<String> names = client.getTopLevelWorldView().players().stream().distinct().map(Actor::getName);

        this.players.clear();
        this.players = names.collect(Collectors.toList());

        log.info("player count: " + this.players.size());

        return this.players;
    }


    @Subscribe
    public void onPlayerSpawned(PlayerSpawned spawn) {
        String name = spawn.getPlayer().getName();
        if (!players.contains(name)) {
            players.add(name);
        }
    }

    @Subscribe
    public void onPlayerDespawned(PlayerDespawned despawn) {
        String name = despawn.getPlayer().getName();
        players.remove(name);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged change) {
        GameState state = change.getGameState();
        switch (state) {
            case HOPPING:
            case LOADING:
            case LOGGING_IN:
                seed();
        }
    }


}
