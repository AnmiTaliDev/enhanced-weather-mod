package com.anmitalidev.enhancedweather.weather;

/**
 * Константы для системы погоды Enhanced Weather Mod
 */
public class WeatherConstants {
    
    // Длительности погоды (в тиках, 20 тиков = 1 секунда)
    public static final int STORM_DURATION_TICKS = 6000;        // 5 минут
    public static final int LIGHT_RAIN_DURATION_TICKS = 12000;  // 10 минут
    public static final int FLOOD_DURATION_TICKS = 18000;       // 15 минут
    
    // Частота случайной погоды
    public static final int RANDOM_WEATHER_INTERVAL_TICKS = 24000; // 20 минут
    
    // Шансы событий (1 из N)
    public static final int STORM_LIGHTNING_CHANCE = 40;        // Каждые 2 секунды
    public static final int LIGHT_RAIN_CAULDRON_FILL_CHANCE = 100;  // 1% в секунду
    public static final int LIGHT_RAIN_CROP_GROWTH_CHANCE = 50;     // 2% в секунду
    
    // Параметры паводка
    public static final int FLOOD_WATER_LEVEL_INCREASE = 2;     // Блоков воды
    public static final int FLOOD_RECEDE_BLOCKS_PER_TICK = 5;   // Блоков убирать за тик
    public static final int FLOOD_BUBBLES_PER_SECOND = 15;      // Пузырей в секунду
    
    // Визуальные эффекты
    public static final int STORM_WIND_PARTICLES_COUNT = 15;    // Частиц ветра
    public static final int LIGHT_RAIN_PARTICLES_COUNT = 8;     // Частиц дождя
    public static final float STORM_DARKNESS_LEVEL = 0.4F;      // Уровень затемнения
    
    // Звуковые эффекты (интервалы в тиках)
    public static final int STORM_WIND_SOUND_INTERVAL = 120;    // 6 секунд
    public static final int STORM_THUNDER_SOUND_INTERVAL = 100; // 5 секунд
    public static final int LIGHT_RAIN_SOUND_INTERVAL = 200;    // 10 секунд
    public static final int FLOOD_WATER_SOUND_INTERVAL = 150;   // 7.5 секунд
    public static final int FLOOD_SPLASH_SOUND_INTERVAL = 80;   // 4 секунды
    
    // Радиусы поиска и воздействия
    public static final int WEATHER_EFFECT_RADIUS = 32;        // Блоков от игрока
    public static final int FLOOD_LOWLAND_CHECK_RADIUS = 5;     // Блоков для поиска низин
    public static final int PARTICLE_RENDER_DISTANCE = 32;     // Дистанция рендера частиц
    
    // Интервалы обновления
    public static final int WEATHER_UPDATE_INTERVAL = 20;      // Каждую секунду
    public static final int CLEANUP_INTERVAL = 200;            // Каждые 10 секунд
    
    // Частные конструктор - класс только для констант
    private WeatherConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}