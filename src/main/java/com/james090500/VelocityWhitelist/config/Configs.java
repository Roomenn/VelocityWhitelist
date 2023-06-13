package com.james090500.VelocityWhitelist.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.james090500.VelocityWhitelist.VelocityWhitelist;
import com.james090500.VelocityWhitelist.helpers.PlayerEntry;
import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;

public class Configs {

    @Getter private static Config config;
    @Getter private static HashMap<UUID, String> whitelist = new HashMap<>();
    private static VelocityWhitelist plugin;
    private static Path configFile;
    private static Path whitelistFile;
    private static FileTime lastModified;

    /**
     * Loads the config files.
     * @param velocityWhitelist
     */
    public static void loadConfigs(VelocityWhitelist velocityWhitelist) {
        configFile = Path.of(velocityWhitelist.getDataDirectory() + "/config.toml");
        whitelistFile = Path.of("./whitelist.json");
        plugin = velocityWhitelist;

        //Create data directory
        if(!velocityWhitelist.getDataDirectory().toFile().exists()) {
            velocityWhitelist.getDataDirectory().toFile().mkdir();
        }

        //Load the config.toml to memory
        if(!configFile.toFile().exists()) {
            try (InputStream in = VelocityWhitelist.class.getResourceAsStream("/config.toml")) {
                Files.copy(in, configFile);
            } catch (Exception e) {
                velocityWhitelist.getLogger().error("Error loading config.toml");
                e.printStackTrace();
            }
        }
        config = new Toml().read(configFile.toFile()).to(Config.class);

        //Load whitelist players to memory (if any)
        if(whitelistFile.toFile().exists()) {

            try {
                BasicFileAttributes attr = Files.readAttributes(whitelistFile, BasicFileAttributes.class);
                lastModified = attr.lastModifiedTime();
            } catch (Exception e) {
                velocityWhitelist.getLogger().error("Error reading LastModified of whitelist.json");
                e.printStackTrace();
            }

            Configs.loadWhitelist();
        }
    }

    /**
     * Save the config
     */
    public static void saveConfig() {
        try {
            new TomlWriter().write(config, configFile.toFile());
        } catch (Exception e) {
            plugin.getLogger().error("Error writing config.toml");
            e.printStackTrace();
        }
    }

    public static boolean whitelistContains(UUID uuid) {
        return whitelist.get(uuid) != null;
    }

    /**
     * Load Whitelist
     */
    public static @Nullable void loadWhitelist() {
        try (InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(whitelistFile.toFile()), StandardCharsets.UTF_8)) {
            Type whitelistSetType = new TypeToken<HashSet<PlayerEntry>>(){}.getType();
            HashSet<PlayerEntry> whitelistSet = new Gson().fromJson(inputStreamReader, whitelistSetType);

            //whitelist.clear();
            for(PlayerEntry p : whitelistSet) {
                whitelist.put(p.getUuid(), p.getName());
            }

        } catch (Exception e) {
            plugin.getLogger().error("Error loading whitelist.json");
            e.printStackTrace();
        }
    }

    /**
     * Save the whitelist file
     */
    public static void saveWhitelist() {
        try {
            HashSet<PlayerEntry> whitelistSet = new HashSet<>();
            for (UUID uuid : whitelist.keySet()) {
                whitelistSet.add(new PlayerEntry(uuid, whitelist.get(uuid)));
            }

            FileWriter fileWriter = new FileWriter(whitelistFile.toFile());
            new Gson().toJson(whitelistSet, fileWriter);
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            plugin.getLogger().error("Error writing whitelist.json");
            e.printStackTrace();
        }
    }

    /**
     * Update the cached whitelist if file was modify since startup
     */
    public static boolean hasBeenUpdated(){
        if(whitelistFile.toFile().exists()) {
            try {
                BasicFileAttributes attr = Files.readAttributes(whitelistFile, BasicFileAttributes.class);
                if (attr.lastModifiedTime().equals(lastModified)) {
                    return false;
                }
                lastModified = attr.lastModifiedTime();
            } catch (IOException e) {
                plugin.getLogger().error("Error reading LastModified of whitelist.json");
                e.printStackTrace();
            }

            Configs.loadWhitelist();
            return true;
        }
        return false;
    }

    /**
     * The main config
     */
    public class Config {

        @Getter @Setter
        private boolean enabled;
        @Getter
        private String message;

        @Override
        public String toString() {
            return "Panel{" +
                "enabled='" + enabled + '\'' +
                ", message='" + message + '\'' +
            '}';
        }
    }
}