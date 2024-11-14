package com.shadowjojo.soulanchor.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;
import java.util.List;

public class SoulCompass extends Item {
    public SoulCompass() {
        super(new Item.Properties()
            .stacksTo(1)
            .durability(100));  // The compass has limited uses
    }

    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        pTooltip.add(Component.literal("Points to your most recent death location")
            .withStyle(ChatFormatting.GRAY));
        
        if (pStack.isDamaged()) {
            int usesLeft = pStack.getMaxDamage() - pStack.getDamageValue();
            pTooltip.add(Component.literal(usesLeft + " uses remaining")
                .withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged; // Prevent animation when only NBT changes
    }
} 