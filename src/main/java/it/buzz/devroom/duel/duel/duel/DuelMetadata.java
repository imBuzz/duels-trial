package it.buzz.devroom.duel.duel.duel;

import it.buzz.devroom.duel.Duel;
import it.buzz.devroom.duel.duel.DuelHandler;
import it.buzz.devroom.duel.duel.duel.map.DuelMap;
import it.buzz.devroom.duel.duel.duel.player.inventory.InventorySnapshot;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record DuelMetadata(UUID player, UUID target, InventorySnapshot kit) {

    public DuelGame makeDuel(Duel plugin, DuelMap duelMap){
        final DuelHandler duelHandler = plugin.getDuelHandler();

        return new DuelGame(plugin, kit, duelMap,
                duelHandler.getPlayer(player),
                duelHandler.getPlayer(target));
    }

    public Set<Player> getOnlinePlayers(){
        Set<Player> pl = new HashSet<>();

        Player player_1 = Bukkit.getPlayer(player);
        if (player_1 != null) pl.add(player_1);

        Player player_2= Bukkit.getPlayer(target);
        if (player_2 != null) pl.add(player_2);

        return pl;
    }

}
