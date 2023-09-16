package it.buzz.devroom.duel.duel.duel.player.inventory.contents;

import it.buzz.devroom.duel.duel.duel.player.inventory.InventorySnapshot;
import lombok.Builder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Builder
public class ArmorContents {

    public static final ArmorContents EMPTY_CONTENTS = ArmorContents.builder().build();

    @Builder.Default
    private final InventorySnapshot.SerializedItemStack helmet = new InventorySnapshot.SerializedItemStack(Material.AIR, 1);
    @Builder.Default
    private final InventorySnapshot.SerializedItemStack chestplate = new InventorySnapshot.SerializedItemStack(Material.AIR, 1);
    @Builder.Default
    private final InventorySnapshot.SerializedItemStack leggings = new InventorySnapshot.SerializedItemStack(Material.AIR, 1);
    @Builder.Default
    private final InventorySnapshot.SerializedItemStack boots = new InventorySnapshot.SerializedItemStack(Material.AIR, 1);

    public ItemStack[] asArray() {
        return new ItemStack[]{helmet.toItemStack(),
                chestplate.toItemStack(),
                leggings.toItemStack(),
                boots.toItemStack()};
    }

    public ItemStack getLeggings() {
        return leggings.toItemStack();
    }

    public ItemStack getHelmet() {
        return helmet.toItemStack();
    }

    public ItemStack getBoots() {
        return boots.toItemStack();
    }

    public ItemStack getChestplate() {
        return chestplate.toItemStack();
    }
}