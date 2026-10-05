package io.github.abradee.simplyQuests;

import org.bukkit.plugin.java.JavaPlugin;
import de.clickism.modrinthupdatechecker.ModrinthUpdateChecker;

public final class SimplyQuests extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("The plugin has started.");
        getLogger().info("Feel free to donate through https://patreon.com/abradee");
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
        // Plugin shutdown logic
    }
}
