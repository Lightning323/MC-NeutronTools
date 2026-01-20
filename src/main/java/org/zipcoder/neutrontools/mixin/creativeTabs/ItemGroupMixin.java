package org.zipcoder.neutrontools.mixin.creativeTabs;

import org.zipcoder.neutrontools.creativetabs.CreativeTabCustomizationData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zipcoder.neutrontools.NeutronTools;
import org.zipcoder.neutrontools.mixin.creativeTabs.accessor.CreativeModeTabsAccessor;
import org.zipcoder.neutrontools.utils.CreativeTabUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.zipcoder.neutrontools.utils.CreativeTabUtils.getTranslationKey;

@Mixin(value = CreativeModeTab.class, priority = 10000)
public abstract class ItemGroupMixin {
    @Shadow
    private Collection<ItemStack> displayItems = ItemStackLinkedSet.createTypeAndTagSet();
    @Shadow
    private Set<ItemStack> displayItemsSearchTab = ItemStackLinkedSet.createTypeAndTagSet();

    @Shadow
    public abstract void rebuildSearchTree();

    @Shadow
    @Final
    private Component displayName;

    @Inject(method = "buildContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab;rebuildSearchTree()V"))
    private void addon_buildContents(CreativeModeTab.ItemDisplayParameters displayContext, CallbackInfo ci) {
        CreativeModeTab self = (CreativeModeTab) (Object) this;

        // Don't change anything in Survival Inventory, Search or Hotbar
        if (
                self == BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabsAccessor.getSearchTab()) ||
                        self == BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabsAccessor.getHotbarTab()) ||
                        self == BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabsAccessor.getInventoryTab())) {
            return;
        }

        /**
         * Get Tab ID
         */
        NeutronTools.LOGGER.debug("Updating creative tab: {}", CreativeTabUtils.getTranslationKey(self));

        /**
         * Item removal
         */
        Set<String> itemsToDelete = CreativeTabCustomizationData.INSTANCE.tabDeletions.get(self);
        if (itemsToDelete != null && !itemsToDelete.isEmpty()) {
            itemsToDelete.forEach(removalID -> { //For each item in this tab we want to delete
                displayItems.removeIf(stack -> {//If the tab has the same item ID, remove it
                    String stackId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(); //O(1) time complexity
                    return removalID.equals(stackId);
                });
                displayItemsSearchTab.removeIf(stack -> {
                    String stackId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                    return removalID.equals(stackId);
                });
            });
        }

        /**
         * Item addition
         */
        List<ItemStack> itemsToAdd = CreativeTabCustomizationData.INSTANCE.tabAdditions.get(self);
        if (itemsToAdd != null && !itemsToAdd.isEmpty()) {
            displayItems.addAll(itemsToAdd);
            displayItemsSearchTab.addAll(itemsToAdd);
        }

        //If anything has changed, rebuild search tree
        if (
                (itemsToAdd != null && !itemsToAdd.isEmpty()) ||
                        (itemsToDelete != null && !itemsToDelete.isEmpty())
        ) rebuildSearchTree();
    }
}