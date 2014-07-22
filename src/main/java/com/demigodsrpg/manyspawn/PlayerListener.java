package com.demigodsrpg.manyspawn;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener  {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(!player.hasPlayedBefore()) {
            Location spawn = CommandManager.getSpawnFor(event.getPlayer());
            if(ManySpawnPlugin.CONFIG.getBoolean("start-in-safe")) {
                spawn = ManySpawnPlugin.getSafeZone();
            }
            player.teleport(spawn);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent event) {
        if(!event.getTo().getWorld().equals(event.getFrom().getWorld())) {
            Player player = event.getPlayer();
            if(!player.hasPermission("manyspawn.keepgamemode")) {
                if (event.getFrom().getWorld().equals(ManySpawnPlugin.SAFE_WORLD)) {
                    player.setGameMode(GameMode.SURVIVAL);
                } else if (event.getTo().getWorld().equals(ManySpawnPlugin.SAFE_WORLD)) {
                    player.setGameMode(GameMode.ADVENTURE);
                }
            }
        }
    }
}
