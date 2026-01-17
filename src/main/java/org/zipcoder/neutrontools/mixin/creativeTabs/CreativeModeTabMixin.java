package org.zipcoder.neutrontools.mixin.creativeTabs;

import org.zipcoder.creativetabs.client.data.CustomCreativeTabJsonHelper;
import org.zipcoder.creativetabs.client.impl.CreativeModeTabMixin_I;
import org.zipcoder.creativetabs.client.tabs.CreativeTabCustomizationData;
import org.zipcoder.creativetabs.utils.CreativeTabUtils;
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

import static org.zipcoder.creativetabs.utils.CreativeTabUtils.getTabKey;
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

        if (CreativeTabCustomizationData.INSTANCE.getNewTabs().contains(self) && CreativeTabCustomizationData.INSTANCE.getTabItems().containsKey(self)) {
            ci.cancel();

            displayItems.clear();
            displayItemsSearchTab.clear();
            List<ItemStack> stacks = CreativeTabCustomizationData.INSTANCE.getTabItems().get(self);

            displayItems.addAll(stacks);
            displayItemsSearchTab.addAll(stacks);
            rebuildSearchTree();
        }
    }

    @Inject(method = "hasAnyItems", at = @At("RETURN"), cancellable = true)
    private void injectHasAnyItems(CallbackInfoReturnable<Boolean> cir) {
        CreativeModeTab self = (CreativeModeTab) ((Object) this);

        if (CreativeTabCustomizationData.INSTANCE.getNewTabs().contains(self)
                && CreativeTabCustomizationData.INSTANCE.getTabItems().containsKey(self)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void injectDisplayName(CallbackInfoReturnable<Component> cir) {
        Component value = this.displayName;
        CreativeTabUtils.replacementTab(convertName(getTabKey(value))).ifPresent(tabData -> {
            if (!CreativeTabCustomizationData.INSTANCE.isShowTabNames()) {
                cir.setReturnValue(Component.translatable(CreativeTabUtils.prefix(tabData.getLeft().getTabName())));
            }
        });

        if (!CreativeTabCustomizationData.INSTANCE.isShowTabNames())
            return;

        cir.setReturnValue(Component.literal(getTabKey(value)));
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
//        cir.setReturnValue(filterItems(cir.getReturnValue()));
    }

    @Inject(method = "getSearchTabDisplayItems", at = @At("RETURN"), cancellable = true)
    private void injectSearchItemsFilter(CallbackInfoReturnable<Collection<ItemStack>> cir) {
        if (cached_filteredSearchTab == null && !cir.getReturnValue().isEmpty()) { //Cache the search tab
            LOGGER.info("tab {}: \tCaching search tab display items...", this.displayName.getString());
            cached_filteredSearchTab = filterItems(cir.getReturnValue());
        }

        if (cached_filteredSearchTab != null) cir.setReturnValue(cached_filteredSearchTab);
//        cir.setReturnValue(filterItems(cir.getReturnValue()));
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
            String tabKey = getTabKey(this.displayName);
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
    private Collection<ItemStack> filterItems(Collection<ItemStack> inputStacks) {
        CreativeModeTab self = (CreativeModeTab) ((Object) this);
        if (CreativeTabCustomizationData.INSTANCE.getNewTabs().contains(self) || self.getType() == CreativeModeTab.Type.SEARCH)
            return inputStacks;

        Collection<ItemStack> oldStacks = this.displayItems;

        Optional<Pair<CustomCreativeTabJsonHelper, List<ItemStack>>> replacementTab = CreativeTabUtils.replacementTab(convertName(getTabKey(this.displayName)));
        if (replacementTab.isPresent()) {
            List<ItemStack> returnStacks = new ArrayList<>(replacementTab.get().getRight());

            if (replacementTab.get().getLeft().getTabItems().stream().anyMatch(i -> i.getName().equalsIgnoreCase("existing"))) {
                returnStacks.addAll(oldStacks.stream().filter(i -> !CreativeTabCustomizationData.INSTANCE.getHiddenItems().contains(i.getItem())).toList());
            }

            return returnStacks;
        }

        Collection<ItemStack> filteredStacks = new ArrayList<>();

        if (oldStacks != null && !oldStacks.isEmpty()) {
            oldStacks.forEach(i -> {
                if (!CreativeTabCustomizationData.INSTANCE.getHiddenItems().contains(i.getItem())) {
                    filteredStacks.add(i);
                }
            });

            if (!filteredStacks.isEmpty()) {
                return filteredStacks;
            }
        }

        return inputStacks;
    }

    @Unique
    private String convertName(String tabName) {
        return tabName.replace(".", "_");
    }
}
