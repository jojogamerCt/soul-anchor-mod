package com.shadowjojo.soulanchor.client;

import com.shadowjojo.soulanchor.data.DeathPoint;
import com.shadowjojo.soulanchor.tracking.DeathTracker;
import com.shadowjojo.soulanchor.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class CompassHandler {
    private static final int PARTICLE_SPACING = 2;
    private static final int MAX_DISTANCE = 50;
    private static final double SOUND_DISTANCE = 10.0;
    private long lastSoundTime = 0;

    @SubscribeEvent
    public void onClientTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player == null) return;
        
        Player player = event.player;
        ItemStack heldItem = player.getMainHandItem();
        
        if (heldItem.is(ModItems.SOUL_COMPASS.get())) {
            List<DeathPoint> deathPoints = DeathTracker.getDeathPoints(player.getUUID());
            
            if (!deathPoints.isEmpty()) {
                DeathPoint lastDeath = deathPoints.get(0);
                BlockPos deathPos = lastDeath.getPosition();
                
                // Only work in the same dimension
                if (player.level().dimension().equals(lastDeath.getDimension())) {
                    Vec3 playerPos = player.position();
                    Vec3 targetPos = Vec3.atCenterOf(deathPos);
                    double distance = playerPos.distanceTo(targetPos);

                    // Don't show effects if all items are recovered
                    if (!lastDeath.areAllItemsRecovered()) {
                        // Play sound when getting closer
                        if (distance <= SOUND_DISTANCE) {
                            long currentTime = System.currentTimeMillis();
                            if (currentTime - lastSoundTime > 1000) { // Play sound every second
                                float pitch = (float) (1.0f + (SOUND_DISTANCE - distance) / SOUND_DISTANCE);
                                float volume = (float) (1.0f - distance / SOUND_DISTANCE);
                                player.level().playLocalSound(
                                    playerPos.x, playerPos.y, playerPos.z,
                                    SoundEvents.SOUL_ESCAPE,
                                    SoundSource.AMBIENT,
                                    volume * 0.5f, // Reduced volume
                                    pitch,
                                    false
                                );
                                lastSoundTime = currentTime;
                            }
                        }

                        // Create particle trail
                        createParticleTrail(player, playerPos, targetPos, distance);
                    }
                    
                    // Add breaking effect regardless of item recovery
                    if (heldItem.getDamageValue() >= heldItem.getMaxDamage() * 0.9) {
                        addBreakingEffect(player, heldItem);
                    }
                }
            }
        }
    }

    private void createParticleTrail(Player player, Vec3 playerPos, Vec3 targetPos, double distance) {
        Vec3 direction = targetPos.subtract(playerPos).normalize();
        int particleCount = (int) Math.min(distance / PARTICLE_SPACING, MAX_DISTANCE / PARTICLE_SPACING);
        
        for (int i = 1; i <= particleCount; i++) {
            double progress = i * PARTICLE_SPACING;
            Vec3 particlePos = playerPos.add(direction.multiply(progress, progress, progress));
            
            // Main trail particles
            player.level().addParticle(
                ParticleTypes.SOUL_FIRE_FLAME,
                particlePos.x,
                particlePos.y + 0.5,
                particlePos.z,
                0.0D, 0.0D, 0.0D
            );
            
            // Ambient particles
            if (player.level().random.nextFloat() < 0.2f) {
                addAmbientParticle(player, particlePos);
            }
        }
    }

    private void addAmbientParticle(Player player, Vec3 pos) {
        double offsetX = player.level().random.nextDouble() * 0.5 - 0.25;
        double offsetY = player.level().random.nextDouble() * 0.5 - 0.25;
        double offsetZ = player.level().random.nextDouble() * 0.5 - 0.25;
        
        player.level().addParticle(
            ParticleTypes.SOUL,
            pos.x + offsetX,
            pos.y + 0.5 + offsetY,
            pos.z + offsetZ,
            0.0D, 0.05D, 0.0D
        );
    }

    private void addBreakingEffect(Player player, ItemStack compass) {
        if (player.level().getGameTime() % 20 == 0) { // Every second
            Vec3 pos = player.position();
            for (int i = 0; i < 5; i++) {
                player.level().addParticle(
                    ParticleTypes.SOUL,
                    pos.x + (player.level().random.nextDouble() - 0.5) * 0.5,
                    pos.y + player.level().random.nextDouble() * 2,
                    pos.z + (player.level().random.nextDouble() - 0.5) * 0.5,
                    0.0D, 0.1D, 0.0D
                );
            }
            player.level().playLocalSound(
                pos.x, pos.y, pos.z,
                SoundEvents.ITEM_BREAK,
                SoundSource.PLAYERS,
                0.8f, 0.8f + player.level().random.nextFloat() * 0.4f,
                false
            );
        }
    }
} 