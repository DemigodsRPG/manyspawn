package com.demigodsrpg.manyspawn;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CommandManager implements CommandExecutor {
    private static Random RANDOM = new Random();

    static ConcurrentMap<UUID, Integer> SPAWN_CACHE = new ConcurrentHashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof ConsoleCommandSender) {
            sender.sendMessage("Console can't use this command.");
            return true;
        }
        Player player = (Player) sender;
        String cmd = command.getName().toLowerCase();

        switch (cmd) {
        /*
         * SPAWN
         */
            case "spawn":
                Location spawn = getSpawnFor(player);
                player.teleport(spawn);
                player.sendMessage(ChatColor.YELLOW + "Teleported to spawn.");
                break;
        /*
         * SAFE
         */
            case "safe":
                player.teleport(ManySpawnPlugin.getSafeZone());
                player.sendMessage(ChatColor.YELLOW + "Teleported to safe spawn.");
                break;
        /*
         * ADDSPAWN
         */
            case "addspawn":
                double result = ManySpawnPlugin.addSpawn(player.getLocation());
                if (result == 0.0) {
                    player.sendMessage(ChatColor.YELLOW + "Spawn locations have been updated.");
                } else if (result <= 200.0) {
                    player.sendMessage(ChatColor.RED + "You need to be " + (int) result + " blocks away from here.");
                } else {
                    player.sendMessage(ChatColor.RED + "Somehow this location doesn't exist...");
                }
                break;
        /*
         * SETSAFE
         */
            case "setsafe":
                if (ManySpawnPlugin.setSafeZone(player.getLocation())) {
                    player.sendMessage(ChatColor.YELLOW + "Safe spawn has been set.");
                } else {
                    player.sendMessage(ChatColor.RED + "Safe spawn could not be set.");
                }
                break;

            default:
                return false;
        }
        return true;
    }

    public static Location getSpawnFor(Player player) {
        int size = ManySpawnPlugin.getSpawnLocations().size();
        int index = 0;
        if(SPAWN_CACHE.containsKey(player.getUniqueId())) {
            index = SPAWN_CACHE.get(player.getUniqueId());
        } else if (size > 1) {
            index = generateIntRange(0, size - 1);
        }
        SPAWN_CACHE.putIfAbsent(player.getUniqueId(), index);
        return ManySpawnPlugin.getSpawnLocations().get(index);
    }

    /**
     * Generates a random integer with a value between <code>min</code> and <code>max</code>.
     *
     * @param min the minimum value of the integer.
     * @param max the maximum value of the integer.
     *
     * @return Random integer.
     */
    public static int generateIntRange(int min, int max) {
        return RANDOM.nextInt(max - min + 1) + min;
    }
}

