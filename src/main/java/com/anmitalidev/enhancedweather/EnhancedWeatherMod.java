package com.anmitalidev.enhancedweather;

import com.anmitalidev.enhancedweather.commands.WeatherCommands;
import com.anmitalidev.enhancedweather.events.WeatherEventHandler;
import com.anmitalidev.enhancedweather.weather.WeatherManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(EnhancedWeatherMod.MODID)
public class EnhancedWeatherMod {
    public static final String MODID = "enhancedweather";
    private static final Logger LOGGER = LogManager.getLogger();
    
    public static WeatherManager weatherManager;

    public EnhancedWeatherMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::doClientStuff);
        
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new WeatherEventHandler());
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Enhanced Weather Mod - Common Setup");
        weatherManager = new WeatherManager();
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        LOGGER.info("Enhanced Weather Mod - Client Setup");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        WeatherCommands.register(event.getDispatcher());
        LOGGER.info("Enhanced Weather Mod - Commands registered");
    }
}