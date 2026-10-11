package io.github.abradee.simplyQuests.Commands.SubCommands;

import io.github.abradee.simplyQuests.Commands.SubCommand;
import io.github.abradee.simplyQuests.SimplyQuests;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements SubCommand {

    private static final String PERMISSION = "simplyquests.reload";
    private final SimplyQuests plugin;

    public ReloadCommand(@NotNull SimplyQuests plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName() {
        return "reload";
    }

    @Override
    public @NotNull String getDescription() {
        return "Reloads the SimplyQuests configuration files.";
    }

    @Override
    public @NotNull String getSyntax() {
        return "/quests reload";
    }

    @Override
    public String getPermission() {
        return PERMISSION;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(Component.text("You do not have permission to reload the configuration.", NamedTextColor.RED));
            return;
        }

        try {
            plugin.reloadConfig();
            sender.sendMessage(Component.text("SimplyQuests configuration reloaded successfully!", NamedTextColor.GREEN));
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to reload configuration: " + e.getMessage());
            sender.sendMessage(Component.text("An error occurred while reloading the configuration. Check console.", NamedTextColor.RED));
        }
    }
}