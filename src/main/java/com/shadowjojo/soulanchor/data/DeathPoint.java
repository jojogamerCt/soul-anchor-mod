package com.shadowjojo.soulanchor.data;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class DeathPoint {
    private final BlockPos position;
    private final ResourceKey<Level> dimension;
    private final long timestamp;
    private final String cause;
    private final List<ItemStack> inventory;
    private final Set<String> recoveredItems;

    public DeathPoint(BlockPos pos, ResourceKey<Level> dim, String deathCause, List<ItemStack> items) {
        this.position = pos;
        this.dimension = dim;
        this.timestamp = System.currentTimeMillis();
        this.cause = deathCause;
        this.inventory = items;
        this.recoveredItems = new HashSet<>();
    }

    public BlockPos getPosition() { return position; }
    public ResourceKey<Level> getDimension() { return dimension; }
    public long getTimestamp() { return timestamp; }
    public String getCause() { return cause; }
    public List<ItemStack> getInventory() { return inventory; }
    
    public boolean isItemRecovered(ItemStack item) {
        return recoveredItems.contains(getItemIdentifier(item));
    }
    
    public void markItemAsRecovered(ItemStack item) {
        recoveredItems.add(getItemIdentifier(item));
    }
    
    public boolean areAllItemsRecovered() {
        return inventory.stream().allMatch(this::isItemRecovered);
    }
    
    private String getItemIdentifier(ItemStack item) {
        return item.getItem().toString() + "_" + item.getCount();
    }
} 