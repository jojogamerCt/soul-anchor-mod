package com.shadowjojo.soulanchor.tracking;

import com.shadowjojo.soulanchor.data.DeathPoint;
import com.shadowjojo.soulanchor.client.ParticleManager;
import com.shadowjojo.soulanchor.client.gui.DeathInventoryMenu;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.MenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.server.TickTask;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;

import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.ChatFormatting;

public class DeathTracker {
    private static final Map<UUID, List<DeathPoint>> DEATH_POINTS = new HashMap<>();
    private static final Map<UUID, List<ItemStack>> PENDING_INVENTORIES = new HashMap<>();
    private static final int MAX_DEATH_POINTS = 5;

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        // Store exact death position
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        
        BlockPos exactPos = new BlockPos(
            Mth.floor(x),
            Mth.floor(y),
            Mth.floor(z)
        );
        
        // Collect inventory items
        List<ItemStack> inventory = player.getInventory().items.stream()
            .filter(stack -> !stack.isEmpty())
            .map(ItemStack::copy)
            .collect(Collectors.toList());

        DeathPoint deathPoint = new DeathPoint(
            exactPos,
            player.level().dimension(),
            event.getSource().getMsgId(),
            inventory
        );

        DEATH_POINTS.computeIfAbsent(player.getUUID(), k -> new ArrayList<>())
                   .add(0, deathPoint);

        // Store inventory for showing after respawn
        if (!inventory.isEmpty()) {
            PENDING_INVENTORIES.put(player.getUUID(), inventory);
        }

        // Trim list to max size
        List<DeathPoint> points = DEATH_POINTS.get(player.getUUID());
        if (points.size() > MAX_DEATH_POINTS) {
            points.subList(MAX_DEATH_POINTS, points.size()).clear();
        }

        // Send death message with coordinates
        String message = String.format("§c§lDeath location: %.2f, %.2f, %.2f in %s",
            x, y, z,
            deathPoint.getDimension().location()
        );
        player.sendSystemMessage(Component.literal(message));

        // Trigger particles for all nearby players at exact death location
        for (Player nearbyPlayer : player.level().players()) {
            if (nearbyPlayer.distanceTo(player) <= 32) {
                ParticleManager.spawnDeathParticles(player.level(), exactPos, y);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        UUID playerUUID = player.getUUID();

        // Show inventory GUI after respawn if there's a pending inventory
        if (PENDING_INVENTORIES.containsKey(playerUUID)) {
            List<ItemStack> lostItems = PENDING_INVENTORIES.remove(playerUUID);
            
            // Delay the GUI opening slightly to ensure client is ready
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.level().getServer().tell(new TickTask(0, () -> {
                    serverPlayer.openMenu(new MenuProvider() {
                        @Override
                        public Component getDisplayName() {
                            return Component.literal("Items Lost on Death");
                        }

                        @Override
                        public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
                            return new DeathInventoryMenu(containerId, playerInv, lostItems);
                        }
                    });
                }));
            }
        }
    }

    @SubscribeEvent
    public void onItemPickup(ItemPickupEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        ItemStack pickedItem = event.getStack();
        
        List<DeathPoint> deathPoints = DEATH_POINTS.get(player.getUUID());
        if (deathPoints != null && !deathPoints.isEmpty()) {
            DeathPoint lastDeath = deathPoints.get(0);
            
            // Check if this item was from our death inventory
            boolean wasDeathItem = lastDeath.getInventory().stream()
                .anyMatch(deathItem -> ItemStack.matches(deathItem, pickedItem));
            
            if (wasDeathItem) {
                lastDeath.markItemAsRecovered(pickedItem);
                
                // Check if this was the last item
                if (lastDeath.areAllItemsRecovered()) {
                    // Play recovery completion effect
                    if (player instanceof ServerPlayer serverPlayer) {
                        serverPlayer.level().playSound(
                            null,
                            player.getX(), player.getY(), player.getZ(),
                            SoundEvents.PLAYER_LEVELUP,
                            SoundSource.PLAYERS,
                            1.0f, 1.0f
                        );
                        
                        // Add celebration particles
                        for (int i = 0; i < 20; i++) {
                            double x = player.getX() + (player.level().random.nextDouble() - 0.5) * 2;
                            double y = player.getY() + player.level().random.nextDouble() * 2;
                            double z = player.getZ() + (player.level().random.nextDouble() - 0.5) * 2;
                            
                            serverPlayer.serverLevel().sendParticles(
                                ParticleTypes.END_ROD,
                                x, y, z,
                                1,
                                0.0D, 0.1D, 0.0D,
                                0.0D
                            );
                        }
                        
                        player.sendSystemMessage(Component.literal("All items recovered!")
                            .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
                    }
                }
            }
        }
    }

    public static List<DeathPoint> getDeathPoints(UUID playerUUID) {
        return DEATH_POINTS.getOrDefault(playerUUID, new ArrayList<>());
    }
} 