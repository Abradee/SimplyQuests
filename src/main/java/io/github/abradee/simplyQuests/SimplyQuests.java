package io.github.abradee.simplyQuests;

import de.clickism.modrinthupdatechecker.ModrinthUpdateChecker;
import io.github.abradee.simplyQuests.Commands.SimplyQuestsCommand;
import io.github.abradee.simplyQuests.Listeners.PlayerConnectionListener;
import io.github.abradee.simplyQuests.Listeners.QuestListener;
import io.github.abradee.simplyQuests.Managers.DatabaseManager;
import io.github.abradee.simplyQuests.Managers.QuestManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class SimplyQuests extends JavaPlugin {

    private QuestManager questManager;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        getLogger().info("The plugin has started.");
        getLogger().info("Feel free to donate through https://patreon.com/abradee");

        try {
            this.databaseManager = new DatabaseManager(getDataFolder(), "database.db");
        } catch (SQLException ex) {
            getLogger().severe("Failed to connect to the database: " + ex.getMessage());
            ex.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.questManager = new QuestManager(this, this.databaseManager);

        getServer().getPluginManager().registerEvents(new QuestListener(questManager), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(questManager), this);

        PluginCommand command = getCommand("simplyquests");
        if (command != null) {
            SimplyQuestsCommand executor = new SimplyQuestsCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        } else {
            getLogger().severe("Command 'simplyquests' is not registered in plugin.yml!");
        }

        new ModrinthUpdateChecker("simplyquests", "paper", null)
                .checkVersion(latestVersion -> {
                    String currentVersion = getDescription().getVersion();

                    if (currentVersion.contains("-BUILD") || currentVersion.contains("-SNAPSHOT")) {
                        getLogger().warning("Running a development build (" + currentVersion + "). Skipping update check.");
                        getLogger().warning("Development builds are NOT for anything outside of testing.");
                        getLogger().warning("See https://abradee.github.io/devbuild/ for more info.");
                        return;
                    }

                    if (currentVersion.equals(latestVersion)) {
                        getLogger().info("You are running the latest version!");
                    } else {
                        getLogger().warning("A new update is available: v" + latestVersion);
                        getLogger().warning("Your current version: v" + currentVersion);
                        getLogger().warning("Download it here: https://modrinth.com/plugin/simplyquests");
                    }
                });
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            try {
                databaseManager.closeConnection();
            } catch (SQLException ex) {
                getLogger().severe("Failed to close database connection: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        getLogger().info("The plugin has stopped.");
        getLogger().info("Thanks for using SimplyQuests!");
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}