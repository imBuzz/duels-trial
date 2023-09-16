package it.buzz.devroom.duel;

import it.buzz.devroom.duel.data.MySQLConnector;
import it.buzz.devroom.duel.duel.DuelHandler;
import it.buzz.devroom.duel.duel.commands.DuelCommandHandler;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class Duel extends JavaPlugin {

    @Getter private MySQLConnector mySQLConnector;
    @Getter private DuelHandler duelHandler;
    private DuelCommandHandler commandHandler;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        mySQLConnector = new MySQLConnector(this);
        mySQLConnector.start();

        duelHandler = new DuelHandler(this);
        duelHandler.start();

        commandHandler = new DuelCommandHandler(this);
        commandHandler.start();
    }


    @Override
    public void onDisable() {
        commandHandler.stop();
        duelHandler.stop();
        mySQLConnector.stop();
    }



}
