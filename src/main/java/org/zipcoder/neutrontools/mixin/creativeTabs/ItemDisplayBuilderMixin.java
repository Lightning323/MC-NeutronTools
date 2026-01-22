package org.zipcoder.neutrontools.mixin.creativeTabs;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zipcoder.neutrontools.NeutronTools;

import java.util.Collection;
import java.util.Set;

// 👀 We're mixing into the static inner class CreativeModeTab.ItemDisplayBuilder
@Mixin(targets = "net.minecraft.world.item.CreativeModeTab$ItemDisplayBuilder")
public abstract class ItemDisplayBuilderMixin {

    // --- Shadow fields if you need them ---
    @Shadow
    public Collection<ItemStack> tabContents;
    @Shadow
    public Set<ItemStack> searchTabContents;
    @Shadow
    private CreativeModeTab tab;
    @Shadow
    private FeatureFlagSet featureFlagSet;

    @Inject(method = "accept(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/CreativeModeTab$TabVisibility;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onAcceptHead(ItemStack stack, CreativeModeTab.TabVisibility visibility, CallbackInfo ci) {
        //TODO: It would be good to investigate what is causing this instead of cover it up
        if (stack.getCount() != 1) {//Some mod adds air to the creative tab in functional blocks, why? I dont know
            NeutronTools.LOGGER.warn("[Mixin] CANCELED Adding item to creative tab for not having stack size of 1: {}; count: {} to Tab: {}",
                    stack.getDisplayName().getString(), stack.getCount(), tab.getDisplayName());
            ci.cancel();
        }
    }
}
