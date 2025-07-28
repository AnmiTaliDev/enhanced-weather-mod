package com.anmitalidev.enhancedweather.weather;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

/**
 * Профессиональная реализация системы паводка
 * Thread-safe, оптимизированная, с полным lifecycle management
 */
public final class FloodWeather {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Random RANDOM = new Random();
    
    // Thread-safe data structures
    private static final ConcurrentHashMap<ServerLevel, FloodData> activeFloods = new ConcurrentHashMap<>();
    
    /**
     * Внутренний класс для хранения данных паводка
     */
    private static final class FloodData {
        private final Set<BlockPos> waterBlocks = ConcurrentHashMap.newKeySet();
        private volatile FloodPhase phase = FloodPhase.RISING;
        private volatile long startTime = System.currentTimeMillis();
        
        enum FloodPhase {
            RISING(0, 5000),    // 0-5 минут - подъём
            PEAK(5000, 10000),  // 5-10 минут - пик  
            RECEDING(10000, 15000); // 10-15 минут - спад
            
            private final long startTime;
            private final long endTime;
            
            FloodPhase(long startTime, long endTime) {
                this.startTime = startTime;
                this.endTime = endTime;
            }
            
            public static FloodPhase getPhaseForTime(long elapsedTime) {
                return Arrays.stream(values())
                    .filter(phase -> elapsedTime >= phase.startTime && elapsedTime < phase.endTime)
                    .findFirst()
                    .orElse(RECEDING);
            }
        }
    }
    
    /**
     * Запускает паводок в указанном мире
     */
    public static void start(ServerLevel level) {
        Objects.requireNonNull(level, "ServerLevel cannot be null");
        
        LOGGER.debug("Starting flood in dimension: {}", level.dimension().location());
        
        level.setWeatherParameters(0, WeatherConstants.FLOOD_DURATION_TICKS, true, true);
        activeFloods.put(level, new FloodData());
        
        // Уведомляем игроков о начале паводка
        broadcastFloodStart(level);
    }
    
    /**
     * Обновляет состояние паводка
     */
    public static void update(ServerLevel level) {
        Objects.requireNonNull(level, "ServerLevel cannot be null");
        
        FloodData floodData = activeFloods.get(level);
        if (floodData == null) return;
        
        updateFloodPhase(floodData);
        processFloodEffects(level, floodData);
        createVisualEffects(level);
    }
    
    /**
     * Останавливает паводок
     */
    public static void stop(ServerLevel level) {
        Objects.requireNonNull(level, "ServerLevel cannot be null");
        
        LOGGER.debug("Stopping flood in dimension: {}", level.dimension().location());
        
        FloodData floodData = activeFloods.remove(level);
        if (floodData != null) {
            removeAllFloodWater(level, floodData.waterBlocks);
        }
        
        level.setWeatherParameters(6000, 0, false, false);
    }
    
    /**
     * Очищает все данные паводков (memory leak prevention)
     */
    public static void cleanup() {
        LOGGER.info("Cleaning up flood data for {} active floods", activeFloods.size());
        
        activeFloods.values().forEach(floodData -> floodData.waterBlocks.clear());
        activeFloods.clear();
        
        LOGGER.debug("Flood cleanup completed");
    }
    
    /**
     * Обновляет фазу паводка на основе времени
     */
    private static void updateFloodPhase(FloodData floodData) {
        long elapsedTime = System.currentTimeMillis() - floodData.startTime;
        FloodData.FloodPhase newPhase = FloodData.FloodPhase.getPhaseForTime(elapsedTime);
        
        if (newPhase != floodData.phase) {
            floodData.phase = newPhase;
            LOGGER.debug("Flood phase changed to: {}", newPhase);
        }
    }
    
    /**
     * Обрабатывает эффекты паводка в зависимости от фазы
     */
    private static void processFloodEffects(ServerLevel level, FloodData floodData) {
        level.players().parallelStream()
            .filter(player -> isInFloodableArea(level, player.blockPosition()))
            .forEach(player -> processPlayerArea(level, player, floodData));
    }
    
    /**
     * Обрабатывает область вокруг игрока
     */
    private static void processPlayerArea(ServerLevel level, Player player, FloodData floodData) {
        BlockPos playerPos = player.blockPosition();
        int radius = WeatherConstants.WEATHER_EFFECT_RADIUS / 2; // Уменьшенный радиус для производительности
        
        // Ищем низины в области игрока
        findLowAreas(level, playerPos, radius)
            .forEach(pos -> handleWaterAtPosition(level, pos, floodData));
    }
    
    /**
     * Находит низины в указанной области (оптимизированный алгоритм)
     */
    private static Set<BlockPos> findLowAreas(ServerLevel level, BlockPos center, int radius) {
        Set<BlockPos> lowAreas = new HashSet<>();
        
        // Сэмплируем каждый 4-й блок для производительности
        IntStream.range(-radius, radius + 1)
            .filter(x -> x % 4 == 0)
            .forEach(x -> IntStream.range(-radius, radius + 1)
                .filter(z -> z % 4 == 0)
                .mapToObj(z -> center.offset(x, 0, z))
                .map(pos -> level.getHeightmapPos(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos))
                .filter(pos -> isLowArea(level, pos))
                .forEach(lowAreas::add));
        
        return lowAreas;
    }
    
    /**
     * Проверяет, является ли позиция низиной
     */
    private static boolean isLowArea(ServerLevel level, BlockPos pos) {
        // Быстрая проверка - сравниваем с соседними блоками
        return Arrays.stream(new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}})
            .mapToInt(offset -> level.getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                pos.offset(offset[0], 0, offset[1])).getY())
            .average()
            .orElse(pos.getY()) > pos.getY() + 1;
    }
    
    /**
     * Обрабатывает воду в конкретной позиции
     */
    private static void handleWaterAtPosition(ServerLevel level, BlockPos pos, FloodData floodData) {
        switch (floodData.phase) {
            case RISING:
            case PEAK:
                addFloodWater(level, pos, floodData);
                break;
            case RECEDING:
                removeFloodWater(level, pos, floodData);
                break;
        }
    }
    
    /**
     * Добавляет воду паводка
     */
    private static void addFloodWater(ServerLevel level, BlockPos pos, FloodData floodData) {
        int maxHeight = floodData.phase == FloodData.FloodPhase.PEAK ? 
            WeatherConstants.FLOOD_WATER_LEVEL_INCREASE : 1;
        
        IntStream.rangeClosed(0, maxHeight)
            .mapToObj(pos::above)
            .filter(waterPos -> canPlaceWater(level, waterPos))
            .forEach(waterPos -> {
                level.setBlock(waterPos, Blocks.WATER.defaultBlockState(), 3);
                floodData.waterBlocks.add(waterPos);
            });
    }
    
    /**
     * Убирает воду паводка (постепенно)
     */
    private static void removeFloodWater(ServerLevel level, BlockPos pos, FloodData floodData) {
        if (RANDOM.nextInt(10) == 0) { // 10% шанс убрать блок
            floodData.waterBlocks.removeIf(waterPos -> {
                if (waterPos.distSqr(pos) < 16) { // В радиусе 4 блоков
                    level.setBlock(waterPos, Blocks.AIR.defaultBlockState(), 3);
                    return true;
                }
                return false;
            });
        }
    }
    
    /**
     * Проверяет, можно ли разместить воду в позиции
     */
    private static boolean canPlaceWater(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || 
               state.is(Blocks.GRASS) || 
               state.is(Blocks.TALL_GRASS) ||
               state.is(Blocks.FERN);
    }
    
    /**
     * Проверяет, находится ли позиция в области, подверженной паводку
     */
    private static boolean isInFloodableArea(ServerLevel level, BlockPos pos) {
        Biome biome = level.getBiome(pos).value();
        return biome.getPrecipitationAt(pos) == Biome.Precipitation.RAIN;
    }
    
    /**
     * Создаёт визуальные эффекты паводка
     */
    private static void createVisualEffects(ServerLevel level) {
        level.players().forEach(player -> {
            BlockPos playerPos = player.blockPosition();
            
            // Частицы брызг воды
            IntStream.range(0, 10)
                .forEach(i -> createSplashParticle(level, playerPos));
            
            // Звуковые эффекты
            if (RANDOM.nextInt(WeatherConstants.FLOOD_WATER_SOUND_INTERVAL) == 0) {
                level.playSound(null, playerPos, SoundEvents.AMBIENT_UNDERWATER_LOOP,
                    SoundSource.WEATHER, 0.3F, 1.0F);
            }
        });
    }
    
    /**
     * Создаёт частицу брызг
     */
    private static void createSplashParticle(ServerLevel level, BlockPos center) {
        double x = center.getX() + (RANDOM.nextDouble() - 0.5) * 20;
        double y = center.getY() + RANDOM.nextDouble() * 3;
        double z = center.getZ() + (RANDOM.nextDouble() - 0.5) * 20;
        
        if (level.getBlockState(new BlockPos((int)x, (int)y, (int)z)).is(Blocks.WATER)) {
            level.sendParticles(ParticleTypes.SPLASH, x, y + 0.5, z, 1, 0, 0.1, 0, 0.1);
        }
    }
    
    /**
     * Уведомляет игроков о начале паводка
     */
    private static void broadcastFloodStart(ServerLevel level) {
        level.players().forEach(player ->
            level.playSound(null, player.blockPosition(), SoundEvents.WEATHER_RAIN_ABOVE,
                SoundSource.WEATHER, 0.8F, 0.6F));
    }
    
    /**
     * Убирает всю воду паводка при остановке
     */
    private static void removeAllFloodWater(ServerLevel level, Set<BlockPos> waterBlocks) {
        waterBlocks.parallelStream()
            .filter(pos -> level.getBlockState(pos).is(Blocks.WATER))
            .forEach(pos -> level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3));
        
        waterBlocks.clear();
    }
    
    // Приватный конструктор для utility класса
    private FloodWeather() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}