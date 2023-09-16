package it.buzz.devroom.duel.holder;

import it.buzz.devroom.duel.Duel;
import org.bukkit.event.Listener;

public abstract class AbstractPluginHolder implements Listener {

    protected final Duel plugin;

    protected AbstractPluginHolder(Duel plugin) {
        this.plugin = plugin;
    }

    public Duel getPlugin() {
        return plugin;
    }
}
