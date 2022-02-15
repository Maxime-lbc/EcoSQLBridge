package fr.lebonniec.maxime.ecosqlbridge.network;

import fr.lebonniec.maxime.ecosqlbridge.EcoSQLBridge;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.logging.Logger;


public class AccountsConnector {

    private static final Logger logger;
    static String DB_uri;
    static String DB_user;
    static String DB_password;
    static String tabName;
    private static Connection connection;

    static {
        final FileConfiguration configuration = EcoSQLBridge.getINSTANCE().config;
        logger = EcoSQLBridge.getINSTANCE().logger;
        DB_uri = "jdbc:mysql://" + configuration.getString("host") + ":" + configuration.getInt("port") + "/" + configuration.getString("database") + "?serverTimezone=UTC";
        DB_user = configuration.getString("username");
        DB_password = configuration.getString("password");
        tabName = "ecosqlbridge_accounts";
    }

    public static boolean userExists(Player player) {
        final String uuid = player.getUniqueId()
                                  .toString();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + tabName + " WHERE uuid = (?)");
            preparedStatement.setString(1,
                                        uuid
            );
            try {
                ResultSet resultSet = preparedStatement.executeQuery();
                try {
                    return resultSet.next();
                } catch (SQLException e) {
                    logger.warning("Cannot get player " + player.getDisplayName() + "in database");
                    e.printStackTrace();
                } finally {
                    if (resultSet != null) {
                        try {
                            resultSet.close();
                        } catch (SQLException e) {
                            logger.warning("Cannot close statement");
                            e.printStackTrace();
                        }
                    }
                }
            } catch (SQLException e) {
                logger.warning("cannot execute query");
                e.printStackTrace();
            } finally {
                if (preparedStatement != null) {
                    try {
                        preparedStatement.close();
                    } catch (SQLException e) {
                        logger.warning("cannot close statement");
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            logger.warning("Unable to read player " + player.getDisplayName() + "in database");
            e.printStackTrace();
        }
        return false;
    }

    public static void createUser(Player player) {
        final String uuid = player.getUniqueId()
                                  .toString();
        final String username = player.getDisplayName();
        final double baseBalance = Math.round(EcoSQLBridge.getINSTANCE().economy.getBalance(player));

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + tabName + " (uuid, username, balance) VALUES (?, ?, ?)");
            preparedStatement.setString(1,
                                        uuid
            );
            preparedStatement.setString(2,
                                        username
            );
            preparedStatement.setDouble(3,
                                        baseBalance
            );
            preparedStatement.executeUpdate();
            preparedStatement.close();
            logger.info("[EcoSQLBridge] User " + player.getName() + " created.");
        } catch (SQLException e) {
            logger.warning("[EcoSQLBridge] Unable to create player " + player.getDisplayName() + "in database");
            e.printStackTrace();
        }
    }

    public static void syncUser(Player player) {
        final double theoreticalBalance = getUserBalance(player);
        final double actualBalance = EcoSQLBridge.getINSTANCE().economy.getBalance(player);
        if (actualBalance < theoreticalBalance) {
            EcoSQLBridge.getINSTANCE().economy.depositPlayer(player,
                                                             theoreticalBalance - actualBalance
            );
        } else if (actualBalance > theoreticalBalance) {
            EcoSQLBridge.getINSTANCE().economy.withdrawPlayer(player,
                                                              actualBalance - theoreticalBalance
            );
        }
        logger.info("[EcoSQLBridge] User " + player.getName() + " synced.");
    }


    public static double getUserBalance(Player player) {
        final String uuid = player.getUniqueId()
                                  .toString();
        double balance = 0;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + tabName + " WHERE uuid = (?)");
            preparedStatement.setString(1,
                                        uuid
            );
            try {
                ResultSet resultSet = preparedStatement.executeQuery();
                try {
                    if (resultSet.next()) {
                        balance = resultSet.getDouble("balance");
                    }
                } catch (SQLException e) {
                    logger.warning("Cannot get player " + player.getDisplayName() + "in database");
                    e.printStackTrace();
                } finally {
                    if (resultSet != null) {
                        try {
                            resultSet.close();
                        } catch (SQLException e) {
                            logger.warning("Cannot close statement");
                            e.printStackTrace();
                        }
                    }
                }
            } catch (SQLException e) {
                logger.warning("cannot execute query");
                e.printStackTrace();
            } finally {
                if (preparedStatement != null) {
                    try {
                        preparedStatement.close();
                    } catch (SQLException e) {
                        logger.warning("cannot close statement");
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            logger.warning("Unable to read player " + player.getDisplayName() + "in database");
            e.printStackTrace();
        }
        return balance;
    }

    public static void updateUserBalance(Player player) {
        final String uuid = player.getUniqueId()
                                  .toString();
        final double balance = EcoSQLBridge.getINSTANCE().economy.getBalance(player);
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + tabName + " SET balance = ? WHERE uuid = ?");
            preparedStatement.setDouble(1,
                                        balance
            );
            preparedStatement.setString(2,
                                        uuid
            );
            preparedStatement.executeUpdate();
            preparedStatement.close();
            logger.info("[EcoSQLBridge] User " + player.getName() + " updated.");
        } catch (SQLException e) {
            logger.warning("[EcoSQLBridge] Unable to update player " + player.getDisplayName() + "in database");
            e.printStackTrace();
        }
    }

    public static void initConnexion() {
        openConnexion();

        try {
            if (!checkTableExistence(tabName)) {
                logger.info("Table not found in database, will attempt to create one");
                createTable();
            }
        } catch (SQLException e) {
            logger.warning("Unable to check table existence in database");
            e.printStackTrace();
        }
    }

    public static void createTable() {
        reopenIfClosed();

        if (tabName != null) {
            try {
                Statement statement = connection.createStatement();

                try {
                    String sql = "CREATE TABLE " + tabName + "(uuid VARCHAR(36) PRIMARY KEY NOT NULL, username VARCHAR(16) NOT NULL UNIQUE, balance DOUBLE NOT NULL DEFAULT 0.0, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)";
                    statement.executeUpdate(sql);
                    logger.info("Successfully created table" + tabName + " in database");
                } catch (SQLException e) {
                    logger.warning("Error creating table in database");
                    e.printStackTrace();
                } finally {
                    if (statement != null) {
                        try {
                            statement.close();
                        } catch (SQLException e) {
                            logger.warning("cannot close statement");
                            e.printStackTrace();
                        }
                    }

                }
            } catch (SQLException e) {
                logger.warning("Error creating table in database");
                e.printStackTrace();
            }
        } else {
            logger.warning("Cannot create table in given database");
        }

    }

    public static void openConnexion() {
        try {
            connection = DriverManager.getConnection(DB_uri,
                                                     DB_user,
                                                     DB_password
            );
            logger.info("Successfully connected to the database");
        } catch (SQLException e) {
            logger.warning("Error connecting to database, check the given information in the config");
            e.printStackTrace();
        }
    }

    public static void closeConnexion() {
        try {
            if (connection != null) {
                connection.close();
                logger.info("Successfully disconnected from database");
            }
        } catch (SQLException e) {
            logger.warning("Unable to close connexion with database");
            e.printStackTrace();
        }

    }

    public static void reopenIfClosed() {
        try {
            if (connection.isClosed()) {
                logger.info("connexion closed, trying to reopen to execute task");
                openConnexion();
            }
        } catch (SQLException e) {
            logger.warning("cannot check connexion status");
            e.printStackTrace();
        }
    }

    private static boolean checkTableExistence(String tableName) throws SQLException {
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        ResultSet res = databaseMetaData.getTables((String) null,
                                                   (String) null,
                                                   tableName,
                                                   new String[]{"TABLE"}
        );
        return res.next();
    }

}
