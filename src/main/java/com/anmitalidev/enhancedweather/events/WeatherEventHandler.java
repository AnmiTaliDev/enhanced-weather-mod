package com.anmitalidev.enhancedweather.events;

import com.anmitalidev.enhancedweather.weather.WeatherManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber
public class WeatherEventHandler {
    private static final Random random = new Random();
    private static int tickCounter = 0;
    
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        tickCounter++;
        
        // Обработка эффектов погоды каждые 20 тиков (1 секунда)
        if (tickCounter % 20 == 0) {
            for (ServerLevel level : event.getServer().getAllLevels()) {
                if (level.dimension() != Level.OVERWORLD) continue;
                
                WeatherManager.WeatherType currentWeather = WeatherManager.getCurrentWeather(level);
                
                switch (currentWeather) {
                    case STORM:
                        handleStormEffects(level);
                        break;
                    case LIGHT_RAIN:
                        handleLightRainEffects(level);
                        break;
                    case FLOOD:
                        handleFloodEffects(level);
                        break;
                }
            }
        }
    }
    
    private static void handleStormEffects(ServerLevel level) {
        // Дополнительный урон от молний
        for (Player player : level.players()) {
            // Небольшой шанс получить урон от ветра, если игрок на открытом воздухе
            if (level.canSeeSky(player.blockPosition()) && random.nextInt(600) == 0) {
                player.hurt(level.damageSources().lightningBolt(), 1.0F);
                level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_HURT, 
                    SoundSource.PLAYERS, 0.5F, 1.0F);
            }
            
            // Замедление движения из-за сильного ветра
            if (level.canSeeSky(player.blockPosition()) && random.nextInt(100) == 0) {
                // Добавляем эффект замедления на короткое время
                player.push(-0.1 + random.nextDouble() * 0.2, 0, -0.1 + random.nextDouble() * 0.2);
            }
        }
    }
    
    private static void handleLightRainEffects(ServerLevel level) {
        // Медленное наполнение котлов
        for (Player player : level.players()) {
            BlockPos playerPos = player.blockPosition();
            
            // Проверяем область вокруг игрока на наличие котлов
            for (int x = -8; x <= 8; x++) {
                for (int z = -8; z <= 8; z++) {
                    for (int y = -3; y <= 3; y++) {
                        BlockPos checkPos = playerPos.offset(x, y, z);
                        BlockState state = level.getBlockState(checkPos);
                        
                        if (state.getBlock() instanceof CauldronBlock && level.canSeeSky(checkPos)) {
                            if (random.nextInt(100) == 0) { // 1% шанс каждую секунду
                                if (state.getBlock() == Blocks.CAULDRON) {
                                    // Пустой котел -> 1 уровень воды
                                    level.setBlock(checkPos, Blocks.WATER_CAULDRON.defaultBlockState()
                                        .setValue(LayeredCauldronBlock.LEVEL, 1), 3);
                                } else if (state.getBlock() == Blocks.WATER_CAULDRON) {
                                    int currentLevel = state.getValue(LayeredCauldronBlock.LEVEL);
                                    if (currentLevel < 3) {
                                        level.setBlock(checkPos, state.setValue(LayeredCauldronBlock.LEVEL, currentLevel + 1), 3);
                                    }
                                }
                                
                                // Звук капли
                                level.playSound(null, checkPos, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON,
                                    SoundSource.BLOCKS, 0.3F, 1.0F + random.nextFloat() * 0.4F);
                            }
                        }
                    }
                }
            }
        }
        
        // Ускоренный рост растений
        for (Player player : level.players()) {
            BlockPos pos = player.blockPosition();
            for (int i = 0; i < 3; i++) {
                BlockPos cropPos = pos.offset(
                    random.nextInt(16) - 8,
                    random.nextInt(8) - 4,
                    random.nextInt(16) - 8
                );
                
                BlockState state = level.getBlockState(cropPos);
                if (state.getBlock() instanceof net.minecraft.world.level.block.CropBlock && 
                    level.canSeeSky(cropPos) && random.nextInt(50) == 0) {
                    
                    // Попытка ускорить рост
                    state.getBlock().randomTick(state, level, cropPos, level.random);
                }
            }
        }
    }
    
    private static void handleFloodEffects(ServerLevel level) {
        // Замедление движения в воде паводка
        for (Player player : level.players()) {
            if (player.isInWater() && random.nextInt(20) == 0) {
                // Дополнительное сопротивление воды
                player.push(
                    -player.getDeltaMovement().x * 0.1,
                    0,
                    -player.getDeltaMovement().z * 0.1
                );
            }
            
            // Возможность получить урон от сильного течения
            if (player.isInWater() && level.getBlockState(player.blockPosition()).getBlock() == Blocks.WATER &&
                random.nextInt(200) == 0) {
                
                player.hurt(level.damageSources().drown(), 0.5F);
                level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_SPLASH,
                    SoundSource.PLAYERS, 0.8F, 0.8F);
            }
        }
    }
    
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level)) return;
        
        WeatherManager.WeatherType currentWeather = WeatherManager.getCurrentWeather(level);
        
        // Эффекты погоды на мобов
        switch (currentWeather) {
            case STORM:
                // Мобы пытаются укрыться от бури
                if (level.canSeeSky(entity.blockPosition()) && random.nextInt(100) == 0) {
                    // Паника у животных
                    if (entity instanceof net.minecraft.world.entity.animal.Animal) {
                        entity.push(
                            (random.nextDouble() - 0.5) * 0.3,
                            0.1,
                            (random.nextDouble() - 0.5) * 0.3
                        );
                    }
                }
                break;
                
            case FLOOD:
                // Мобы получают урон в воде паводка (кроме водных)
                if (entity.isInWater() && !(entity instanceof net.minecraft.world.entity.animal.WaterAnimal) &&
                    random.nextInt(400) == 0) {
                    entity.hurt(level.damageSources().drown(), 1.0F);
                }
                break;
        }
    }
    
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        
        WeatherManager.WeatherType currentWeather = WeatherManager.getCurrentWeather(level);
        
        // Запрещаем размещение огня во время дождя
        if ((currentWeather == WeatherManager.WeatherType.LIGHT_RAIN || 
             currentWeather == WeatherManager.WeatherType.STORM) &&
            event.getPlacedBlock().getBlock() == Blocks.FIRE &&
            level.canSeeSky(event.getPos())) {
            
            event.setCanceled(true);
            
            if (event.getEntity() instanceof Player player) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "Fire cannot be placed during rain!"));
            }
        }
    }
}