package io.github.abradee.simplyQuests.Commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface SubCommand {

    @NotNull String getName();

    @NotNull String getDescription();

    @NotNull String getSyntax();

    @Nullable String getPermission();

    void execute(@NotNull CommandSender sender, @NotNull String[] args);

    default @NotNull List<String> getAliases() {
        return List.of();
    }

    default @NotNull List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String[] args) {
        return List.of();
    }
}