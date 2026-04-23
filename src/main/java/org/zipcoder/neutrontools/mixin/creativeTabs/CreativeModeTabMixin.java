package org.zipcoder.neutrontools.mixin.creativeTabs;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.zipcoder.neutrontools.config.creativeTabs.CreativeTabConfig;
import org.zipcoder.neutrontools.config.creativeTabs.ItemAdditionList;
import org.zipcoder.neutrontools.config.creativeTabs.TabConfig;
import org.zipcoder.neutrontools.creativetabs.NeutronCreativeTabs;
import org.zipcoder.neutrontools.creativetabs.client.impl.CreativeModeTabMixin_I;
import org.zipcoder.neutrontools.events.ClientModEvents;
import org.zipcoder.neutrontools.utils.CreativeTabUtils;

import java.util.*;

import static org.zipcoder.neutrontools.NeutronTools.LOGGER;
import static org.zipcoder.neutrontools.utils.CreativeTabUtils.getTranslationKey;

@Mixin(CreativeModeTab.class)
public abstract class CreativeModeTabMixin implements CreativeModeTabMixin_I {

    @Shadow
    private Collection<ItemStack> displayItems;
    @Shadow
    private Set<ItemStack> displayItemsSearchTab;

    @Shadow
    @Final
    private Component displayName;

    @Shadow
    public abstract Collection<ItemStack> getDisplayItems();

    @Shadow
    @Final
    private int searchBarWidth;

    //Cached values
    @Unique
    private ItemStack cached_TabIcon = null;
    @Unique
    private boolean isCachedCustomIcon = false;
    @Unique
    private boolean isCachedCustomDisplayName = false;
    @Unique
    private Component cached_displayName = null;

    /**
     * Resets the cache for this tab
     */
    @Override
    public void resetCache() {
        isCachedCustomIcon = false;
        isCachedCustomDisplayName = false;
        cached_TabIcon = null;
        cached_displayName = null;
    }


    /**
     *
     * @param displayItems
     * @param isSearchItems
     * @return a unique list of filtered items (so we can add them to search tab)
     */
    @Unique
    private void modifyDisplayItems(Collection<ItemStack> displayItems, boolean isSearchItems) {
        CreativeModeTab self = (CreativeModeTab) ((Object) this);
        TabConfig tabEditConfig = CreativeTabConfig.INSTANCE.tabEdits.get(self);

        Set<Item> itemsToRemove = CreativeTabConfig.INSTANCE.disabledItems;

        if (tabEditConfig != null) {
            ItemAdditionList itemsToAdd = tabEditConfig.itemsAdd;
            if (itemsToAdd != null) itemsToAdd.addItemsInto(displayItems);

            itemsToRemove.addAll(tabEditConfig.itemsRemove);
        }
        //Add the items from unregistered tabs to the search tab otherwise they will not show up in the search tab
        if (isSearchItems) displayItems.addAll(NeutronCreativeTabs.getItemsFromUnregisteredTabs());

        //Keep only unique items and Make sure disabled items are removed from list
        Set<CreativeTabUtils.StackFingerprint> seen = new HashSet<>();
        List<ItemStack> uniqueFilteredResult = new ArrayList<>();
        for (ItemStack stack : displayItems) {
            // For 1.12 - 1.20.4: use stack.getTag()
            // For 1.20.5+: use stack.getComponents()
            if ( //TODO: If the item is not added to the JEI blacklist, it might still not be hidden from search
                    seen.add(
                            new CreativeTabUtils.StackFingerprint(stack.getItem(), stack.getComponents()))
                            && !itemsToRemove.contains(stack.getItem())
            ) {
                uniqueFilteredResult.add(stack);
            }
        }
        displayItems.clear();
        displayItems.addAll(uniqueFilteredResult);
    }


    /// //////////////////////////////////////////
    /// Injections =========================== //
    /// //////////////////////////////////////////


    @Inject(method = "buildContents", at = @At("TAIL"), cancellable = true)
    private void injectBuildContents(CreativeModeTab.ItemDisplayParameters arg, CallbackInfo ci) {
        CreativeModeTab self = (CreativeModeTab) (Object) this;

        //Add original tab items to the config first
        NeutronCreativeTabs.cached_originalCreativeTabItems.put(CreativeTabUtils.getRegistryID(self), new ArrayList<>(displayItems));
        NeutronCreativeTabs.cached_originalCreativeTabs.add(self);

        // Get the total number of registered tabs
        int totalTabs = BuiltInRegistries.CREATIVE_MODE_TAB.size();

        // Check if this was the final tab
        if (NeutronCreativeTabs.cached_originalCreativeTabs.size() >= totalTabs) {
            onAllTabsFinishedBuilding();
        }
    }

    public void modifyItems() {
        CreativeModeTab tab = (CreativeModeTab) (Object) this;
        if (CreativeTabConfig.INSTANCE.isTabDisabled(tab)) {
            displayItems.clear();
            displayItemsSearchTab.clear();
        } else {
            modifyDisplayItems(displayItems, false);
            modifyDisplayItems(displayItemsSearchTab, true);
        }
    }

    private void onAllTabsFinishedBuilding() {
        LOGGER.info("Modifying items for all tabs; Tags ready: {}", ClientModEvents.isTagsReady());
        for (CreativeModeTab tab : NeutronCreativeTabs.cached_originalCreativeTabs) {
            ((CreativeModeTabMixin_I) tab).modifyItems();
        }
    }

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void injectDisplayName(CallbackInfoReturnable<Component> cir) {

        CreativeModeTab self = (CreativeModeTab) ((Object) this);

        if (cached_displayName == null) {
            TabConfig tabEditConfig = new TabConfig();
            if (tabEditConfig == null || tabEditConfig.tabNameKey == null) {
                cached_displayName = this.displayName;
            } else{
                cached_displayName = Component.translatable(CreativeTabUtils.prefix(tabEditConfig.tabNameKey));
                isCachedCustomDisplayName = true;
            }
        }

        if (CreativeTabConfig.INSTANCE.getTabNameMode() == CreativeTabConfig.TabNameMode.RESOURCE_ID) {
            cir.setReturnValue(Component.literal(CreativeTabUtils.getRegistryID(self)));
        } else if (CreativeTabConfig.INSTANCE.getTabNameMode() == CreativeTabConfig.TabNameMode.TRANSLATION_KEY) {
            cir.setReturnValue(Component.literal(getTranslationKey(cached_displayName)));
        } else cir.setReturnValue(cached_displayName);

    }

    //TODO: Make sure things like this arent happening anywhere else
    //This method was called EVERY time the icon is requested, so we need to cache it
    @Inject(method = "getIconItem", at = @At("RETURN"), cancellable = true)
    private void injectIcon(CallbackInfoReturnable<ItemStack> cir) {

        CreativeModeTab self = (CreativeModeTab) ((Object) this);
        if (!isCachedCustomIcon) {
            TabConfig tabEditConfig = CreativeTabConfig.INSTANCE.tabEdits.get(self);
            if (tabEditConfig != null && tabEditConfig.tabIcon != null) {
                LOGGER.debug("tab {}: \tCaching tab icon...", this.displayName.getString());
                cached_TabIcon = CreativeTabUtils.makeTabIcon(tabEditConfig.tabIcon).get();
            }
            isCachedCustomIcon = true;
        }

        if (cached_TabIcon != null && !cached_TabIcon.isEmpty())
            cir.setReturnValue(cached_TabIcon);

    }


}
