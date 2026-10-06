package io.github.abradee.simplyQuests.Listeners;

import io.github.abradee.simplyQuests.Managers.QuestManager;
import io.github.abradee.simplyQuests.Models.QuestType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class QuestListener implements Listener {

    private final QuestManager questManager;

    public QuestListener(QuestManager questManager) {
        this.questManager = questManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        String blockName = event.getBlock().getType().name();
        questManager.handleProgress(event.getPlayer(), QuestType.MINE, blockName);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        if (killer == null) return;

        String mobName = entity.getType().name();
        questManager.handleProgress(killer, QuestType.KILL, mobName);
    }
}