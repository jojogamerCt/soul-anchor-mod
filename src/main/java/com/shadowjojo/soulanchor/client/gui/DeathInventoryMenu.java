package com.shadowjojo.soulanchor.client.gui;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import java.util.List;

public class DeathInventoryMenu extends AbstractContainerMenu {
    private final ItemStackHandler inventory;

    public DeathInventoryMenu(int containerId, Inventory playerInventory, List<ItemStack> deathItems) {
        super(MenuType.GENERIC_9x6, containerId);
        this.inventory = new ItemStackHandler(54);
        
        // Load death items
        for(int i = 0; i < Math.min(deathItems.size(), 54); i++) {
            this.inventory.setStackInSlot(i, deathItems.get(i));
        }

        // Add death inventory slots (view-only)
        for(int row = 0; row < 6; row++) {
            for(int col = 0; col < 9; col++) {
                int index = col + row * 9;
                this.addSlot(new SlotItemHandler(inventory, index, 8 + col * 18, 18 + row * 18) {
                    @Override
                    public boolean mayPickup(Player player) {
                        return false; // Make slots view-only
                    }
                });
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY; // Prevent shift-clicking
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
} 