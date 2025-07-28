package com.anmitalidev.enhancedweather.commands;

import com.anmitalidev.enhancedweather.weather.WeatherManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class WeatherCommands {
    
    private static final SuggestionProvider<CommandSourceStack> WEATHER_SUGGESTIONS = 
        (context, builder) -> SharedSuggestionProvider.suggest(
            new String[]{"clear", "storm", "lightrain", "flood"}, builder);
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("enhancedweather")
            .requires(source -> source.hasPermission(2)) // Требует уровень оператора
            .then(Commands.argument("type", StringArgumentType.string())
                .suggests(WEATHER_SUGGESTIONS)
                .executes(WeatherCommands::setWeather))
            .executes(WeatherCommands::getWeatherInfo)
        );
        
        // Краткие команды для удобства
        dispatcher.register(Commands.literal("estorm")
            .requires(source -> source.hasPermission(2))
            .executes(context -> setSpecificWeather(context, WeatherManager.WeatherType.STORM))
        );
        
        dispatcher.register(Commands.literal("elightrain")
            .requires(source -> source.hasPermission(2))
            .executes(context -> setSpecificWeather(context, WeatherManager.WeatherType.LIGHT_RAIN))
        );
        
        dispatcher.register(Commands.literal("eflood")
            .requires(source -> source.hasPermission(2))
            .executes(context -> setSpecificWeather(context, WeatherManager.WeatherType.FLOOD))
        );
        
        dispatcher.register(Commands.literal("eclear")
            .requires(source -> source.hasPermission(2))
            .executes(context -> setSpecificWeather(context, WeatherManager.WeatherType.CLEAR))
        );
    }
    
    private static int setWeather(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String weatherType = StringArgumentType.getString(context, "type").toLowerCase();
        
        ServerLevel serverLevel = source.getLevel();
        if (serverLevel == null) {
            source.sendFailure(Component.literal("This command can only be used in a server world"));
            return 0;
        }
        
        if (serverLevel.dimension() != Level.OVERWORLD) {
            source.sendFailure(Component.literal("Enhanced weather only works in the Overworld"));
            return 0;
        }
        
        WeatherManager.WeatherType weather;
        String weatherName;
        
        switch (weatherType) {
            case "clear":
                weather = WeatherManager.WeatherType.CLEAR;
                weatherName = "Clear";
                break;
            case "storm":
                weather = WeatherManager.WeatherType.STORM;
                weatherName = "Storm";
                break;
            case "lightrain":
                weather = WeatherManager.WeatherType.LIGHT_RAIN;
                weatherName = "Light Rain";
                break;
            case "flood":
                weather = WeatherManager.WeatherType.FLOOD;
                weatherName = "Flood";
                break;
            default:
                source.sendFailure(Component.literal("Unknown weather type: " + weatherType + 
                    ". Available types: clear, storm, lightrain, flood"));
                return 0;
        }
        
        WeatherManager.setWeather(serverLevel, weather);
        
        source.sendSuccess(() -> Component.literal("Weather changed to: " + weatherName), true);
        return 1;
    }
    
    private static int setSpecificWeather(CommandContext<CommandSourceStack> context, WeatherManager.WeatherType weatherType) {
        CommandSourceStack source = context.getSource();
        
        try {
            ServerLevel serverLevel = source.getLevel();
        if (serverLevel == null) {
                source.sendFailure(Component.literal("This command can only be used in a server world"));
                return 0;
            }
            
            if (serverLevel.dimension() != Level.OVERWORLD) {
                source.sendFailure(Component.literal("Enhanced weather only works in the Overworld"));
                return 0;
            }
            
            WeatherManager.setWeather(serverLevel, weatherType);
            
            String weatherName = getWeatherDisplayName(weatherType);
            source.sendSuccess(() -> Component.literal("Weather changed to: " + weatherName), true);
            return 1;
            
        } catch (Exception e) {
            source.sendFailure(Component.literal("Error setting weather: " + e.getMessage()));
            return 0;
        }
    }
    
    private static int getWeatherInfo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        try {
            ServerLevel serverLevel = source.getLevel();
        if (serverLevel == null) {
                source.sendFailure(Component.literal("This command can only be used in a server world"));
                return 0;
            }
            
            if (serverLevel.dimension() != Level.OVERWORLD) {
                source.sendFailure(Component.literal("Enhanced weather only works in the Overworld"));
                return 0;
            }
            
            WeatherManager.WeatherType currentWeather = WeatherManager.getCurrentWeather(serverLevel);
            String weatherName = getWeatherDisplayName(currentWeather);
            
            source.sendSuccess(() -> Component.literal("Current enhanced weather: " + weatherName), false);
            
            // Дополнительная информация о ванильной погоде
            boolean isRaining = serverLevel.isRaining();
            boolean isThundering = serverLevel.isThundering();
            float rainLevel = serverLevel.getRainLevel(1.0F);
            
            source.sendSuccess(() -> Component.literal(String.format(
                "Vanilla weather - Raining: %s, Thundering: %s, Rain Level: %.2f", 
                isRaining, isThundering, rainLevel)), false);
            
            // Показываем доступные команды
            source.sendSuccess(() -> Component.literal(
                "Available commands: /enhancedweather <clear|storm|lightrain|flood>, " +
                "/estorm, /elightrain, /eflood, /eclear"), false);
            
            return 1;
            
        } catch (Exception e) {
            source.sendFailure(Component.literal("Error getting weather info: " + e.getMessage()));
            return 0;
        }
    }
    
    private static String getWeatherDisplayName(WeatherManager.WeatherType weatherType) {
        switch (weatherType) {
            case STORM:
                return "Storm";
            case LIGHT_RAIN:
                return "Light Rain";
            case FLOOD:
                return "Flood";
            case CLEAR:
            default:
                return "Clear";
        }
    }
}