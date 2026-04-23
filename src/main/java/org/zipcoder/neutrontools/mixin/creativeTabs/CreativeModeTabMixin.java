package org.zipcoder.neutrontools.mixin.creativeTabs;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.zipcoder.neutrontools.config.creativeTabs.CreativeTabConfig;
import org.zipcoder.neutrontools.config.creativeTabs.NewTabJsonHelper;
import org.zipcoder.neutrontools.creativetabs.NeutronCreativeTabs;
import org.zipcoder.neutrontools.config.creativeTabs.ItemAdditionList;
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
    @Unique
    private CreativeModeTab.ItemDisplayParameters neutron$cachedParameters;


    public void rebuildSearchTree() {
        if (this.neutron$cachedParameters == null) return;
        // 1. Re-run the internal logic to populate displayItems and displayItemsSearchTab
        ((CreativeModeTab) (Object) this).buildContents(this.neutron$cachedParameters);
    }

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
    @Unique
    private Collection<ItemStack> cached_FilteredDisplayItems = null;
    @Unique
    private Collection<ItemStack> cached_filteredSearchTab = null;


    /**
     * Resets the cache for this tab
     */
    @Override
    public void resetCache() {
        isCachedCustomIcon = false;
        isCachedCustomDisplayName = false;
        cached_TabIcon = null;
        cached_FilteredDisplayItems = null;
        cached_filteredSearchTab = null;
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

        //First get a list of all items to add and remove
        ItemAdditionList itemsToAdd = CreativeTabConfig.INSTANCE.tabAdditions.get(self);

        Set<Item> itemsToRemove = CreativeTabConfig.INSTANCE.disabledItems;
        Set<Item> tabRemovals = CreativeTabConfig.INSTANCE.tabRemovals.get(self);
        if (tabRemovals != null && !tabRemovals.isEmpty()) {
            itemsToRemove.addAll(tabRemovals);
        }

        //Add the items from unregistered tabs to the search tab otherwise they will not show up in the search tab
        if (isSearchItems) displayItems.addAll(NeutronCreativeTabs.getItemsFromUnregisteredTabs());

        //Add items from tab addition list
        if (itemsToAdd != null) itemsToAdd.addItemsInto(displayItems);

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
        this.neutron$cachedParameters = arg;
        CreativeModeTab self = (CreativeModeTab) (Object) this;
        LOGGER.debug("Building contents for tab: {}; Tags ready: {}",
                self.getDisplayName().getString(),
                ClientModEvents.isTagsReady());


        //Add original tab items to the config first
        NeutronCreativeTabs.cached_originalCreativeTabs.put(CreativeTabUtils.getRegistryID(self), new ArrayList<>(displayItems));

        if (CreativeTabConfig.INSTANCE.isTabDisabled(self)) {
            displayItems.clear();
            displayItemsSearchTab.clear();
            return;
        } else {
            modifyDisplayItems(displayItems, false);
            modifyDisplayItems(displayItemsSearchTab, true);
        }

        if (!displayItems.isEmpty()) {
            NeutronCreativeTabs.cached_creativeTabs.put(CreativeTabUtils.getRegistryID(self), displayItems);
        }
    }

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void injectDisplayName(CallbackInfoReturnable<Component> cir) {

        CreativeModeTab self = (CreativeModeTab) ((Object) this);

        if (cached_displayName == null) {
            Pair<NewTabJsonHelper, ItemAdditionList> replaceTab = CreativeTabConfig.INSTANCE.getReplacementTab(self);
            if (replaceTab == null) {
                cached_displayName = this.displayName;
            } else {
                cached_displayName = Component.translatable(CreativeTabUtils.prefix(replaceTab.getLeft().getTabName()));
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
                Pair<NewTabJsonHelper, ItemAdditionList> replacementTab = CreativeTabConfig.INSTANCE.getReplacementTab(self);
                if (replacementTab != null) {
                    LOGGER.debug("tab {}: \tCaching tab icon...", this.displayName.getString());
                    cached_TabIcon = CreativeTabUtils.makeTabIcon(replacementTab.getLeft()).get();
                }
                isCachedCustomIcon = true;
            }

            if (cached_TabIcon != null && !cached_TabIcon.isEmpty())
                cir.setReturnValue(cached_TabIcon);

    }


}
