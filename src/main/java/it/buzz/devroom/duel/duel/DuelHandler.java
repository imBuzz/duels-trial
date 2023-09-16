package it.buzz.devroom.duel.duel;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import it.buzz.devroom.duel.Duel;
import it.buzz.devroom.duel.data.MySQLConnector;
import it.buzz.devroom.duel.duel.duel.DuelMetadata;
import it.buzz.devroom.duel.duel.duel.map.DuelMap;
import it.buzz.devroom.duel.duel.duel.player.DuelPlayer;
import it.buzz.devroom.duel.duel.duel.player.inventory.InventorySnapshot;
import it.buzz.devroom.duel.duel.duel.player.inventory.contents.ArmorContents;
import it.buzz.devroom.duel.holder.AbstractPluginHolder;
import it.buzz.devroom.duel.holder.Startable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.bukkit.event.EventPriority.HIGH;

public class DuelHandler extends AbstractPluginHolder implements Startable, Listener {

    private final static Gson GSON = new Gson();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("duel-thread").build());

    private final Map<String, InventorySnapshot> kits = new HashMap<>();
    private final Map<String, DuelMap> maps = new HashMap<>();

    private final Map<UUID, DuelPlayer> players = new HashMap<>();

    public DuelHandler(Duel plugin) {
        super(plugin);
    }

    @Override
    public void start() {
        createDefaultKit();
        createDefaultMap();

        loadMaps();
        loadKits();

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void stop() {
        executor.shutdownNow();

        kits.clear();
        maps.clear();
    }

    public void acceptDuel(DuelMetadata metadata){
        executor.execute(() -> {
            
            DuelMap map = null;
            for (DuelMap duelMap : maps.values()) {
                if (!duelMap.isInUse()){
                    map = duelMap;
                    map.setInUse(true);
                    break;
                }
            }
            if (map == null){
                metadata.getOnlinePlayers().forEach(player ->
                        player.sendMessage(ChatColor.RED + "There arent any free maps to use"));
                return;
            }


            metadata.makeDuel(plugin, map).start();
        });
    }

    public DuelPlayer getPlayer(UUID uuid){
        return players.get(uuid);
    }

    public InventorySnapshot getKit(String name){
        return kits.getOrDefault(name, kits.getOrDefault("default", InventorySnapshot.EMPTY_SNAPSHOT));
    }
    public boolean isKitExists(String name){
        return kits.containsKey(name.toLowerCase());
    }

    private void createDefaultMap(){
        DuelMap duelMap = new DuelMap("MapName", "world", "10:10:10:1:1", "20:20:20:1:1");

        File folder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "maps");
        if (!folder.exists())
            folder.mkdir();

        File defaultFile = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "maps" + File.separator + "default.json");
        if (!defaultFile.exists()) {
            try (Writer writer = new FileWriter(defaultFile.getAbsolutePath())) {
                GSON.toJson(duelMap, writer);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void createDefaultKit(){
        InventorySnapshot snapshot = InventorySnapshot.builder()
                .armorContents(
                        ArmorContents.builder()
                                .helmet(new InventorySnapshot.SerializedItemStack(Material.CHAINMAIL_HELMET, 1))
                                .build()
                )
                .inventory(Lists.newArrayList(
                        InventorySnapshot.InventoryItem.builder()
                                .itemStack(new InventorySnapshot.SerializedItemStack(Material.DIAMOND_SWORD, 1))
                                .position(0)
                                .build()
                ))
                .build();

        File folder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "kits");
        if (!folder.exists())
            folder.mkdir();

        File defaultFile = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "kits" + File.separator + "default.json");
        if (!defaultFile.exists()) {
            try (Writer writer = new FileWriter(defaultFile.getAbsolutePath())) {
                GSON.toJson(snapshot, writer);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void loadKits(){
        File kitFolder = new File(plugin.getDataFolder() + File.separator + "kits");
        int loaded = 0;

        if (kitFolder.exists() && kitFolder.isDirectory()){
            for (File file : Objects.requireNonNull(kitFolder.listFiles())) {
                if (file.getName().endsWith(".json")){
                    try {
                        InventorySnapshot snapshot = GSON.fromJson(new FileReader(file), InventorySnapshot.class);
                        kits.put(file.getName().split("\\.")[0], snapshot);

                        loaded++;
                    }
                    catch (Exception e){
                        e.printStackTrace();
                        plugin.getLogger().severe("An error occured while loading this kit: " + file.getName());
                    }
                }
            }
        }

        plugin.getLogger().info("Loaded " + loaded + " kits.");
    }
    private void loadMaps(){
        File kitFolder = new File(plugin.getDataFolder() + File.separator + "maps");
        int loaded = 0;

        if (kitFolder.exists() && kitFolder.isDirectory()){
            for (File file : Objects.requireNonNull(kitFolder.listFiles())) {
                if (file.getName().endsWith(".json")){
                    try {
                        DuelMap map = GSON.fromJson(new FileReader(file), DuelMap.class);
                        maps.put(file.getName().split("\\.")[0].toLowerCase(), map);

                        loaded++;
                    }
                    catch (Exception e){
                        e.printStackTrace();
                        plugin.getLogger().severe("An error occured while loading this map: " + file.getName());
                    }
                }
            }
        }

        plugin.getLogger().info("Loaded " + loaded + " maps.");
    }

    @EventHandler
    private void join(PlayerJoinEvent event){
        final Player player = event.getPlayer();
        plugin.getMySQLConnector().execute(() -> {
            try (Connection connection = plugin.getMySQLConnector().connection(); PreparedStatement statement =
                    connection.prepareStatement("SELECT * FROM " + MySQLConnector.ACTIVE_TABLE + " WHERE uuid= '" + player.getUniqueId() + "' LIMIT 1")) {
                try (ResultSet set = statement.executeQuery()) {
                    if (set.next()) {
                        int wins = set.getInt(3);
                        int kills = set.getInt(4);
                        int deaths = set.getInt(5);
                        int total_games = set.getInt(6);
                        int winstreak = set.getInt(7);

                        players.put(player.getUniqueId(),
                                new DuelPlayer(player.getUniqueId(), wins, kills, deaths, total_games, winstreak));
                    }
                    else {
                        players.put(player.getUniqueId(), new DuelPlayer(player.getUniqueId()));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @EventHandler(priority = HIGH)
    private void quit(PlayerQuitEvent event){
        final UUID playerUUID = event.getPlayer().getUniqueId();
        final String playerName = event.getPlayer().getName();

        plugin.getMySQLConnector().execute(() -> {
            final DuelPlayer duelPlayer = players.get(playerUUID);
            try (Connection connection = plugin.getMySQLConnector().connection(); PreparedStatement statement =
                    connection.prepareStatement("INSERT INTO " + MySQLConnector.ACTIVE_TABLE +
                            " (uuid, name, wins, kills, deaths, total_games, winstreak) values (?, ?, ?, ?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE " +
                            "wins = " + duelPlayer.getWins() + ", " +
                            "kills = " + duelPlayer.getKills() + ", " +
                            "deaths = " + duelPlayer.getDeaths() + ", " +
                            "total_games = " + duelPlayer.getTotalGames() + ", " +
                            "winstreak = " + duelPlayer.getWinstreak() + ";")) {


                statement.setString(1, playerUUID.toString());
                statement.setString(2, playerName);
                statement.setInt(3, duelPlayer.getWins());
                statement.setInt(4, duelPlayer.getKills());
                statement.setInt(5, duelPlayer.getDeaths());
                statement.setInt(6, duelPlayer.getTotalGames());
                statement.setInt(7, duelPlayer.getWinstreak());

                statement.execute();

            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

}
