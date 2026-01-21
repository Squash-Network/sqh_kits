package com.squashcompany.kits.kits;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.squashcompany.kits.kits.commands.KitsCommand;
import com.squashcompany.kits.kits.config.KitsConfig;
import com.squashcompany.kits.kits.manager.KitManager;

import java.util.logging.Level;
import javax.annotation.Nonnull;

/**
 * Main entry point for the Kits plugin.
 *
 * A kit system for rankup servers featuring: - /kits command to open selection
 * menu - Configurable kits with items - Cooldown system per kit - VIP kit
 * support (WIP)
 */
public class Kits extends JavaPlugin {

    private static Kits instance;

    public Kits(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    public static Kits get() {
        return instance;
    }

    @Override
    protected void setup() {
        // Initialize configuration
        KitsConfig.get();

        // Initialize kit manager
        KitManager.get();

        // Register commands
        this.getCommandRegistry().registerCommand(new KitsCommand());

        getLogger().at(Level.INFO).log("Kits plugin setup complete!");
    }

    @Override
    protected void start() {
        // Called when the plugin is enabled
        getLogger().at(Level.INFO).log("Kits has been enabled!");
        getLogger().at(Level.INFO).log("Use /kits to open the kit selection menu.");
    }

    @Override
    protected void shutdown() {
        // Called when the plugin is disabled
        getLogger().at(Level.INFO).log("Kits has been disabled!");
    }
}
