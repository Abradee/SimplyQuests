package io.github.abradee.simplyQuests.Managers;

import io.github.abradee.simplyQuests.Models.Quest;
import io.github.abradee.simplyQuests.Models.QuestType;
import io.github.abradee.simplyQuests.Models.Season;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class QuestManager {

    private final JavaPlugin plugin;
    private final Map<String, Quest> quests = new HashMap<>();
    private final Map<UUID, Map<String, Integer>> playerProgress = new HashMap<>();
    private Season activeSeason = Season.NONE;

    public QuestManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadQuests();
    }

    public void loadQuests() {
        quests.clear();
        File file = new File(plugin.getDataFolder(), "quests.yml");
        if (!file.exists()) {
            plugin.saveResource("quests.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        String seasonStr = config.getString("active-season", "NONE").toUpperCase();
        try {
            this.activeSeason = Season.valueOf(seasonStr);
        } catch (IllegalArgumentException e) {
            this.activeSeason = Season.NONE;
        }

        ConfigurationSection section = config.getConfigurationSection("quests");
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            String name = section.getString(id + ".name", id);
            String target = section.getString(id + ".target", "").toUpperCase();
            int amount = section.getInt(id + ".amount", 1);

            QuestType type;
            try {
                type = QuestType.valueOf(section.getString(id + ".type", "MINE").toUpperCase());
            } catch (IllegalArgumentException e) {
                continue;
            }

            Season season;
            try {
                season = Season.valueOf(section.getString(id + ".season", "NONE").toUpperCase());
            } catch (IllegalArgumentException e) {
                season = Season.NONE;
            }

            quests.put(id, new Quest(id, name, type, target, amount, season));
        }
    }

    public void handleProgress(Player player, QuestType type, String targetIdentifier) {
        UUID uuid = player.getUniqueId();

        for (Quest quest : quests.values()) {
            if (quest.type() != type) continue;

            if (quest.season() != Season.NONE && quest.season() != this.activeSeason) continue;

            if (!quest.target().equalsIgnoreCase(targetIdentifier)) continue;

            Map<String, Integer> userQuests = playerProgress.computeIfAbsent(uuid, k -> new HashMap<>());
            int current = userQuests.getOrDefault(quest.id(), 0);

            if (current >= quest.requiredAmount()) continue;

            current++;
            userQuests.put(quest.id(), current);

            if (current >= quest.requiredAmount()) {
                player.sendMessage(Component.text("Completed Quest: " + quest.name() + "!", NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text(
                        quest.name() + ": " + current + "/" + quest.requiredAmount(),
                        NamedTextColor.YELLOW
                ));
            }
        }
    }
}