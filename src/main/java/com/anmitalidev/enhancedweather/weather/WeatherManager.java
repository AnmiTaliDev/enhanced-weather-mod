package com.anmitalidev.enhancedweather.weather;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public class WeatherManager {
    private static final Logger LOGGER = LogManager.getLogger();
    
    // Thread-safe Maps вместо HashMap
    private static final ConcurrentHashMap<ServerLevel, WeatherType> activeWeather = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<ServerLevel, Integer> weatherDuration = new ConcurrentHashMap<>();
    private static final Random random = new Random();
    
    public enum WeatherType {
        CLEAR,
        STORM,
        LIGHT_RAIN,
        FLOOD
    }
    
    /**
     * Устанавливает погоду для указанного мира
     * @param level Мир (не может быть null)
     * @param weatherType Тип погоды (не может быть null)
     */
    public static void setWeather(ServerLevel level, WeatherType weatherType) {
        // Null проверки
        Objects.requireNonNull(level, "ServerLevel cannot be null");
        Objects.requireNonNull(weatherType, "WeatherType cannot be null");
        
        LOGGER.debug("Setting weather to {} for dimension {}", weatherType, level.dimension().location());
        
        // Очищаем предыдущую погоду
        clearWeather(level);
        
        activeWeather.put(level, weatherType);
        
        switch (weatherType) {
            case STORM:
                StormWeather.start(level);
                weatherDuration.put(level, WeatherConstants.STORM_DURATION_TICKS);
                break;
            case LIGHT_RAIN:
                LightRainWeather.start(level);
                weatherDuration.put(level, WeatherConstants.LIGHT_RAIN_DURATION_TICKS);
                break;
            case FLOOD:
                FloodWeather.start(level);
                weatherDuration.put(level, WeatherConstants.FLOOD_DURATION_TICKS);
                break;
            case CLEAR:
                // clearWeather уже вызван выше
                break;
        }
    }
    
    /**
     * Получает текущую погоду для мира
     * @param level Мир (не может быть null)
     * @return Тип погоды (никогда не null)
     */
    public static WeatherType getCurrentWeather(ServerLevel level) {
        Objects.requireNonNull(level, "ServerLevel cannot be null");
        return activeWeather.getOrDefault(level, WeatherType.CLEAR);
    }
    
    /**
     * Очищает погоду в указанном мире
     * @param level Мир (не может быть null)
     */
    private static void clearWeather(ServerLevel level) {
        Objects.requireNonNull(level, "ServerLevel cannot be null");
        
        LOGGER.debug("Clearing weather for dimension {}", level.dimension().location());
        
        // Останавливаем все типы погоды
        StormWeather.stop(level);
        LightRainWeather.stop(level);
        FloodWeather.stop(level);
        
        // Устанавливаем ванильную ясную погоду
        level.setWeatherParameters(6000, 0, false, false);
        
        // Убираем из maps
        activeWeather.remove(level);
        weatherDuration.remove(level);
    }
    
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        // Null проверка сервера
        if (event.getServer() == null) return;
        
        for (ServerLevel level : event.getServer().getAllLevels()) {
            // Null проверка level и проверка на Overworld
            if (level == null || level.dimension() != Level.OVERWORLD) continue;
            
            // Обновление длительности погоды
            weatherDuration.computeIfPresent(level, (l, duration) -> {
                int newDuration = duration - 1;
                if (newDuration <= 0) {
                    clearWeather(l);
                    return null; // Удаляем из map
                } else {
                    updateWeatherEffects(l);
                    return newDuration;
                }
            });
            
            // Случайное появление погоды (используем константу)
            if (random.nextInt(WeatherConstants.RANDOM_WEATHER_INTERVAL_TICKS) == 0) {
                WeatherType newWeather = WeatherType.values()[random.nextInt(WeatherType.values().length)];
                if (newWeather != WeatherType.CLEAR && getCurrentWeather(level) == WeatherType.CLEAR) {
                    setWeather(level, newWeather);
                }
            }
        }
    }
    
    /**
     * Обновляет эффекты текущей погоды
     * @param level Мир (не может быть null)
     */
    private static void updateWeatherEffects(ServerLevel level) {
        Objects.requireNonNull(level, "ServerLevel cannot be null");
        
        WeatherType currentWeather = getCurrentWeather(level);
        
        try {
            switch (currentWeather) {
                case STORM:
                    StormWeather.update(level);
                    break;
                case LIGHT_RAIN:
                    LightRainWeather.update(level);
                    break;
                case FLOOD:
                    FloodWeather.update(level);
                    break;
                case CLEAR:
                    // Ничего не делаем
                    break;
            }
        } catch (Exception e) {
            LOGGER.error("Error updating weather effects for {}: {}", currentWeather, e.getMessage());
        }
    }
    
    /**
     * Memory leak cleanup - очищаем все данные при остановке сервера
     */
    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        LOGGER.info("Enhanced Weather Mod - Cleaning up weather data on server stop");
        
        // Останавливаем все активную погоду
        for (ServerLevel level : activeWeather.keySet()) {
            try {
                clearWeather(level);
            } catch (Exception e) {
                LOGGER.error("Error clearing weather for level during shutdown: {}", e.getMessage());
            }
        }
        
        // Принудительно очищаем maps
        activeWeather.clear();
        weatherDuration.clear();
        
        // Очищаем данные в классах погоды
        StormWeather.cleanup();
        LightRainWeather.cleanup();
        FloodWeather.cleanup();
        
        LOGGER.info("Enhanced Weather Mod - Cleanup completed");
    }
    
    /**
     * Получает оставшееся время погоды в тиках
     * @param level Мир (не может быть null)
     * @return Оставшееся время в тиках, или 0 если погоды нет
     */
    public static int getRemainingDuration(ServerLevel level) {
        Objects.requireNonNull(level, "ServerLevel cannot be null");
        return weatherDuration.getOrDefault(level, 0);
    }
    
    /**
     * Проверяет, активна ли какая-либо enhanced погода
     * @param level Мир (не может быть null)
     * @return true если активна enhanced погода
     */
    public static boolean hasActiveWeather(ServerLevel level) {
        Objects.requireNonNull(level, "ServerLevel cannot be null");
        return getCurrentWeather(level) != WeatherType.CLEAR;
    }
}