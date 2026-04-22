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
import org.zipcoder.neutrontools.NeutronTools;
import org.zipcoder.neutrontools.config.creativeTabs.CreativeTabConfig;
import org.zipcoder.neutrontools.creativetabs.CreativeTabs;
import org.zipcoder.neutrontools.config.creativeTabs.ItemAdditionList;
import org.zipcoder.neutrontools.config.creativeTabs.NewTabJsonHelper;
import org.zipcoder.neutrontools.creativetabs.client.impl.CreativeModeTabMixin_I;
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


    @Unique
    private Collection<ItemStack> editItemStacks(Collection<ItemStack> inputStacks, boolean isSearchItems) {
        CreativeModeTab self = (CreativeModeTab) ((Object) this);
        //If this is a new tab or the search tab, return the input stacks
        if (CreativeTabConfig.INSTANCE.newTabs.contains(self) || self.getType() == CreativeModeTab.Type.SEARCH)
            return inputStacks;

        //Get the original stacks
        Collection<ItemStack> originalStacks = this.displayItems;
        if (isSearchItems) originalStacks = this.displayItemsSearchTab;

        //Get the items to remove
        Set<Item> itemsToRemove = new HashSet<>();
        itemsToRemove.addAll(CreativeTabConfig.INSTANCE.hiddenItems);

        Set<Item> tabRemovals = CreativeTabConfig.INSTANCE.tabRemovals.get(self);
        if (tabRemovals != null && !tabRemovals.isEmpty()) {
            itemsToRemove.addAll(tabRemovals);
        }


        //Add items of replacement tab
        Pair<NewTabJsonHelper, ItemAdditionList> replacementTab = CreativeTabConfig.INSTANCE.getReplacementTab(self);
        if (replacementTab != null) {
            ItemAdditionList replacementTabAdditions = replacementTab.getRight();
            if (replacementTab.getLeft().isShouldKeepExisting()) {
                List<ItemStack> existing = new ArrayList<>();
                originalStacks.stream()
                        .filter(i -> !itemsToRemove.contains(i.getItem()))
                        .forEach(existing::add);
                replacementTabAdditions.addStacks(replacementTab.getLeft().getExistingIndex(), existing);
            }

            List<ItemStack> list = new ArrayList<>();
            replacementTabAdditions.apply(list);
            return addFilterAndReturn(list, isSearchItems);
        }

        Collection<ItemStack> filteredStacks = new ArrayList<>();
        if (originalStacks != null && !originalStacks.isEmpty()) {
            originalStacks.forEach(i -> {
                if (!itemsToRemove.contains(i.getItem())) {
                    filteredStacks.add(i);
                }
            });

            if (!filteredStacks.isEmpty()) {
                return addFilterAndReturn(filteredStacks, isSearchItems); //Add items right before returning it
            }
        }

        return addFilterAndReturn(inputStacks, isSearchItems); //Add items right before returning it
    }

    @Unique
    private List<ItemStack> addFilterAndReturn(Collection<ItemStack> inputStacks, boolean isSearchItems) {
        CreativeModeTab self = (CreativeModeTab) ((Object) this);
        ItemAdditionList itemsToAdd = CreativeTabConfig.INSTANCE.tabAdditions.get(self);

        //We need to add the items from unregistered tabs to the search tab otherwise they will not show up in the search tab
        if (isSearchItems) inputStacks.addAll(CreativeTabs.getItemsFromUnregisteredTabs());
        if (itemsToAdd != null) itemsToAdd.apply(inputStacks);

        //Keep only unique items and Make sure priority hidden items are removed from list
        Set<CreativeTabUtils.StackFingerprint> seen = new HashSet<>();
        List<ItemStack> uniqueFilteredResult = new ArrayList<>();
        for (ItemStack stack : inputStacks) {
            // For 1.12 - 1.20.4: use stack.getTag()
            // For 1.20.5+: use stack.getComponents()
            if ( //TODO: If the item is not added to the JEI blacklist, it might still not be hidden from search
                    seen.add(new CreativeTabUtils.StackFingerprint(stack.getItem(), stack.getComponents()))
                            && !CreativeTabConfig.INSTANCE.priorityHiddenItems.contains(stack.getItem())
            ) {
//                System.out.println("tab: "+CreativeTabUtils.getTranslationKey(self)
//                        +" Adding stack: "+stack.getItem().toString());
                uniqueFilteredResult.add(stack);
            }
        }
        return uniqueFilteredResult;
    }


    /// //////////////////////////////////////////
    /// Injections =========================== //
    /// //////////////////////////////////////////


    @Inject(method = "buildContents", at = @At("TAIL"), cancellable = true)
    private void injectBuildContents(CreativeModeTab.ItemDisplayParameters arg, CallbackInfo ci) {
        this.neutron$cachedParameters = arg;
        CreativeModeTab self = (CreativeModeTab) (Object) this;
        LOGGER.debug("Building contents for tab: {}", self.getDisplayName().getString());

        if (CreativeTabConfig.INSTANCE.isTabDisabled(self)) {
            LOGGER.debug("\tDisabling tab: {}", self.getDisplayName().getString());
            displayItems.clear();
            displayItemsSearchTab.clear();
        } else if (CreativeTabConfig.INSTANCE.newTabs.contains(self)
                && CreativeTabConfig.INSTANCE.tabAdditions.containsKey(self)) {

            NeutronTools.LOGGER.debug("\tAdding contents of new tab: {}", self.getDisplayName().getString());
            ci.cancel(); //clear tab
            displayItems.clear();
            displayItemsSearchTab.clear();
            ItemAdditionList stacks = CreativeTabConfig.INSTANCE.tabAdditions.get(self);
            stacks.apply(displayItems);
            stacks.apply(displayItemsSearchTab);
            rebuildSearchTree();
        }
    }
//    @Inject(method = "hasAnyItems", at = @At("RETURN"), cancellable = true)
//    private void injectHasAnyItems(CallbackInfoReturnable<Boolean> cir) {
//
//        CreativeModeTab self = (CreativeModeTab) ((Object) this);
//
//        if (CreativeTabConfig.INSTANCE.newTabs.contains(self)
//                && CreativeTabConfig.INSTANCE.tabAdditions.containsKey(self)) {
//            cir.setReturnValue(true);
//        }
//
//    }
//
//    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
//    private void injectDisplayName(CallbackInfoReturnable<Component> cir) {
//
//        CreativeModeTab self = (CreativeModeTab) ((Object) this);
//
//        if (cached_displayName == null) {
//            Pair<NewTabJsonHelper, ItemAdditionList> replaceTab = CreativeTabConfig.INSTANCE.getReplacementTab(self);
//            if (replaceTab == null) {
//                cached_displayName = this.displayName;
//            } else {
//                cached_displayName = Component.translatable(CreativeTabUtils.prefix(replaceTab.getLeft().getTabName()));
//                isCachedCustomDisplayName = true;
//            }
//        }
//
//        if (CreativeTabConfig.INSTANCE.getTabNameMode() == CreativeTabConfig.TabNameMode.RESOURCE_ID) {
//            cir.setReturnValue(Component.literal(getRegistryID(self)));
//        } else if (CreativeTabConfig.INSTANCE.getTabNameMode() == CreativeTabConfig.TabNameMode.TRANSLATION_KEY) {
//            cir.setReturnValue(Component.literal(getTranslationKey(cached_displayName)));
//        } else cir.setReturnValue(cached_displayName);
//
//    }
//
//    @Inject(method = "contains", at = @At("RETURN"), cancellable = true)
//    private void injectContains(ItemStack arg, CallbackInfoReturnable<Boolean> cir) {
//        cir.setReturnValue(getDisplayItems().contains(arg));
//    }
//
//
//    @Inject(method = "getDisplayItems", at = @At("RETURN"), cancellable = true)
//    private void injectDisplayItemsFilter(CallbackInfoReturnable<Collection<ItemStack>> cir) {
//        //First time caching of our original creative tabs
//        if (cached_FilteredDisplayItems == null) {
//            CreativeModeTab self = (CreativeModeTab) ((Object) this);
//            if (CreativeTabConfig.INSTANCE.original_tabDisplayItems.get(self) == null) {
//                //We reload for the first time here so we can index the original state of the tabs
//                CreativeTabConfig.INSTANCE.original_tabDisplayItems.put(self, new ArrayList<>());
//                CreativeTabConfig.INSTANCE.original_tabDisplayItems.get(self).addAll(cir.getReturnValue());
//                ClientModEvents.onCreativeTabReady(self);
//            }
//        }
//
//
//        if (cached_FilteredDisplayItems == null && !cir.getReturnValue().isEmpty()) { //Cache the display items
//            LOGGER.debug("tab {}: \tCaching display items...", this.displayName.getString());
//            cached_FilteredDisplayItems = editItemStacks(cir.getReturnValue(), false);
//        }
//        if (cached_FilteredDisplayItems != null) cir.setReturnValue(cached_FilteredDisplayItems);
//
//    }
//
//    @Inject(method = "getSearchTabDisplayItems", at = @At("RETURN"), cancellable = true)
//    private void injectSearchItemsFilter(CallbackInfoReturnable<Collection<ItemStack>> cir) {
//
//        if (cached_filteredSearchTab == null && !cir.getReturnValue().isEmpty()) { //Cache the search tab
//            LOGGER.debug("tab {}: \tCaching search tab display items...", this.displayName.getString());
//            cached_filteredSearchTab = editItemStacks(cir.getReturnValue(), true);
//        }
//        if (cached_filteredSearchTab != null) cir.setReturnValue(cached_filteredSearchTab);
//
//    }
//
//
//    //TODO: Make sure things like this arent happening anywhere else
//    //This method was called EVERY time the icon is requested, so we need to cache it
//    @Inject(method = "getIconItem", at = @At("RETURN"), cancellable = true)
//    private void injectIcon(CallbackInfoReturnable<ItemStack> cir) {
//
//            CreativeModeTab self = (CreativeModeTab) ((Object) this);
//            if (!isCachedCustomIcon) {
//                Pair<NewTabJsonHelper, ItemAdditionList> replacementTab = CreativeTabConfig.INSTANCE.getReplacementTab(self);
//                if (replacementTab != null) {
//                    LOGGER.debug("tab {}: \tCaching tab icon...", this.displayName.getString());
//                    cached_TabIcon = CreativeTabUtils.makeTabIcon(replacementTab.getLeft()).get();
//                }
//                isCachedCustomIcon = true;
//            }
//
//            if (cached_TabIcon != null && !cached_TabIcon.isEmpty())
//                cir.setReturnValue(cached_TabIcon);
//
//    }


}
