package it.buzz.devroom.duel.duel.duel.player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

@RequiredArgsConstructor @Getter
public class DuelPlayer {

    private final UUID uuid;
    @Setter private int wins, kills, deaths, totalGames, winstreak;

    public DuelPlayer(UUID uuid, int wins, int kills, int deaths, int totalGames, int winstreak){
        this(uuid);
        this.wins = wins;
        this.kills = kills;
        this.deaths = deaths;
        this.totalGames = totalGames;
        this.winstreak = winstreak;
    }

    public void incrementWins(int amount){
        wins += amount;
        wins = wins < 0 ? 0 : wins;
    }

    public void incrementKills(int amount){
        kills += amount;
        kills = kills < 0 ? 0 : kills;
    }


    public void incrementDeaths(int amount){
        deaths += amount;
        deaths = deaths < 0 ? 0 : deaths;
    }

    public void incrementTotalGames(int amount){
        totalGames += amount;
        totalGames = totalGames < 0 ? 0 : totalGames;
    }


    public void incrementWinstreak(int amount){
        winstreak += amount;
        winstreak = winstreak < 0 ? 0 : winstreak;
    }


    @Setter private Location oldLocation;

    public Player handle(){
        return Bukkit.getPlayer(uuid);
    }

    public UUID getUUID() {
        return uuid;
    }
}

