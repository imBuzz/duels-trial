package it.buzz.devroom.duel.duel.duel.map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class DuelMap {

    @Getter private final String name, world;
    private final String point1, point2;
    @Getter @Setter private transient boolean inUse;

    public Location getPoint1() {
        return deserializeLocation(world, point1);
    }
    public Location getPoint2() {
        return deserializeLocation(world, point2);
    }

    public static Location deserializeLocation(String world, String string) {
        if (string == null) return null;
        String[] split = string.split(":");
        return new Location(Bukkit.getWorld(world), Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Float.parseFloat(split[3]), Float.parseFloat(split[4]));
    }

}
