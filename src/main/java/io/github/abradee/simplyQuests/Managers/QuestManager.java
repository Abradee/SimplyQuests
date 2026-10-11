package io.github.abradee.simplyQuests.Managers;

import io.github.abradee.simplyQuests.Models.Quest;
import io.github.abradee.simplyQuests.Models.QuestType;
import io.github.abradee.simplyQuests.Models.Season;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QuestManager {

    private final JavaPlugin plugin;
    private final DatabaseManager databaseManager;
    private final Map<String, Quest> quests = new HashMap<>();

    private final Map<UUID, Map<String, Integer>> playerProgress = new ConcurrentHashMap<>();
    private Season activeSeason = Season.NONE;

    public QuestManager(JavaPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        loadQuests();
    }

    public void loadPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Map<String, Integer> progress = databaseManager.loadPlayerQuests(uuid);
            playerProgress.put(uuid, progress);
        });
    }

    public void unloadPlayer(Player player) {
        playerProgress.remove(player.getUniqueId());
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
            String rewardType = section.getString(id + ".rewardtype", "").toLowerCase();
            String reward = section.getString(id + ".reward", "");
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

            quests.put(id, new Quest(id, name, type, target, rewardType, reward, amount, season));
        }
    }

    public void handleProgress(Player player, QuestType type, String targetIdentifier) {
        UUID uuid = player.getUniqueId();
        if (!playerProgress.containsKey(uuid)) return;

        for (Quest quest : quests.values()) {
            if (quest.type() != type) continue;
            if (quest.season() != Season.NONE && quest.season() != this.activeSeason) continue;
            if (!quest.target().equalsIgnoreCase(targetIdentifier)) continue;

            Map<String, Integer> userQuests = playerProgress.get(uuid);
            int current = userQuests.getOrDefault(quest.id(), 0);

            if (current >= quest.requiredAmount()) continue;

            current++;
            userQuests.put(quest.id(), current);

            int finalCurrent = current;
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                databaseManager.saveQuestProgress(uuid, quest.id(), finalCurrent);
            });

            if (current >= quest.requiredAmount()) {
                player.sendMessage(Component.text("Completed Quest: " + quest.name() + "!", NamedTextColor.GREEN));
                giveReward(player, quest);
            } else {
                player.sendMessage(Component.text(
                        quest.name() + ": " + current + "/" + quest.requiredAmount(),
                        NamedTextColor.YELLOW
                ));
            }
        }
    }

    private void giveReward(Player player, Quest quest) {
        String type = quest.rewardType().toLowerCase();
        String reward = quest.reward().trim();

        switch (type) {
            case "key" -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "crate key " + player.getName() + " " + reward.toLowerCase());
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            }
            case "money" -> {
                long parsedAmount = parseMoneyAmount(reward);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "addmoney " + player.getName() + " " + parsedAmount);
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            }
            case "item" -> {
                Material material = Material.matchMaterial(reward.toUpperCase());
                if (material != null) {
                    ItemStack item = new ItemStack(material, 1);
                    HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item);
                    for (ItemStack leftover : overflow.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), leftover);
                    }
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                } else {
                    plugin.getLogger().warning("Invalid item reward '" + reward + "' for quest " + quest.id());
                }
            }
            default -> plugin.getLogger().warning("Unknown reward type '" + type + "' for quest " + quest.id());
        }
    }

    private long parseMoneyAmount(String input) {
        String clean = input.toLowerCase().replaceAll("[$,]", "").trim();
        try {
            if (clean.endsWith("k")) {
                double val = Double.parseDouble(clean.substring(0, clean.length() - 1));
                return (long) (val * 1_000);
            } else if (clean.endsWith("m")) {
                double val = Double.parseDouble(clean.substring(0, clean.length() - 1));
                return (long) (val * 1_000_000);
            } else if (clean.endsWith("b")) {
                double val = Double.parseDouble(clean.substring(0, clean.length() - 1));
                return (long) (val * 1_000_000_000L);
            }
            return (long) Double.parseDouble(clean);
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Failed to parse money amount: " + input);
            return 0;
        }
    }
}