package com.anmitalidev.enhancedweather.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class WeatherSounds {
    private static final Random random = new Random();
    private static int soundTimer = 0;
    
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        Player player = mc.player;
        
        if (level == null || player == null) return;
        
        soundTimer++;
        
        // Определяем текущую погоду и воспроизводим соответствующие звуки
        if (level.isThundering() && level.getRainLevel(1.0F) > 0.8F) {
            playStormSounds(level, player);
        } else if (level.isRaining() && level.getRainLevel(1.0F) < 0.5F) {
            playLightRainSounds(level, player);
        } else if (level.isRaining() && level.getRainLevel(1.0F) > 0.9F) {
            playFloodSounds(level, player);
        }
    }
    
    private static void playStormSounds(ClientLevel level, Player player) {
        // Звуки ветра через ванильные звуки
        if (soundTimer % 120 == 0 && random.nextInt(3) == 0) {
            level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                SoundEvents.ELYTRA_FLYING, SoundSource.WEATHER, 0.3F, 0.5F, false);
        }
        
        // Далекий гром
        if (soundTimer % 100 == 0 && random.nextInt(4) == 0) {
            level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 0.4F, 0.6F, false);
        }
    }
    
    private static void playLightRainSounds(ClientLevel level, Player player) {
        // Тихие звуки дождя через ванильные звуки
        if (soundTimer % 200 == 0 && random.nextInt(3) == 0) {
            level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 0.2F, 1.2F, false);
        }
    }
    
    private static void playFloodSounds(ClientLevel level, Player player) {
        // Звуки воды через ванильные звуки
        if (soundTimer % 150 == 0 && random.nextInt(2) == 0) {
            level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMBIENT_UNDERWATER_LOOP, SoundSource.WEATHER, 0.3F, 1.0F, false);
        }
        
        // Булькающие звуки
        if (soundTimer % 80 == 0 && random.nextInt(3) == 0) {
            level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                SoundEvents.GENERIC_SPLASH, SoundSource.WEATHER, 0.2F, 0.8F, false);
        }
    }
}