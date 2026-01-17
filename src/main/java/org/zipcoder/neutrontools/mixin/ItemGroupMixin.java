package org.zipcoder.neutrontools.mixin;

import org.zipcoder.creativetabs.client.tabs.CustomCreativeTabRegistry;
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
import org.zipcoder.neutrontools.mixin.creativeTabs.accessor.CreativeModeTabAccessor;
import org.zipcoder.neutrontools.mixin.creativeTabs.accessor.CreativeModeTabsAccessor;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.zipcoder.creativetabs.utils.CreativeTabUtils.getTabKey;

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
            //System.out.println("Skipping");
            return;
        }

        /**
         * Get Tab ID
         */
//        ResourceLocation tabName = BuiltInRegistries.CREATIVE_MODE_TAB.getKey(self);
//        assert tabName != null;
//        String tabID = tabName.toString();
        String tabID = getTabKey(((CreativeModeTabAccessor) self).getInternalDisplayName());
        NeutronTools.LOGGER.debug("Updating creative tab: " + tabID);

        /**
         * Item removal
         */
        List<String> itemsToDelete = CustomCreativeTabRegistry.INSTANCE.addon.itemsToDelete.get(tabID);
        if (itemsToDelete != null && !itemsToDelete.isEmpty()) {
            //System.out.println("Removing items from tab: " + tabID);
            itemsToDelete.forEach(removeItem -> { //For each item in this tab we want to delete

                displayItems.removeIf(stack -> {//If the tab has the same item ID, remove it
                    String tabItemID = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                    return removeItem.equals(tabItemID);
                });

                displayItemsSearchTab.removeIf(stack -> {
                    String tabItemID = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                    return removeItem.equals(tabItemID);
                });

            });
        }

        /**
         * Item addition
         */
        List<ItemStack> itemsToAdd = CustomCreativeTabRegistry.INSTANCE.addon.itemsToAdd.get(tabID);
        if (itemsToAdd != null && !itemsToAdd.isEmpty()) {
            //System.out.println("\nAdding items to tab: " + tabID + " (" + itemsToAdd.size() + ")\n" + itemsToAdd);
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