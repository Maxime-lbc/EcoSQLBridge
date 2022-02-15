package fr.lebonniec.maxime.ecosqlbridge.listeners;

import fr.lebonniec.maxime.ecosqlbridge.EcoSQLBridge;
import fr.lebonniec.maxime.ecosqlbridge.network.AccountsConnector;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.logging.Logger;


public class PlayerListener
        implements Listener
{

    static final Logger logger = EcoSQLBridge.getINSTANCE().logger;
    static final BukkitScheduler scheduler = EcoSQLBridge.getINSTANCE()
                                                         .getServer()
                                                         .getScheduler();

    @EventHandler
    public static void onPlayerJoin(PlayerJoinEvent e)
    {
        Player player = e.getPlayer();
        logger.info("[EcoSQLBridge] Player " + player.getName() + " joined the server.");
        scheduler.runTaskAsynchronously(EcoSQLBridge.getINSTANCE(),
                                        () -> {
                                            if (!AccountsConnector.userExists(player)) {
                                                logger.info("[EcoSQLBridge] User " + player.getName() + " doesn't exists. Creating it.");
                                                AccountsConnector.createUser(player);
                                            } else {
                                                logger.info("[EcoSQLBridge] User " + player.getName() + " exists. Let's sync it.");
                                                AccountsConnector.syncUser(player);
                                            }
                                        }
        );
    }

    @EventHandler
    public static void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        logger.info("[EcoSQLBridge] Player " + player.getName() + " left the server.");
        scheduler.runTaskAsynchronously(EcoSQLBridge.getINSTANCE(),
                                        () -> {
                                            if (AccountsConnector.userExists(player)) {
                                                logger.info("[EcoSQLBridge] User " + player.getName() + " exists. Let's update it's account.");
                                                AccountsConnector.updateUserBalance(player);
                                            } else {
                                                logger.info("[EcoSQLBridge] User " + player.getName() + " doesn't exists. Creating it.");
                                                AccountsConnector.createUser(player);
                                            }
                                        }
        );
    }

}


