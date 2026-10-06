package io.github.abradee.simplyQuests;

import de.clickism.modrinthupdatechecker.ModrinthUpdateChecker;
import io.github.abradee.simplyQuests.Listeners.QuestListener;
import io.github.abradee.simplyQuests.Managers.QuestManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimplyQuests extends JavaPlugin {

    private QuestManager questManager;

    @Override
    public void onEnable() {
        getLogger().info("The plugin has started.");
        getLogger().info("Feel free to donate through https://patreon.com/abradee");

        // Initialize Quest Manager (loads quests.yml)
        this.questManager = new QuestManager(this);

        // Register Listeners
        getServer().getPluginManager().registerEvents(new QuestListener(questManager), this);

        // Version check via Modrinth
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
        getLogger().info("The plugin has stopped.");
        getLogger().info("Thanks for using SimplyQuests!");
    }

    public QuestManager getQuestManager() {
        return questManager;
    }
}