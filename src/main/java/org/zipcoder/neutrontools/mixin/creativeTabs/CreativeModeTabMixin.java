package org.zipcoder.neutrontools.mixin.creativeTabs;

import net.minecraft.world.item.Item;
import org.zipcoder.neutrontools.NeutronTools;
import org.zipcoder.neutrontools.creativetabs.CreativeTabs;
import org.zipcoder.neutrontools.creativetabs.client.data.NewTabJsonHelper;
import org.zipcoder.neutrontools.creativetabs.client.impl.CreativeModeTabMixin_I;
import org.zipcoder.neutrontools.creativetabs.client.data.CreativeTabEdits;
import org.zipcoder.neutrontools.utils.CreativeTabUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
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

import java.util.*;

import static org.zipcoder.neutrontools.utils.CreativeTabUtils.getRegistryID;
import static org.zipcoder.neutrontools.utils.CreativeTabUtils.getTranslationKey;
import static org.zipcoder.neutrontools.NeutronTools.LOGGER;

@Mixin(CreativeModeTab.class)
public abstract class CreativeModeTabMixin implements CreativeModeTabMixin_I {

    @Shadow
    private Collection<ItemStack> displayItems;
    @Shadow
    private Set<ItemStack> displayItemsSearchTab;

    @Shadow
    public abstract void rebuildSearchTree();

    @Shadow
    @Final
    private Component displayName;

    @Shadow
    public abstract Collection<ItemStack> getDisplayItems();

    @Shadow
    @Final
    private int searchBarWidth;

    @Inject(method = "buildContents", at = @At("HEAD"), cancellable = true)
    private void injectBuildContents(CreativeModeTab.ItemDisplayParameters arg, CallbackInfo ci) {
        CreativeModeTab self = (CreativeModeTab) (Object) this;

        //Add new tabs
        if (CreativeTabEdits.INSTANCE.newTabs.contains(self)
                && CreativeTabEdits.INSTANCE.tabAdditions.containsKey(self)) {

            NeutronTools.LOGGER.debug("Adding contents of new tab: {}", self.getDisplayName().getString());
            ci.cancel(); //clear tab
            displayItems.clear();
            displayItemsSearchTab.clear();
            List<ItemStack> stacks = CreativeTabEdits.INSTANCE.tabAdditions.get(self);

            displayItems.addAll(stacks);
            displayItemsSearchTab.addAll(stacks);
            rebuildSearchTree();
        }
    }

    @Unique
    private Collection<ItemStack> editItemStacks(Collection<ItemStack> inputStacks, boolean isSearchItems) {
        CreativeModeTab self = (CreativeModeTab) ((Object) this);

        //If this is a new tab or the search tab, return the input stacks
        if (CreativeTabEdits.INSTANCE.newTabs.contains(self) || self.getType() == CreativeModeTab.Type.SEARCH)
            return inputStacks;

        //Get the original stacks
        Collection<ItemStack> originalStacks = this.displayItems;
        if (isSearchItems) originalStacks = this.displayItemsSearchTab;

        //Get the items to remove
        Set<Item> itemsToRemove = new HashSet<>();
        itemsToRemove.addAll(CreativeTabEdits.INSTANCE.getHiddenItems());

        Set<Item> tabRemovals = CreativeTabEdits.INSTANCE.tabRemovals.get(self);
        if (tabRemovals != null && !tabRemovals.isEmpty()) {
            itemsToRemove.addAll(tabRemovals);
        }

        //get the items to add
        List<ItemStack> itemsToAdd = CreativeTabEdits.INSTANCE.tabAdditions.get(self);


        Pair<NewTabJsonHelper, List<ItemStack>> replacementTab = CreativeTabUtils.getReplacementTab(self);
        if (replacementTab != null) {
            List<ItemStack> returnStacks = new ArrayList<>(replacementTab.getRight());

            if (replacementTab.getLeft().getTabItems().stream().anyMatch(i -> i.name.equalsIgnoreCase("existing"))) {
                returnStacks.addAll(originalStacks.stream().filter(
                        i ->
                                !itemsToRemove.contains(i.getItem())).toList());
            }

            return addAndReturn(returnStacks, itemsToAdd, isSearchItems);  //Add items right before returning it
        }

        Collection<ItemStack> filteredStacks = new ArrayList<>();

        if (originalStacks != null && !originalStacks.isEmpty()) {
            originalStacks.forEach(i -> {
                if (!itemsToRemove.contains(i.getItem())) {
                    filteredStacks.add(i);
                }
            });

            if (!filteredStacks.isEmpty()) {
                return addAndReturn(filteredStacks, itemsToAdd, isSearchItems); //Add items right before returning it
            }
        }

        return addAndReturn(inputStacks, itemsToAdd, isSearchItems); //Add items right before returning it
    }

    private List<ItemStack> addAndReturn(Collection<ItemStack> inputStacks, List<ItemStack> itemsToAdd, boolean isSearchItems) {
        //We need to add the items from unregistered tabs to the search tab otherwise they will not show up in the search tab
        if (isSearchItems) inputStacks.addAll(CreativeTabs.getItemsFromUnregisteredTabs());
        if (itemsToAdd != null) inputStacks.addAll(itemsToAdd);
        return CreativeTabUtils.getUniqueNbtOrderedStacks(inputStacks);
    }


    @Inject(method = "hasAnyItems", at = @At("RETURN"), cancellable = true)
    private void injectHasAnyItems(CallbackInfoReturnable<Boolean> cir) {
        CreativeModeTab self = (CreativeModeTab) ((Object) this);

        if (CreativeTabEdits.INSTANCE.newTabs.contains(self)
                && CreativeTabEdits.INSTANCE.tabAdditions.containsKey(self)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void injectDisplayName(CallbackInfoReturnable<Component> cir) {
        CreativeModeTab self = (CreativeModeTab) ((Object) this);

        if (cached_displayName == null) {
            Pair<NewTabJsonHelper, List<ItemStack>> replaceTab = CreativeTabUtils.getReplacementTab(self);
            if (replaceTab == null) {
                cached_displayName = this.displayName;
            } else {
                cached_displayName = Component.translatable(CreativeTabUtils.prefix(replaceTab.getLeft().getTabName()));
                isCachedCustomDisplayName = true;
            }
        }

        if (CreativeTabEdits.INSTANCE.getTabNameMode() == CreativeTabEdits.TabNameMode.RESOURCE_ID) {
            cir.setReturnValue(Component.literal(getRegistryID(self)));
        } else if (CreativeTabEdits.INSTANCE.getTabNameMode() == CreativeTabEdits.TabNameMode.TRANSLATION_KEY) {
            cir.setReturnValue(Component.literal(getTranslationKey(cached_displayName)));
        } else cir.setReturnValue(cached_displayName);
    }

    @Inject(method = "contains", at = @At("RETURN"), cancellable = true)
    private void injectContains(ItemStack arg, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(getDisplayItems().contains(arg));
    }

    @Inject(method = "getDisplayItems", at = @At("RETURN"), cancellable = true)
    private void injectDisplayItemsFilter(CallbackInfoReturnable<Collection<ItemStack>> cir) {
        if (cached_FilteredDisplayItems == null && !cir.getReturnValue().isEmpty()) { //Cache the display items
            LOGGER.debug("tab {}: \tCaching display items...", this.displayName.getString());
            cached_FilteredDisplayItems = editItemStacks(cir.getReturnValue(), false);
        }
        if (cached_FilteredDisplayItems != null) cir.setReturnValue(cached_FilteredDisplayItems);
    }

    @Inject(method = "getSearchTabDisplayItems", at = @At("RETURN"), cancellable = true)
    private void injectSearchItemsFilter(CallbackInfoReturnable<Collection<ItemStack>> cir) {
        if (cached_filteredSearchTab == null && !cir.getReturnValue().isEmpty()) { //Cache the search tab
            LOGGER.debug("tab {}: \tCaching search tab display items...", this.displayName.getString());
            cached_filteredSearchTab = editItemStacks(cir.getReturnValue(), true);
        }
        if (cached_filteredSearchTab != null) cir.setReturnValue(cached_filteredSearchTab);
    }

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


    //TODO: Make sure things like this arent happening anywhere else
    //This method was called EVERY time the icon is requested, so we need to cache it
    @Inject(method = "getIconItem", at = @At("RETURN"), cancellable = true)
    private void injectIcon(CallbackInfoReturnable<ItemStack> cir) {
        CreativeModeTab self = (CreativeModeTab) ((Object) this);
        if (!isCachedCustomIcon) {
            Pair<NewTabJsonHelper, List<ItemStack>> replacementTab = CreativeTabUtils.getReplacementTab(self);
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
