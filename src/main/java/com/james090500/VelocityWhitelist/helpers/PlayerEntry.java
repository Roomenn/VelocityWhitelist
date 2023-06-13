package com.james090500.VelocityWhitelist.helpers;

import lombok.Getter;

import java.util.UUID;

public class PlayerEntry {
    @Getter private UUID uuid;
    @Getter private String name;

    public PlayerEntry(UUID uuid, String username) {
        this.uuid = uuid;
        this.name = username;
    }

    @Override
    public String toString() {
        return "Username: " + name;
    }
}
