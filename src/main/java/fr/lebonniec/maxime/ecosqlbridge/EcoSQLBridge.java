package fr.lebonniec.maxime.ecosqlbridge;

import fr.lebonniec.maxime.ecosqlbridge.listeners.PlayerListener;
import fr.lebonniec.maxime.ecosqlbridge.network.AccountsConnector;
import fr.lebonniec.maxime.ecosqlbridge.tasks.UpdateAllBalancesTask;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.logging.Logger;


public final class EcoSQLBridge
        extends JavaPlugin
{

    private static EcoSQLBridge INSTANCE;
    public final Logger logger = Logger.getLogger("Minecraft");
    public Economy economy = null;
    public FileConfiguration config;


    @Override
    public void onLoad() {
        logger.info("[EcoSQLBridge] - Loading...");
    }

    @Override
    public void onEnable()
    {
        logger.info("[EcoSQLBridge] - Started");
        if (!setupEconomy()) {
            logger.severe(String.format("[%s] - Disabled due to no Vault dependency found!",
                                        getDescription().getName()
            ));
            getServer().getPluginManager()
                       .disablePlugin(this);
            return;
        }
        INSTANCE = this;
        loadConfig();
        logger.info("[EcoSQLBridge] - SQL enabled: " + config.getBoolean("enabled"));
        logger.info("[EcoSQLBridge] - Plugin enabled");
        setupConnectors();
        setupListeners();
        setupTasks();
    }

    @Override
    public void onDisable()
    {
        stopConnectors();
        logger.info("[EcoSQLBridge] - Plugin disabled");
    }

    private void loadConfig() {
        this.saveDefaultConfig();
        this.config = getConfig();
        logger.info("[EcoSQLBridge] - Loaded config");
    }


    private boolean setupEconomy()
    {

        if (getServer().getPluginManager()
                       .getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager()
                                                            .getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }

    private void setupListeners() {
        getServer().getPluginManager()
                   .registerEvents(new PlayerListener(),
                                   this
                   );
    }

    private void setupTasks() {
        BukkitTask updateAllBalancesTask = new UpdateAllBalancesTask().runTaskTimerAsynchronously(this,
                                                                                                  0L,
                                                                                                  (20 * 60L)
        );
    }

    private void setupConnectors() {
        if (config.getBoolean("enabled")) {
            AccountsConnector.initConnexion();
            logger.info("[EcoSQLBridge] - Connectors are up!");
        }
    }

    private void stopConnectors() {
        if (config.getBoolean("enabled")) {
            AccountsConnector.closeConnexion();
            logger.info("[EcoSQLBridge] - Connectors are down!");
        }
    }

    public static EcoSQLBridge getINSTANCE() {

        return INSTANCE;
    }

}
