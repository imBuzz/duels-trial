package it.buzz.devroom.duel.duel.duel;

import it.buzz.devroom.duel.Duel;
import it.buzz.devroom.duel.duel.duel.map.DuelMap;
import it.buzz.devroom.duel.duel.duel.player.DuelPlayer;
import it.buzz.devroom.duel.duel.duel.player.inventory.InventorySnapshot;
import it.buzz.devroom.duel.duel.duel.reason.DuelEndingReason;
import it.buzz.devroom.duel.duel.duel.state.DuelState;
import it.buzz.devroom.duel.holder.AbstractPluginHolder;
import lombok.experimental.PackagePrivate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DuelGame extends AbstractPluginHolder {

    private final UUID uuid = UUID.randomUUID();

    @PackagePrivate
    final Map<UUID, DuelPlayer> players = new ConcurrentHashMap<>();

    @PackagePrivate
    final InventorySnapshot kit;
    private final DuelMap duelMap;

    @PackagePrivate
    DuelState state;
    private BukkitTask activeBukkitTask;

    public DuelGame(Duel plugin, InventorySnapshot kit, DuelMap duelMap, DuelPlayer... dPlayers) {
        super(plugin);
        this.kit = kit;
        this.duelMap = duelMap;

        for (DuelPlayer dPlayer : dPlayers)
            players.put(dPlayer.getUUID(), dPlayer);
    }


    public void start() {
        state = DuelState.STARTING;
        players.values().forEach(player -> player.incrementTotalGames(1));

        final DuelPlayer[] array = players.values().toArray(new DuelPlayer[0]);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player player = array[0].handle();

            kit.apply(player);
            array[0].setOldLocation(player.getLocation());
            player.teleport(duelMap.getPoint1());

        }, 1L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player player = array[1].handle();

            kit.apply(player);
            array[1].setOldLocation(player.getLocation());
            player.teleport(duelMap.getPoint2());

        }, 1L);

        activeBukkitTask = new StartingTask(this).runTaskTimerAsynchronously(plugin, 0L, 20L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.getPluginManager().registerEvents(this, plugin), 20L);
    }


    public void stop(DuelEndingReason reason) {
        state = DuelState.ENDING;

        HandlerList.unregisterAll(this);
        if (activeBukkitTask != null) {
            Bukkit.getScheduler().cancelTask(activeBukkitTask.getTaskId());
        }
    }

    @EventHandler
    private void onMove(PlayerMoveEvent e) {
        if (state == DuelState.STARTING && players.containsKey(e.getPlayer().getUniqueId()) && e.getFrom().getZ() != e.getTo().getZ()
                && e.getFrom().getX() != e.getTo().getX()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onDeath(PlayerDeathEvent event){
        final Player player = event.getEntity();
        if (!players.containsKey(player.getUniqueId())) return;

        event.getDrops().clear();

        Bukkit.getScheduler().runTaskLater(plugin, () -> player.spigot().respawn(), 1L);

        players.values().forEach(dPlayer -> {
            if (dPlayer.handle() == player) {
                player.sendMessage(ChatColor.RED + "You lost");

                dPlayer.incrementDeaths(1);
                dPlayer.incrementWinstreak(Integer.MAX_VALUE * -1);
            }
            else {
                dPlayer.handle().sendMessage(ChatColor.GREEN + "You won");

                if (player.getKiller() == dPlayer.handle())
                    dPlayer.incrementKills(1);

                dPlayer.incrementWins(1);
                dPlayer.incrementWinstreak(1);
            }

            Bukkit.getScheduler().runTaskLater(plugin, () -> dPlayer.handle().teleport(dPlayer.getOldLocation()), 1L);
        });

        stop(DuelEndingReason.NORMALLY);
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event){
        event.getPlayer().getInventory().clear();

        DuelPlayer duelPlayer = players.remove(event.getPlayer().getUniqueId());
        if (duelPlayer == null) return;

        duelPlayer.incrementDeaths(1);
        duelPlayer.incrementWinstreak(Integer.MAX_VALUE * -1);

        players.values().forEach(dPlayer -> {
            dPlayer.handle().sendMessage(ChatColor.GREEN + "You won");
            Bukkit.getScheduler().runTaskLater(plugin, () -> dPlayer.handle().teleport(dPlayer.getOldLocation()), 1L);
        });

        stop(DuelEndingReason.NORMALLY);
    }


}
