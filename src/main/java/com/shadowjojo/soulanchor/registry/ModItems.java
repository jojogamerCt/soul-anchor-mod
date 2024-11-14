package com.shadowjojo.soulanchor.registry;

import com.shadowjojo.soulanchor.SoulAnchor;
import com.shadowjojo.soulanchor.item.SoulCompass;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = 
        DeferredRegister.create(ForgeRegistries.ITEMS, SoulAnchor.MODID);

    public static final RegistryObject<Item> SOUL_COMPASS = ITEMS.register("soul_compass",
        SoulCompass::new);
} 