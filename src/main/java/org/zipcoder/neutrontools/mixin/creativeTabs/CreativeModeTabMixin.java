package org.zipcoder.neutrontools.mixin.creativeTabs;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.zipcoder.neutrontools.NeutronTools;
import org.zipcoder.neutrontools.creativetabs.client.data.NewTabJsonHelper;
import org.zipcoder.neutrontools.creativetabs.client.impl.CreativeModeTabMixin_I;
import org.zipcoder.neutrontools.creativetabs.client.data.CreativeTabCustomizationData;
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
        if (CreativeTabCustomizationData.INSTANCE.getNewTabs().contains(self)
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

//    @Inject(method = "buildContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab;rebuildSearchTree()V"))
//    private void addon_buildContents(CreativeModeTab.ItemDisplayParameters displayContext, CallbackInfo ci) {
//    }


    @Unique
    private Collection<ItemStack> editItemStacks(Collection<ItemStack> inputStacks) {
        CreativeModeTab self = (CreativeModeTab) ((Object) this);

        //If this is a new tab or the search tab, return the input stacks
        if (CreativeTabCustomizationData.INSTANCE.getNewTabs().contains(self) || self.getType() == CreativeModeTab.Type.SEARCH)
            return inputStacks;

        //Get the original stacks
        Collection<ItemStack> originalStacks = this.displayItems;

        //Get the items to remove
        Set<Item> itemsToRemove = new HashSet<>();
        itemsToRemove.addAll(CreativeTabCustomizationData.INSTANCE.getHiddenItems());

        Set<Item> tabRemovals = CreativeTabCustomizationData.INSTANCE.tabRemovals.get(self);
        if (tabRemovals != null && !tabRemovals.isEmpty()) {
            itemsToRemove.addAll(tabRemovals);
        }

        //get the items to add
        List<ItemStack> itemsToAdd = CreativeTabCustomizationData.INSTANCE.tabAdditions.get(self);


        Pair<NewTabJsonHelper, List<ItemStack>> replacementTab = CreativeTabUtils.getReplacementTab(self);
        if (replacementTab != null) {
            List<ItemStack> returnStacks = new ArrayList<>(replacementTab.getRight());

            if (replacementTab.getLeft().getTabItems().stream().anyMatch(i -> i.getName().equalsIgnoreCase("existing"))) {
                returnStacks.addAll(originalStacks.stream().filter(
                        i ->
                                !itemsToRemove.contains(i.getItem())).toList());
            }

            if (itemsToAdd != null) returnStacks.addAll(itemsToAdd); //Add items right before returning it
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
                if (itemsToAdd != null) filteredStacks.addAll(itemsToAdd); //Add items right before returning it
                return filteredStacks;
            }
        }

        if (itemsToAdd != null) inputStacks.addAll(itemsToAdd); //Add items right before returning it
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

        if (CreativeTabCustomizationData.INSTANCE.getTabNameMode() == CreativeTabCustomizationData.TabNameMode.RESOURCE_ID) {
            ResourceLocation resourceLocation = getRegistryID(self);
            if (resourceLocation != null) {
                cir.setReturnValue(Component.literal(resourceLocation.toString()));
            } else {
                cir.setReturnValue(Component.literal(""));
            }
        } else if (CreativeTabCustomizationData.INSTANCE.getTabNameMode() == CreativeTabCustomizationData.TabNameMode.TRANSLATION_KEY) {
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
            cached_FilteredDisplayItems = editItemStacks(cir.getReturnValue());
        }
        if (cached_FilteredDisplayItems != null) cir.setReturnValue(cached_FilteredDisplayItems);
    }

    @Inject(method = "getSearchTabDisplayItems", at = @At("RETURN"), cancellable = true)
    private void injectSearchItemsFilter(CallbackInfoReturnable<Collection<ItemStack>> cir) {
        if (cached_filteredSearchTab == null && !cir.getReturnValue().isEmpty()) { //Cache the search tab
            LOGGER.debug("tab {}: \tCaching search tab display items...", this.displayName.getString());
            cached_filteredSearchTab = editItemStacks(cir.getReturnValue());
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
    public void rebuildCache() {
        isCachedCustomIcon = false;
        isCachedCustomDisplayName = false;
        cached_TabIcon = null;
        cached_FilteredDisplayItems = null;
        cached_filteredSearchTab = null;
        cached_displayName = null;
    }


    //TODO: Make sure things like this arent happening anywhere else
    //This method is called EVERY time the icon is requested, so we need to cache it
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
