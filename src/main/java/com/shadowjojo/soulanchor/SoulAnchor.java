package com.shadowjojo.soulanchor;

import com.shadowjojo.soulanchor.tracking.DeathTracker;
import com.shadowjojo.soulanchor.client.CompassHandler;
import com.shadowjojo.soulanchor.registry.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(SoulAnchor.MODID)
public class SoulAnchor {
    public static final String MODID = "soulanchor";
    private static final Logger LOGGER = LogUtils.getLogger();

    public SoulAnchor() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // Register items
        ModItems.ITEMS.register(modEventBus);
        
        // Register creative tab contents
        modEventBus.addListener(this::addCreative);
        
        // Register our death tracker
        MinecraftForge.EVENT_BUS.register(new DeathTracker());
        
        // Register compass handler on client side
        MinecraftForge.EVENT_BUS.register(new CompassHandler());
        
        // Register ourselves for server and other game events
        MinecraftForge.EVENT_BUS.register(this);
        
        LOGGER.info("Soul Anchor initialized - tracking the fallen...");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.SOUL_COMPASS);
        }
    }
} 