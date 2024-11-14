package com.shadowjojo.soulanchor.client;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ParticleManager {
    public static void spawnDeathParticles(Level level, BlockPos pos, double exactY) {
        Vec3 center = new Vec3(
            pos.getX() + 0.5,
            exactY,
            pos.getZ() + 0.5
        );
        
        // Spawn a spiral of soul particles
        for(int i = 0; i < 36; i++) {
            double angle = i * Math.PI / 18;
            double radius = 0.5;
            
            double x = center.x + Math.cos(angle) * radius;
            double y = center.y;
            double z = center.z + Math.sin(angle) * radius;
            
            level.addParticle(
                ParticleTypes.SOUL_FIRE_FLAME,
                x, y, z,
                0.0D, 0.05D, 0.0D
            );
        }
        
        // Add some soul particles in the center
        for(int i = 0; i < 10; i++) {
            level.addParticle(
                ParticleTypes.SOUL,
                center.x, center.y + 0.5, center.z,
                0.0D, 0.1D, 0.0D
            );
        }
    }
} 