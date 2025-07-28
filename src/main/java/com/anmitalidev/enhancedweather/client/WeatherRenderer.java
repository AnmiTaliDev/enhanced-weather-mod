package com.anmitalidev.enhancedweather.client;

import com.anmitalidev.enhancedweather.weather.WeatherManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class WeatherRenderer {
    private static final Random random = new Random();
    private static float stormDarkness = 0.0F;
    private static int windParticleTimer = 0;
    
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) return;
        
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        Player player = mc.player;
        
        if (level == null || player == null) return;
        
        // Получаем текущую погоду (на клиенте используем приблизительную логику)
        WeatherManager.WeatherType currentWeather = getCurrentClientWeather(level);
        
        switch (currentWeather) {
            case STORM:
                renderStormEffects(event, level, player);
                break;
            case LIGHT_RAIN:
                renderLightRainEffects(event, level, player);
                break;
            case FLOOD:
                renderFloodEffects(event, level, player);
                break;
            default:
                stormDarkness = Math.max(0.0F, stormDarkness - 0.02F);
                break;
        }
    }
    
    private static WeatherManager.WeatherType getCurrentClientWeather(ClientLevel level) {
        // Простая логика определения погоды на клиенте
        if (level.isThundering() && level.getRainLevel(1.0F) > 0.8F) {
            // Проверяем интенсивность молний для определения бури
            return WeatherManager.WeatherType.STORM;
        } else if (level.isRaining() && level.getRainLevel(1.0F) < 0.5F) {
            return WeatherManager.WeatherType.LIGHT_RAIN;
        } else if (level.isRaining() && level.getRainLevel(1.0F) > 0.9F) {
            // Очень сильный дождь может означать паводок
            return WeatherManager.WeatherType.FLOOD;
        }
        
        return WeatherManager.WeatherType.CLEAR;
    }
    
    private static void renderStormEffects(RenderLevelStageEvent event, ClientLevel level, Player player) {
        // Увеличиваем затемнение
        stormDarkness = Math.min(0.4F, stormDarkness + 0.01F);
        
        // Применяем затемнение
        if (stormDarkness > 0.0F) {
            // Затемнение реализуется через изменение уровня освещения
            LightTexture lightTexture = Minecraft.getInstance().gameRenderer.lightTexture();
            // Здесь должна быть логика затемнения, но это требует более сложной реализации
        }
        
        // Частицы ветра
        windParticleTimer++;
        if (windParticleTimer % 3 == 0) {
            createWindParticles(level, player);
        }
        
        // Дополнительные частицы бури
        createStormParticles(level, player);
    }
    
    private static void renderLightRainEffects(RenderLevelStageEvent event, ClientLevel level, Player player) {
        // Мягкие частицы тумана
        if (random.nextInt(5) == 0) {
            createMistParticles(level, player);
        }
        
        // Уменьшенные частицы дождя
        createLightRainParticles(level, player);
    }
    
    private static void renderFloodEffects(RenderLevelStageEvent event, ClientLevel level, Player player) {
        // Частицы брызг и пара
        createFloodParticles(level, player);
        
        // Эффект повышенной влажности
        if (random.nextInt(4) == 0) {
            createMistParticles(level, player);
        }
    }
    
    private static void createWindParticles(ClientLevel level, Player player) {
        BlockPos playerPos = player.blockPosition();
        
        for (int i = 0; i < 8; i++) {
            double x = playerPos.getX() + (random.nextDouble() - 0.5) * 24;
            double y = playerPos.getY() + random.nextDouble() * 8 + 3;
            double z = playerPos.getZ() + (random.nextDouble() - 0.5) * 24;
            
            double velX = (random.nextDouble() - 0.5) * 0.8;
            double velY = -random.nextDouble() * 0.1;
            double velZ = (random.nextDouble() - 0.5) * 0.8;
            
            level.addParticle(ParticleTypes.POOF, x, y, z, velX, velY, velZ);
        }
    }
    
    private static void createStormParticles(ClientLevel level, Player player) {
        BlockPos pos = player.blockPosition();
        
        // Частицы пыли и мусора
        for (int i = 0; i < 5; i++) {
            double x = pos.getX() + (random.nextDouble() - 0.5) * 20;
            double y = pos.getY() + random.nextDouble() * 6;
            double z = pos.getZ() + (random.nextDouble() - 0.5) * 20;
            
            level.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0, 0.02, 0);
        }
    }
    
    private static void createLightRainParticles(ClientLevel level, Player player) {
        BlockPos pos = player.blockPosition();
        
        // Легкие капли
        for (int i = 0; i < 6; i++) {
            double x = pos.getX() + (random.nextDouble() - 0.5) * 16;
            double y = pos.getY() + random.nextDouble() * 10 + 5;
            double z = pos.getZ() + (random.nextDouble() - 0.5) * 16;
            
            if (level.canSeeSky(new BlockPos((int)x, (int)y, (int)z))) {
                level.addParticle(ParticleTypes.DRIPPING_WATER, x, y, z, 0, -0.1, 0);
            }
        }
    }
    
    private static void createMistParticles(ClientLevel level, Player player) {
        BlockPos pos = player.blockPosition();
        
        double x = pos.getX() + (random.nextDouble() - 0.5) * 12;
        double y = pos.getY() + random.nextDouble() * 2;
        double z = pos.getZ() + (random.nextDouble() - 0.5) * 12;
        
        level.addParticle(ParticleTypes.CLOUD, x, y, z, 0, 0.02, 0);
    }
    
    private static void createFloodParticles(ClientLevel level, Player player) {
        BlockPos pos = player.blockPosition();
        
        // Брызги воды
        for (int i = 0; i < 8; i++) {
            double x = pos.getX() + (random.nextDouble() - 0.5) * 18;
            double y = pos.getY() + random.nextDouble() * 3;
            double z = pos.getZ() + (random.nextDouble() - 0.5) * 18;
            
            level.addParticle(ParticleTypes.SPLASH, x, y, z, 0, 0.1, 0);
        }
    }
}