package it.buzz.devroom.duel.data;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import it.buzz.devroom.duel.Duel;
import it.buzz.devroom.duel.holder.AbstractPluginHolder;
import it.buzz.devroom.duel.holder.Startable;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MySQLConnector extends AbstractPluginHolder implements Startable {

    public final static String ACTIVE_TABLE = "pduel_stats";
    private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("duel-data-thread").build());
    private HikariDataSource dataSource;

    public MySQLConnector(Duel plugin) {
        super(plugin);
    }

    @Override
    public void start() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/minecraft");
        config.setUsername("minecraft");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        dataSource = new HikariDataSource(config);

        //Create table
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + ACTIVE_TABLE + " " +
                "(uuid VARCHAR(36) PRIMARY KEY, name VARCHAR(16), wins INT, kills INT, deaths INT, total_games INT, winstreak INT)")) {
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void stop() {
        executor.shutdown();

        plugin.getLogger().info("Waiting for duel-data-thread to end all tasks...");
        try {
            if (executor.awaitTermination(5, TimeUnit.SECONDS)) {
                plugin.getLogger().info("All tasks completed for duel-data-thread");
            } else {
                plugin.getLogger().info("Timed out for duel-data-thread");
            }
        } catch (Exception e) {
            e.printStackTrace();
            plugin.getLogger().info("An error occured while completing tasks for duel-data-thread");
        }

        dataSource.close();
    }

    public void execute(Runnable runnable) {
        if (Thread.currentThread().getName().equals("duel-data-thread")) runnable.run();
        else executor.execute(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public Connection connection() throws SQLException {
        return dataSource.getConnection();
    }


}