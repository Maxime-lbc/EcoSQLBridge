package fr.lebonniec.maxime.ecosqlbridge.tasks;

import fr.lebonniec.maxime.ecosqlbridge.EcoSQLBridge;
import fr.lebonniec.maxime.ecosqlbridge.network.AccountsConnector;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Logger;


public class UpdateAllBalancesTask
        extends BukkitRunnable
{

    private final Logger logger = EcoSQLBridge.getINSTANCE().logger;

    @Override
    public void run() {
        if(EcoSQLBridge.getINSTANCE().getServer().getOnlinePlayers().size() > 0) {
            logger.info("[EcoSQLBridge] Updating all balances...");
            EcoSQLBridge.getINSTANCE()
                        .getServer()
                        .getOnlinePlayers()
                        .forEach(AccountsConnector::updateUserBalance);
            logger.info("[EcoSQLBridge] All balances updated.");
        } else logger.info("[EcoSQLBridge] No players online, skipping update.");

    }

}
