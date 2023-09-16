package it.buzz.devroom.duel.duel.commands;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.buzz.devroom.duel.Duel;
import it.buzz.devroom.duel.duel.duel.DuelMetadata;
import it.buzz.devroom.duel.duel.duel.player.inventory.InventorySnapshot;
import it.buzz.devroom.duel.holder.AbstractPluginHolder;
import it.buzz.devroom.duel.holder.Startable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.UUID;
import java.util.function.BiConsumer;

public class DuelCommandHandler extends AbstractPluginHolder implements Startable {

    //          VICTIM - DUEL METADATA
    private final static Cache<UUID, DuelMetadata> INVITES = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(60)).build();

    public DuelCommandHandler(Duel plugin) {
        super(plugin);
    }

    @Override
    public void start() {
        registerCommand("duel", (sender, args) -> {
            // /duel
            if (args.length < 1){
                sender.sendMessage(ChatColor.AQUA + "Duel made by Buzz");
                return;
            }

            switch (args[0].toLowerCase()){
                case "invite" -> {
                    if (args.length < 2){
                        sender.sendMessage(ChatColor.RED + "Usage: /duel invite <player> [kit]");
                        return;
                    }

                    Player target = Bukkit.getPlayerExact(args[1]);
                    if (target == null){
                        sender.sendMessage(ChatColor.RED + "This player is not online!");
                        return;
                    }

                    if (target == sender){
                        sender.sendMessage(ChatColor.RED + "You cannot invite yourself!");
                        return;
                    }

                    if (INVITES.asMap().containsKey(target.getUniqueId())){
                        sender.sendMessage(ChatColor.RED + "This player has already an invite");
                        return;
                    }

                    InventorySnapshot kit = plugin.getDuelHandler().getKit("default");
                    if (args.length >= 3){
                        if (plugin.getDuelHandler().isKitExists(args[2]))
                            kit = plugin.getDuelHandler().getKit(args[2]);
                    }

                    var metadata = new DuelMetadata(((Player) sender).getUniqueId(), target.getUniqueId(), kit);
                    INVITES.put(target.getUniqueId(), metadata);

                    sender.sendMessage(ChatColor.GREEN + "You have invited " + target.getName());
                    target.sendMessage(ChatColor.GREEN + "You have been invited by: " + sender.getName());
                }
                case "accept" -> {
                    Player player = (Player) sender;
                    if (INVITES.asMap().containsKey(player.getUniqueId())){
                        var metadata = INVITES.asMap().get(player.getUniqueId());
                        if (Bukkit.getPlayer(metadata.player()) != null){
                            plugin.getDuelHandler().acceptDuel(metadata);
                        }
                        else {
                            sender.sendMessage(ChatColor.RED + "Your opponent went offline");
                        }
                    }
                    else {
                        sender.sendMessage(ChatColor.RED + "You don't have an invite to a duel");
                    }
                }
                default -> {
                    sender.sendMessage(ChatColor.RED + "Usage: /duel invite/accept");
                }
            }
        });
    }

    @Override
    public void stop() {

    }


    private void registerCommand(String command, BiConsumer<CommandSender, String[]> executor) {
        plugin.getCommand(command).setExecutor((sender, command1, label, args) -> {
            executor.accept(sender, args);
            return true;
        });
    }


}
