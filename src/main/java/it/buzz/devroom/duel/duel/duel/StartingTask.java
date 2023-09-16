package it.buzz.devroom.duel.duel.duel;

import it.buzz.devroom.duel.duel.duel.DuelGame;
import it.buzz.devroom.duel.duel.duel.state.DuelState;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

@RequiredArgsConstructor
public class StartingTask extends BukkitRunnable {

    private final DuelGame duelGame;
    private int seconds = 10;

    @Override
    public void run() {
        duelGame.players.values().forEach(dPlayer -> dPlayer.handle().sendMessage("Starting in " + seconds));
        seconds--;

        if (seconds <= 0){
            duelGame.state = DuelState.IN_GAME;
            this.cancel();
        }
    }


}
