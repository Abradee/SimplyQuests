package io.github.abradee.simplyQuests.Commands.SubCommands;

import io.github.abradee.simplyQuests.SimplyQuests;
import io.github.abradee.simplyQuests.Commands.SubCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Set;

public class ListCommand implements SubCommand {

    private static final String PERMISSION = "simplyquests.list";
    private final SimplyQuests plugin;

    public ListCommand(@NotNull SimplyQuests plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName() {
        return "list";
    }

    @Override
    public @NotNull String getDescription() {
        return "Lists all available quests configured in quests.yml.";
    }

    @Override
    public @NotNull String getSyntax() {
        return "/quests list";
    }

    @Override
    public @Nullable String getPermission() {
        return PERMISSION;
    }

    @Override
    public @NotNull List<String> getAliases() {
        return List.of("quests", "all");
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        File file = new File(plugin.getDataFolder(), "quests.yml");

        if (!file.exists()) {
            sender.sendMessage(Component.text("quests.yml does not exist yet.", NamedTextColor.RED));
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection questsSection = config.getConfigurationSection("quests");
        if (questsSection == null) {
            sender.sendMessage(Component.text("No 'quests:' section found in quests.yml.", NamedTextColor.RED));
            return;
        }

        Set<String> keys = questsSection.getKeys(false);
        if (keys.isEmpty()) {
            sender.sendMessage(Component.text("No quests found in quests.yml.", NamedTextColor.YELLOW));
            return;
        }

        sender.sendMessage(Component.text("--- Available Quests (" + keys.size() + ") ---", NamedTextColor.GOLD));

        for (String questKey : keys) {
            ConfigurationSection quest = questsSection.getConfigurationSection(questKey);
            if (quest == null) continue;

            String displayName = quest.getString("name", questKey);
            String type = quest.getString("type", "UNKNOWN");
            String target = quest.getString("target", "NONE");
            int amount = quest.getInt("amount", 0);
            String rewardType = quest.getString("rewardtype", "money");
            String reward = quest.getString("reward", "None");
            String season = quest.getString("season", "NONE");

            // Rich hover tooltip displaying task and reward details
            Component hoverDetails = Component.text()
                    .append(Component.text(displayName, NamedTextColor.GOLD)).appendNewline()
                    .append(Component.text("Task: ", NamedTextColor.GRAY))
                    .append(Component.text(type + " " + amount + "x " + target, NamedTextColor.WHITE)).appendNewline()
                    .append(Component.text("Reward: ", NamedTextColor.GRAY))
                    .append(Component.text(reward + " (" + rewardType + ")", NamedTextColor.GREEN)).appendNewline()
                    .append(Component.text("Season: ", NamedTextColor.GRAY))
                    .append(Component.text(season, NamedTextColor.AQUA))
                    .build();

            Component line = Component.text("• ", NamedTextColor.DARK_GRAY)
                    .append(Component.text(displayName, NamedTextColor.YELLOW))
                    .append(Component.text(" [" + questKey + "]", NamedTextColor.DARK_GRAY))
                    .hoverEvent(HoverEvent.showText(hoverDetails));

            sender.sendMessage(line);
        }
    }
}