package io.github.aktomik.deadcorpses;

import io.github.aktomik.deadcorpses.spawn.CorpseListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class DeadCorpses extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new CorpseListener(this), this);
        getLogger().info("Enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Disabled!");
    }
}
