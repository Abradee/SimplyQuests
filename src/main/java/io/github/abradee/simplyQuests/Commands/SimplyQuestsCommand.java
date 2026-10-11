package io.github.abradee.simplyQuests.Commands;

import io.github.abradee.simplyQuests.Commands.SubCommands.ListCommand;
import io.github.abradee.simplyQuests.Commands.SubCommands.ReloadCommand;
import io.github.abradee.simplyQuests.Commands.SubCommand;
import io.github.abradee.simplyQuests.SimplyQuests;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SimplyQuestsCommand implements CommandExecutor, TabCompleter {

    private final SimplyQuests plugin;
    private final Map<String, SubCommand> subCommands = new LinkedHashMap<>();
    private final Map<String, SubCommand> aliases = new HashMap<>();

    public SimplyQuestsCommand(@NotNull SimplyQuests plugin) {
        this.plugin = plugin;

        // Register default subcommands
        registerSubCommand(new ReloadCommand(plugin));
        registerSubCommand(new ListCommand(plugin));
    }

    public void registerSubCommand(@NotNull SubCommand subCommand) {
        String name = subCommand.getName().toLowerCase(Locale.ROOT);
        subCommands.put(name, subCommand);

        for (String alias : subCommand.getAliases()) {
            aliases.put(alias.toLowerCase(Locale.ROOT), subCommand);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (args.length == 0) {
            sendHelpMenu(sender, label);
            return true;
        }

        String subName = args[0].toLowerCase(Locale.ROOT);
        SubCommand target = findSubCommand(subName);

        if (target == null) {
            sender.sendMessage(Component.text("Unknown subcommand. Use ", NamedTextColor.RED)
                    .append(Component.text("/" + label, NamedTextColor.YELLOW)
                            .clickEvent(ClickEvent.runCommand("/" + label))
                            .hoverEvent(HoverEvent.showText(Component.text("Click to view commands", NamedTextColor.GRAY))))
                    .append(Component.text(" to see available commands.", NamedTextColor.RED)));
            return true;
        }

        if (target.getPermission() != null && !sender.hasPermission(target.getPermission())) {
            sender.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
            return true;
        }

        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        target.execute(sender, subArgs);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {

        if (args.length == 1) {
            List<String> available = new ArrayList<>();
            for (SubCommand sub : subCommands.values()) {
                if (sub.getPermission() == null || sender.hasPermission(sub.getPermission())) {
                    available.add(sub.getName());
                }
            }
            return StringUtil.copyPartialMatches(args[0], available, new ArrayList<>());
        }

        if (args.length > 1) {
            SubCommand target = findSubCommand(args[0].toLowerCase(Locale.ROOT));
            if (target != null && (target.getPermission() == null || sender.hasPermission(target.getPermission()))) {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                List<String> completions = target.getTabCompletions(sender, subArgs);
                return completions != null ? completions : Collections.emptyList();
            }
        }

        return Collections.emptyList();
    }

    private @Nullable SubCommand findSubCommand(String name) {
        SubCommand sub = subCommands.get(name);
        return sub != null ? sub : aliases.get(name);
    }

    private void sendHelpMenu(@NotNull CommandSender sender, @NotNull String label) {
        List<SubCommand> permitted = subCommands.values().stream()
                .filter(sub -> sub.getPermission() == null || sender.hasPermission(sub.getPermission()))
                .toList();

        if (permitted.isEmpty()) {
            sender.sendMessage(Component.text("You do not have permission to use any SimplyQuests commands.", NamedTextColor.RED));
            return;
        }

        sender.sendMessage(Component.text("--- SimplyQuests Commands ---", NamedTextColor.GOLD));
        for (SubCommand sub : permitted) {
            Component line = Component.text(sub.getSyntax() + " ", NamedTextColor.YELLOW)
                    .append(Component.text("- " + sub.getDescription(), NamedTextColor.GRAY))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to suggest: " + sub.getSyntax(), NamedTextColor.AQUA)))
                    .clickEvent(ClickEvent.suggestCommand(sub.getSyntax()));

            sender.sendMessage(line);
        }
    }

    public SimplyQuests getPlugin() {
        return plugin;
    }
}