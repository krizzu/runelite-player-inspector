package com.playerinspector.tracker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Singleton
public class NearbyPlayerTracker {

    private final Client client;
    private final EventBus bus;

    private List<String> players = new ArrayList<>();

    @Inject
    public NearbyPlayerTracker(Client client, EventBus bus) {
        this.client = client;
        this.bus = bus;
    }

    public List<String> getPlayers() {
        return new ArrayList<>(this.players);
    }

    public void init() {
        bus.register(this);
    }

    public void deinit() {
        bus.unregister(this);
    }

    // reset currently tracked list to players currently in vicinity
    public void seed() {
        // get other players, filter out local one
        List<String> names = client.getTopLevelWorldView().players().stream().map(Actor::getName).filter(Objects::nonNull).collect(Collectors.toList());

        sendUpdateWithNewPlayers(names);
    }

    public void clear() {
        this.players.clear();
        sendEvent();
    }


    @Subscribe
    public void onPlayerSpawned(PlayerSpawned spawn) {
        String name = getPlayerNameOrNull(spawn.getPlayer());

        if (name == null) {
            return;
        }

        players.add(name);
        sendUpdateWithNewPlayers(players);
    }

    @Subscribe
    public void onPlayerDespawned(PlayerDespawned despawn) {
        String name = getPlayerNameOrNull(despawn.getPlayer());
        if (name == null) {
            return;
        }

        players.remove(name);
        sendEvent();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged change) {
        GameState state = change.getGameState();
        switch (state) {
            case HOPPING:
            case LOADING:
            case LOGGING_IN:
                clear();
                break;
            case LOGGED_IN:
                seed();
                break;

        }
    }

    // updates the list, with local player being at the top
    private void sendUpdateWithNewPlayers(List<String> newPlayers) {
        String localPlayerName = getPlayerNameOrNull(client.getLocalPlayer());
        if (localPlayerName == null) {
            return;
        }

        List<String> all = new ArrayList<>(newPlayers).stream().filter(name -> !localPlayerName.equals(name)).distinct().collect(Collectors.toList());
        all.add(0, localPlayerName);
        this.players = all;

        sendEvent();
    }


    private String getPlayerNameOrNull(Player player) {
        if (player == null) {
            return null;
        }

        String name = player.getName();
        //noinspection RedundantIfStatement
        if (name == null) {
            return null;
        }

        return name;
    }

    private void sendEvent() {
        List<String> copy = new ArrayList<>(this.players);

        bus.post(new NearbyPlayerTrackerUpdate(copy));
    }
}
