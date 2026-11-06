package com.playerinspector.tracker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.playerinspector.utils.Threads;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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
import java.util.stream.Stream;

@Slf4j
@Singleton
public class NearbyPlayerTracker {

    private final Client client;
    private final EventBus bus;
    private final Threads threads;

    @Getter
    private volatile List<String> players = List.of();

    @Inject
    public NearbyPlayerTracker(Client client, EventBus bus, Threads threads) {
        this.client = client;
        this.bus = bus;
        this.threads = threads;
    }

    public void init() {
        bus.register(this);
    }

    public void deinit() {
        bus.unregister(this);
    }

    // reset currently tracked list to players currently in vicinity
    // runs on client thread
    public void seed() {
        threads.onClient(() -> {
            if (client.getGameState() != GameState.LOGGED_IN) {
                return;
            }

            // get other players, filter out local one
            List<String> names = client.getTopLevelWorldView().players().stream().map(Actor::getName).filter(Objects::nonNull).collect(Collectors.toList());

            sendUpdateWithNewPlayers(names);
        });
    }

    //
    public void clear() {
        threads.onClient(() -> {
            this.players = List.of();
            sendEvent(this.players);
        });
    }


    @Subscribe
    public void onPlayerSpawned(PlayerSpawned spawn) {
        String name = getPlayerNameOrNull(spawn.getPlayer());

        if (name == null) {
            return;
        }

        List<String> newList = new ArrayList<>();
        newList.add(name);
        newList.addAll(this.players);
        sendUpdateWithNewPlayers(newList);
    }

    @Subscribe
    public void onPlayerDespawned(PlayerDespawned despawn) {
        String name = getPlayerNameOrNull(despawn.getPlayer());
        if (name == null) {
            return;
        }
        List<String> newList = this.players.stream().filter(n -> !name.equals(n)).collect(Collectors.toList());
        sendUpdateWithNewPlayers(newList);
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
            // todo: change this to debug once done
            log.warn("No Local player, dropping update");
            return;
        }

        // put local player and new players in one list
        List<String> all = Stream.concat(
                Stream.of(localPlayerName),
                newPlayers
                        .stream()
                        .filter(name -> !localPlayerName.equals(name))
                        .distinct()
                        .sorted()
        ).collect(Collectors.toUnmodifiableList());

        this.players = all;
        sendEvent(all);
    }


    private String getPlayerNameOrNull(Player player) {
        if (player == null) {
            return null;
        }

        // might be null
        return player.getName();
    }

    private void sendEvent(List<String> playerList) {
        bus.post(new NearbyPlayerTrackerUpdate(playerList));
    }
}
