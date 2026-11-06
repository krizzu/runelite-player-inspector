package com.playerinspector.tracker;

import lombok.Value;

import java.util.List;


@Value
public class NearbyPlayerTrackerUpdate {

    List<String> players;
}