package org.lightning.neutrontools.mixin.creativeTabs;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lightning.neutrontools.config.creativeTabs.CreativeTabConfig;
import org.lightning.neutrontools.config.creativeTabs.TabEditConfig;
import org.lightning.neutrontools.creativetabs.CreativeTabUtils;
import org.lightning.neutrontools.creativetabs.NeutronCreativeTabs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.lightning.neutrontools.NeutronTools.LOGGER;
import static org.lightning.neutrontools.NeutronTools.MODID;
import static org.lightning.neutrontools.creativetabs.CreativeTabUtils.getTranslationKey;

@Mixin(CreativeModeTab.class)
public abstract class CreativeModeTabMixin {

    @Shadow
    private Collection<ItemStack> displayItems;
    @Shadow
    private Set<ItemStack> displayItemsSearchTab;

    @Shadow
    @Final
    private Component displayName;
    @Shadow
    @Final
    private int searchBarWidth;

    //Cached values
    @Unique
    private ItemStack cached_TabIcon = null;
    @Unique
    private boolean isCachedCustomIcon = false;
    @Unique
    private Component cached_displayName = null;


    /// //////////////////////////////////////////
    /// Injections ============================ //
    /// //////////////////////////////////////////

    @Inject(method = "buildContents", at = @At("TAIL"), cancellable = true)
    private void injectBuildContents(CreativeModeTab.ItemDisplayParameters arg, CallbackInfo ci) {
        CreativeModeTab tab = (CreativeModeTab) ((Object)this);
        if (!NeutronCreativeTabs.MANDATORY_TABS.contains(tab)) { //We should not modify mandatory tabs
            NeutronCreativeTabs.INSTANCE.cache.buildContents(tab,
                    tab.getDisplayItems(),
                    tab.getSearchTabDisplayItems());
            if (CreativeTabConfig.INSTANCE.isTabDisabled(tab)) {
                tab.getDisplayItems().clear();
                tab.getSearchTabDisplayItems().clear();
            } else {
                HashSet<Item> disabled_items = new HashSet<>(CreativeTabConfig.INSTANCE.disabledItems);
                //NOTE: We dont have to hide the items hidden manually because if they were hidden from all tabs, they should have been hidden with disabledItems
//        TabEditConfig tabEditConfig = CreativeTabConfig.INSTANCE.tabEdits.get(self);
//        if (tabEditConfig != null) disabled_items.addAll(tabEditConfig.items_to_remove);
                tab.getSearchTabDisplayItems().removeIf(stack -> disabled_items.contains(stack.getItem()));
            }
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
            if (!NeutronCreativeTabs.MANDATORY_TABS.contains(self)) {
                TabEditConfig tabEditConfig = CreativeTabConfig.INSTANCE.tabEdits.get(self);
                if (tabEditConfig != null
                        && tabEditConfig.tab_icon != null
                        && tabEditConfig.tab_icon.get() != ItemStack.EMPTY) {
                    LOGGER.debug("tab {}: \tCaching tab icon...", this.displayName.getString());
                    cached_TabIcon = tabEditConfig.tab_icon.get();
                }
            }
            isCachedCustomIcon = true;
        }

        if (cached_TabIcon != null && !cached_TabIcon.isEmpty())
            cir.setReturnValue(cached_TabIcon);

    }
}
