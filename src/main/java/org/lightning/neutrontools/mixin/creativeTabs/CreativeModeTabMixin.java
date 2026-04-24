package org.lightning.neutrontools.mixin.creativeTabs;

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
import org.lightning.neutrontools.config.creativeTabs.CreativeTabConfig;
import org.lightning.neutrontools.config.creativeTabs.TabEditConfig;
import org.lightning.neutrontools.creativetabs.NeutronCreativeTabs;
import org.lightning.neutrontools.creativetabs.client.impl.CreativeModeTabMixin_I;
import org.lightning.neutrontools.events.ClientModEvents;
import org.lightning.neutrontools.creativetabs.CreativeTabUtils;

import java.util.*;

import static org.lightning.neutrontools.NeutronTools.LOGGER;
import static org.lightning.neutrontools.NeutronTools.MODID;
import static org.lightning.neutrontools.creativetabs.CreativeTabUtils.getTranslationKey;

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

    /// //////////////////////////////////////////
    /// Injections ============================ //
    /// //////////////////////////////////////////


    @Inject(method = "buildContents", at = @At("TAIL"), cancellable = true)
    private void injectBuildContents(CreativeModeTab.ItemDisplayParameters arg, CallbackInfo ci) {
        CreativeModeTab self = (CreativeModeTab) (Object) this;

        //Add original tab items to the config first
        NeutronCreativeTabs.cached_originalCreativeTabItems.put(CreativeTabUtils.getRegistryID(self), new ArrayList<>(displayItems));
        NeutronCreativeTabs.cached_originalCreativeTabs.add(self);

        // Get the total number of registered tabs
        int totalTabs = BuiltInRegistries.CREATIVE_MODE_TAB.size();

        //Remove disabled items right away (Dont wait)
        //We can only hide items from search if they are hidden at the right time
        HashSet<Item> disabled_items = new HashSet<>(CreativeTabConfig.INSTANCE.disabledItems);
        //NOTE: We dont have to hide the items hidden manually because if they were hidden from all tabs, they should have been hidden with disabledItems
//        TabEditConfig tabEditConfig = CreativeTabConfig.INSTANCE.tabEdits.get(self);
//        if (tabEditConfig != null) disabled_items.addAll(tabEditConfig.items_to_remove);
        displayItemsSearchTab.removeIf(stack -> disabled_items.contains(stack.getItem()));

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
            TabEditConfig tabEditConfig = CreativeTabConfig.INSTANCE.tabEdits.get(tab);
            if (tabEditConfig != null) {
                tabEditConfig.modifyDisplayItems(displayItems, displayItemsSearchTab);
            }
        }
    }

    @Unique
    private void onAllTabsFinishedBuilding() {
        LOGGER.info("Modifying items for all tabs; Tags ready: {}", ClientModEvents.isTagsReady());
        CreativeTabConfig.INSTANCE.load();
        for (CreativeModeTab tab : NeutronCreativeTabs.cached_originalCreativeTabs) {
            ((CreativeModeTabMixin_I) tab).modifyItems();
        }
    }

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void injectDisplayName(CallbackInfoReturnable<Component> cir) {

        CreativeModeTab self = (CreativeModeTab) ((Object) this);

        if (cached_displayName == null) {
            TabEditConfig tabEditConfig = CreativeTabConfig.INSTANCE.tabEdits.get(self);
            if (tabEditConfig == null || tabEditConfig.tab_name_key == null) {
                cached_displayName = this.displayName;
            } else {
                cached_displayName = Component.translatable("itemGroup." + MODID + "." +
                        tabEditConfig.tab_name_key.replace("itemGroup.", "")
                                .replace(".", "_")
                                .replace(" ", "_")); //translatable (needs lang file)
//                cached_displayName = Component.literal(tabEditConfig.tab_name_key); //Literal (no need for translation)
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
            TabEditConfig tabEditConfig = CreativeTabConfig.INSTANCE.tabEdits.get(self);
            if (tabEditConfig != null
                    && tabEditConfig.tab_icon != null
                    && tabEditConfig.tab_icon.get() != ItemStack.EMPTY) {
                LOGGER.debug("tab {}: \tCaching tab icon...", this.displayName.getString());
                cached_TabIcon = tabEditConfig.tab_icon.get();
            }
            isCachedCustomIcon = true;
        }

        if (cached_TabIcon != null && !cached_TabIcon.isEmpty())
            cir.setReturnValue(cached_TabIcon);

    }


}
