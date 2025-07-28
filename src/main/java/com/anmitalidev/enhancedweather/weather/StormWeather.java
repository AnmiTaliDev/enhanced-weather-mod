package com.anmitalidev.enhancedweather.weather;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;

import java.util.Random;

public class StormWeather {
    private static final Random random = new Random();
    
    public static void start(ServerLevel level) {
        level.setWeatherParameters(0, 6000, true, true);
        
        // Звук начала бури
        for (Player player : level.players()) {
            level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, 
                SoundSource.WEATHER, 1.0F, 0.8F);
        }
    }
    
    public static void update(ServerLevel level) {
        // Усиленные молнии
        if (random.nextInt(40) == 0) {
            for (Player player : level.players()) {
                BlockPos pos = player.blockPosition().offset(
                    random.nextInt(64) - 32, 0, random.nextInt(64) - 32);
                pos = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, pos);
                
                LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
                if (lightning != null) {
                    lightning.moveTo(pos.getX(), pos.getY(), pos.getZ());
                    level.addFreshEntity(lightning);
                }
            }
        }
        
        // Частицы ветра и пыли
        for (Player player : level.players()) {
            BlockPos playerPos = player.blockPosition();
            
            // Частицы пыли/листьев в воздухе
            for (int i = 0; i < 15; i++) {
                double x = playerPos.getX() + (random.nextDouble() - 0.5) * 32;
                double y = playerPos.getY() + random.nextDouble() * 10 + 5;
                double z = playerPos.getZ() + (random.nextDouble() - 0.5) * 32;
                
                double velX = (random.nextDouble() - 0.5) * 0.5;
                double velY = -random.nextDouble() * 0.2;
                double velZ = (random.nextDouble() - 0.5) * 0.5;
                
                level.sendParticles(ParticleTypes.POOF, x, y, z, 1, velX, velY, velZ, 0.1);
            }
            
            // Звуки ветра
            if (random.nextInt(100) == 0) {
                level.playSound(null, playerPos, SoundEvents.ELYTRA_FLYING, 
                    SoundSource.WEATHER, 0.3F, 0.5F);
            }
        }
        
        // Затемнение (через частицы тумана)
        for (Player player : level.players()) {
            BlockPos pos = player.blockPosition();
            for (int i = 0; i < 5; i++) {
                double x = pos.getX() + (random.nextDouble() - 0.5) * 16;
                double y = pos.getY() + random.nextDouble() * 8;
                double z = pos.getZ() + (random.nextDouble() - 0.5) * 16;
                
                level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 1, 0, 0, 0, 0.02);
            }
        }
    }
    
    public static void stop(ServerLevel level) {
        level.setWeatherParameters(6000, 0, false, false);
    }
    
    /**
     * Memory leak cleanup
     */
    public static void cleanup() {
        // Storm не хранит статических данных, но добавляем для единообразия
    }
}