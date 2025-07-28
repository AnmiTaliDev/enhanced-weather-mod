package com.anmitalidev.enhancedweather.weather;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

import java.util.Random;

public class LightRainWeather {
    private static final Random random = new Random();
    
    public static void start(ServerLevel level) {
        // Устанавливаем дождь, но без грозы
        level.setWeatherParameters(0, 12000, true, false);
        
        // Тихий звук начала дождя
        for (Player player : level.players()) {
            level.playSound(null, player.blockPosition(), SoundEvents.WEATHER_RAIN, 
                SoundSource.WEATHER, 0.3F, 1.2F);
        }
    }
    
    public static void update(ServerLevel level) {
        // Уменьшенные частицы дождя
        for (Player player : level.players()) {
            BlockPos playerPos = player.blockPosition();
            
            // Легкие капли дождя
            for (int i = 0; i < 8; i++) {
                double x = playerPos.getX() + (random.nextDouble() - 0.5) * 24;
                double y = playerPos.getY() + random.nextDouble() * 12 + 8;
                double z = playerPos.getZ() + (random.nextDouble() - 0.5) * 24;
                
                // Проверяем, что над позицией есть небо
                BlockPos checkPos = new BlockPos((int)x, (int)y, (int)z);
                if (level.canSeeSky(checkPos)) {
                    level.sendParticles(ParticleTypes.DRIPPING_WATER, x, y, z, 1, 0, -0.1, 0, 0.1);
                }
            }
            
            // Частицы тумана для атмосферы
            if (random.nextInt(3) == 0) {
                double x = playerPos.getX() + (random.nextDouble() - 0.5) * 20;
                double y = playerPos.getY() + random.nextDouble() * 3;
                double z = playerPos.getZ() + (random.nextDouble() - 0.5) * 20;
                
                level.sendParticles(ParticleTypes.CLOUD, x, y, z, 1, 0, 0.05, 0, 0.01);
            }
            
            // Тихие звуки дождя
            if (random.nextInt(80) == 0) {
                level.playSound(null, playerPos, SoundEvents.WEATHER_RAIN, 
                    SoundSource.WEATHER, 0.2F, 1.0F + random.nextFloat() * 0.4F);
            }
        }
        
        // Медленное наполнение котлов водой
        for (Player player : level.players()) {
            BlockPos pos = player.blockPosition();
            for (int i = 0; i < 3; i++) {
                BlockPos checkPos = pos.offset(
                    random.nextInt(16) - 8, 
                    random.nextInt(8) - 4, 
                    random.nextInt(16) - 8
                );
                
                if (level.getBlockState(checkPos).getBlock() == Blocks.CAULDRON && 
                    level.canSeeSky(checkPos) && random.nextInt(20) == 0) {
                    // Логика наполнения котла будет обработана через события
                }
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
        // LightRain не хранит статических данных, но добавляем для единообразия
    }
}