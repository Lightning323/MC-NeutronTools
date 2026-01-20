package org.zipcoder.neutrontools.mixin.creativeTabs;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import org.zipcoder.neutrontools.NeutronTools;
import org.zipcoder.neutrontools.creativetabs.client.data.NewTabJsonHelper;
import org.zipcoder.neutrontools.creativetabs.client.impl.CreativeModeTabMixin_I;
import org.zipcoder.neutrontools.creativetabs.client.data.CreativeTabCustomizationData;
import org.zipcoder.neutrontools.events.TagEventHandler;
import org.zipcoder.neutrontools.mixin.creativeTabs.accessor.CreativeModeTabsAccessor;
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

    @Inject(method = "buildContents", at = @At("HEAD"), cancellable = true)
    private void injectBuildContents(CreativeModeTab.ItemDisplayParameters arg, CallbackInfo ci) {
        CreativeModeTab self = (CreativeModeTab) (Object) this;

        //Add new tabs
        if (TagEventHandler.tagsUpdated &&
                CreativeTabCustomizationData.INSTANCE.getNewTabs().contains(self)
                && CreativeTabCustomizationData.INSTANCE.tabAdditions.containsKey(self)) {

            NeutronTools.LOGGER.info("Adding contents of new tab: {}", self.getDisplayName().getString());
            ci.cancel(); //clear tab
            displayItems.clear();
            displayItemsSearchTab.clear();
            List<ItemStack> stacks = CreativeTabCustomizationData.INSTANCE.tabAdditions.get(self);

            displayItems.addAll(stacks);
            displayItemsSearchTab.addAll(stacks);
            rebuildSearchTree();
        }
    }

    @Inject(method = "buildContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab;rebuildSearchTree()V"))
    private void addon_buildContents(CreativeModeTab.ItemDisplayParameters displayContext, CallbackInfo ci) {
        CreativeModeTab self = (CreativeModeTab) (Object) this;
        if (
                !TagEventHandler.tagsUpdated ||
                        self == BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabsAccessor.getSearchTab()) ||
                        self == BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabsAccessor.getHotbarTab()) ||
                        self == BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabsAccessor.getInventoryTab())) {
            return;
        }
        boolean rebuildTree = false;
        NeutronTools.LOGGER.info("Building contents of tab: {}", self.getDisplayName().getString());


        /**
         * Item removal
         * Always do this first!
         */
        Set<String> itemsToDelete = CreativeTabCustomizationData.INSTANCE.tabRemovals.get(self);
        if (itemsToDelete != null && !itemsToDelete.isEmpty()) {
            rebuildTree = true;
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
            rebuildTree = true;
            displayItems.addAll(itemsToAdd);
            displayItemsSearchTab.addAll(itemsToAdd);
        }

        //If anything has changed, rebuild search tree
        if (rebuildTree) {
            rebuildSearchTree();
        }
    }

    public void reload() {
        rebuildSearchTree();
    }


    @Unique
    private Collection<ItemStack> filterItems(Collection<ItemStack> inputStacks) {
        CreativeModeTab self = (CreativeModeTab) ((Object) this);

        //If this is a new tab or the search tab, return the input stacks
        if (CreativeTabCustomizationData.INSTANCE.getNewTabs().contains(self) || self.getType() == CreativeModeTab.Type.SEARCH)
            return inputStacks;

        //Get the original stacks
        Collection<ItemStack> originalStacks = this.displayItems;

        //Get the items to remove
        Set<Item> itemsToRemove = new HashSet<>();
        itemsToRemove.addAll(CreativeTabCustomizationData.INSTANCE.getHiddenItems());
//        Set<String> itemsToDelete = CreativeTabCustomizationData.INSTANCE.tabRemovals.get(self);
//        if (itemsToDelete != null && !itemsToDelete.isEmpty()) {
//            itemsToRemove.addAll(itemsToDelete.stream().map(BuiltInRegistries.ITEM::get).toList());
//        }


        Optional<Pair<NewTabJsonHelper, List<ItemStack>>> replacementTab =
                CreativeTabUtils.replacementTab(convertName(getTranslationKey(this.displayName)));
        if (replacementTab.isPresent()) {
            List<ItemStack> returnStacks = new ArrayList<>(replacementTab.get().getRight());

            if (replacementTab.get().getLeft().getTabItems().stream().anyMatch(i -> i.getName().equalsIgnoreCase("existing"))) {
                returnStacks.addAll(originalStacks.stream().filter(
                        i ->
                                !itemsToRemove.contains(i.getItem())).toList());
            }

            return returnStacks;
        }

        Collection<ItemStack> filteredStacks = new ArrayList<>();

        if (originalStacks != null && !originalStacks.isEmpty()) {
            originalStacks.forEach(i -> {
                if (!itemsToRemove.contains(i.getItem())) {
                    filteredStacks.add(i);
                }
            });

            if (!filteredStacks.isEmpty()) {
                return filteredStacks;
            }
        }

        return inputStacks;
    }


    @Inject(method = "hasAnyItems", at = @At("RETURN"), cancellable = true)
    private void injectHasAnyItems(CallbackInfoReturnable<Boolean> cir) {
        CreativeModeTab self = (CreativeModeTab) ((Object) this);

        if (CreativeTabCustomizationData.INSTANCE.getNewTabs().contains(self) && CreativeTabCustomizationData.INSTANCE.tabAdditions.containsKey(self)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void injectDisplayName(CallbackInfoReturnable<Component> cir) {
        Component value = this.displayName;
        CreativeTabUtils.replacementTab(convertName(getTranslationKey(value))).ifPresent(tabData -> {
            if (!CreativeTabCustomizationData.INSTANCE.isShowTabNames()) {
                cir.setReturnValue(Component.translatable(CreativeTabUtils.prefix(tabData.getLeft().getTabName())));
            }
        });

        if (!CreativeTabCustomizationData.INSTANCE.isShowTabNames())
            return;

        cir.setReturnValue(Component.literal(getTranslationKey(value)));
    }

    @Inject(method = "contains", at = @At("RETURN"), cancellable = true)
    private void injectContains(ItemStack arg, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(getDisplayItems().contains(arg));
    }

    @Inject(method = "getDisplayItems", at = @At("RETURN"), cancellable = true)
    private void injectDisplayItemsFilter(CallbackInfoReturnable<Collection<ItemStack>> cir) {
        if (cached_FilteredDisplayItems == null && !cir.getReturnValue().isEmpty()) { //Cache the display items
            LOGGER.info("tab {}: \tCaching display items...", this.displayName.getString());
            cached_FilteredDisplayItems = filterItems(cir.getReturnValue());
        }
        if (cached_FilteredDisplayItems != null) cir.setReturnValue(cached_FilteredDisplayItems);
    }

    @Inject(method = "getSearchTabDisplayItems", at = @At("RETURN"), cancellable = true)
    private void injectSearchItemsFilter(CallbackInfoReturnable<Collection<ItemStack>> cir) {
        if (cached_filteredSearchTab == null && !cir.getReturnValue().isEmpty()) { //Cache the search tab
            LOGGER.info("tab {}: \tCaching search tab display items...", this.displayName.getString());
            cached_filteredSearchTab = filterItems(cir.getReturnValue());
        }

        if (cached_filteredSearchTab != null) cir.setReturnValue(cached_filteredSearchTab);
    }

    //Cached values
    @Unique
    private ItemStack cached_TabIcon = null;
    @Unique
    private boolean isCachedCustomIcon = false;

    @Unique
    private Collection<ItemStack> cached_FilteredDisplayItems = null;
    @Unique
    private Collection<ItemStack> cached_filteredSearchTab = null;

    /**
     * Resets the cache for this tab
     */
    @Override
    public void rebuildCache() {
        isCachedCustomIcon = false;
        cached_TabIcon = null;
        cached_FilteredDisplayItems = null;
        cached_filteredSearchTab = null;
    }


    //TODO: Make sure things like this arent happening anywhere else
    //This method is called EVERY time the icon is requested, so we need to cache it
    @Inject(method = "getIconItem", at = @At("RETURN"), cancellable = true)
    private void injectIcon(CallbackInfoReturnable<ItemStack> cir) {
        if (!isCachedCustomIcon) {
            //Set the tab icon
            String tabKey = getTranslationKey(this.displayName);
            CreativeTabUtils.replacementTab(convertName(tabKey)).ifPresent(tabData -> {
                LOGGER.info("tab {}: \tCaching tab icon...", this.displayName.getString());
                cached_TabIcon = CreativeTabUtils.makeTabIcon(tabData.getLeft()).get();
            });
            isCachedCustomIcon = true;
        }

        if (cached_TabIcon != null && !cached_TabIcon.isEmpty())
            cir.setReturnValue(cached_TabIcon);
    }


    @Unique
    private String convertName(String tabName) {
        return tabName.replace(".", "_");
    }
}
