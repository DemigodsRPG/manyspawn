package com.demigodsrpg.manyspawn;

import org.bukkit.*;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManySpawnPlugin extends JavaPlugin {
    public static Configuration CONFIG;
    public static World SAFE_WORLD;

    private static ManySpawnPlugin THIS;

    private static Location safeZone;
    private static List<Location> spawnLocations;

    @Override
    public void onEnable() {
        THIS = this;

        getConfig().options().copyDefaults(true);
        CONFIG = getConfig();
        saveConfig();

        CommandManager commands = new CommandManager();
        getCommand("safe").setExecutor(commands);
        getCommand("setsafe").setExecutor(commands);
        getCommand("addspawn").setExecutor(commands);
        getCommand("spawn").setExecutor(commands);

        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

        Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                CommandManager.SPAWN_CACHE.clear();
            }
        }, 0, 2400);



        getLogger().info("Initializing safe zone...");

        safeZone();
        spawnLoc();

        getLogger().info("Enabled successfully.");
    }

    void safeZone() {
        WorldCreator safeCreator = new WorldCreator("safe-zone").
                environment(World.Environment.NORMAL).
                type(WorldType.FLAT).
                generateStructures(false);
        SAFE_WORLD = safeCreator.createWorld();
        SAFE_WORLD.setDifficulty(Difficulty.PEACEFUL);
        SAFE_WORLD.setPVP(false);
        if(CONFIG.isConfigurationSection("safe-zone")) {
            safeZone = unserialize(CONFIG.getConfigurationSection("safe-zone"));
        }
        if(safeZone == null) {
            safeZone = SAFE_WORLD.getSpawnLocation();
            safeZone.setY((double) SAFE_WORLD.getHighestBlockAt(safeZone.getBlockX(), safeZone.getBlockZ()).getY());
            setSafeZone(safeZone);
        }
    }

    static Map<String, Object> serialize(Location loc) {
        Map<String, Object> map = new HashMap<>();
        map.put("world", loc.getWorld().getName());
        map.put("x", loc.getX());
        map.put("y", loc.getY());
        map.put("z", loc.getZ());
        map.put("yaw", loc.getYaw());
        map.put("pitch", loc.getPitch());
        return map;
    }

    static Location unserialize(ConfigurationSection conf) {
        World world = Bukkit.getWorld(conf.getString("world"));
        if(world == null) {
            return null;
        }
        double x, y, z;
        x = conf.getDouble("x");
        y = conf.getDouble("y");
        z = conf.getDouble("z");
        float yaw, pitch;
        yaw = Float.valueOf(conf.getString("yaw"));
        pitch = Float.valueOf(conf.getString("pitch"));
        return new Location(world, x, y, z, yaw, pitch);
    }

    static boolean setSafeZone(Location location) {
        if(SAFE_WORLD.equals(location.getWorld())) {
            safeZone = location;
            SAFE_WORLD.setSpawnLocation(safeZone.getBlockX(), safeZone.getBlockY(), safeZone.getBlockZ());
            CONFIG.set("safe-zone", serialize(safeZone));
            THIS.saveConfig();
            return true;
        }
        return false;
    }

    void spawnLoc() {
        spawnLocations = new ArrayList<>();
        if(CONFIG.isConfigurationSection("spawn-loc")) {
            for(String key : CONFIG.getConfigurationSection("spawn-loc").getKeys(false)) {
                spawnLocations.add(unserialize(CONFIG.getConfigurationSection("spawn-loc").getConfigurationSection(key)));
            }
        } else {
            CONFIG.createSection("spawn-loc");
            World mainWorld = Bukkit.getWorld(CONFIG.getString("main-world"));
            Location spawnLocation = mainWorld.getSpawnLocation();
            spawnLocation.setY((double) mainWorld.getHighestBlockAt(spawnLocation.getBlockX(), spawnLocation.getBlockZ()).getY());
            addSpawn(spawnLocation);
            Location location = spawnLocations.get(0);
            mainWorld.setSpawnLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        }
    }

    static double addSpawn(Location location) {
        if(location == null) {
            return -1.0;
        }
        for(Location other : spawnLocations) {
            if(other.getWorld().equals(location.getWorld()) && other.distance(location) < 200.0) {
                return 200.0 - other.distance(location);
            }
        }
        location.setY(location.getY() + 0.1);
        CONFIG.getConfigurationSection("spawn-loc").set(String.valueOf(System.currentTimeMillis()), serialize(location));
        THIS.saveConfig();
        spawnLocations.add(location);
        return 0.0;
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        SAFE_WORLD.save();
    }

    public static Location getSafeZone() {
        return safeZone;
    }

    public static List<Location> getSpawnLocations() {
        return spawnLocations;
    }
}
