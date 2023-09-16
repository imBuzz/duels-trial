package it.buzz.devroom.duel.duel.duel.player.inventory;

import it.buzz.devroom.duel.duel.duel.player.inventory.contents.ArmorContents;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

@Builder
public class InventorySnapshot {

    public static final InventorySnapshot EMPTY_SNAPSHOT;

    static {
        List<InventoryItem> items = new ArrayList<>();
        for (int i = 0; i < 36; i++) {
            items.add(InventoryItem.builder()
                    .itemStack(new SerializedItemStack(Material.AIR, 1))
                    .position(i)
                    .build());
        }
        EMPTY_SNAPSHOT = InventorySnapshot.builder().inventory(items).build();
    }

    private final List<InventoryItem> inventory;
    @Builder.Default
    private final double health = 20;
    @Builder.Default
    private final int exp = 0;
    @Builder.Default
    private final float saturation = 20;
    @Builder.Default
    private final List<PotionEffect> potionEffects = new ArrayList<>();
    @Builder.Default
    private ArmorContents armorContents = ArmorContents.EMPTY_CONTENTS;

    public static InventoryItem.InventoryItemBuilder itemBuilder() {
        return new InventoryItem.InventoryItemBuilder();
    }

    public void apply(Player player) {
        if (player != null && player.isOnline()) {
            player.getInventory().setHelmet(armorContents.getHelmet());
            player.getInventory().setChestplate(armorContents.getChestplate());
            player.getInventory().setLeggings(armorContents.getLeggings());
            player.getInventory().setBoots(armorContents.getBoots());

            if (inventory != null) {
                for (InventoryItem inventoryItem : inventory) {
                    player.getInventory().setItem(inventoryItem.position, inventoryItem.itemStack.toItemStack());
                }
            }

            player.setHealth(health);
            player.setTotalExperience(exp);
            player.setSaturation(saturation);
            player.updateInventory();
        }
    }

    public List<InventoryItem> getInventory() {
        return inventory;
    }

    public ArmorContents getArmorContents() {
        return armorContents;
    }

    public void setArmorContents(ArmorContents armorContents) {
        this.armorContents = armorContents;
    }

    @Builder
    public static final class InventoryItem {
        private final int position;
        private final SerializedItemStack itemStack;

        public int getPosition() {
            return position;
        }

        public SerializedItemStack getItemStack() {
            return itemStack;
        }
    }

    @RequiredArgsConstructor
    public static final class SerializedItemStack {
        private final Material mat;
        private final int amount;

        public ItemStack toItemStack(){
            return new ItemStack(mat, amount);
        }
    }

}