package io.github.abradee.simplyQuests.Listeners;

import io.github.abradee.simplyQuests.Managers.QuestManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {
    private final QuestManager questManager;

    public PlayerConnectionListener(QuestManager questManager) {
        this.questManager = questManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        questManager.loadPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        questManager.unloadPlayer(event.getPlayer());
    }
}