package org.lightning.neutrontools.mixin.creativeTabs;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.InsertableLinkedOpenCustomHashSet;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.spongepowered.asm.mixin.*;

@Mixin(value = BuildCreativeModeTabContentsEvent.class, remap = false)
public class MixinCreativeTabCrashFix {

    @Shadow
    @Final
    private InsertableLinkedOpenCustomHashSet<ItemStack> parentEntries;
    @Shadow
    @Final
    private InsertableLinkedOpenCustomHashSet<ItemStack> searchEntries;

    @Shadow
    static protected void assertStackCount(ItemStack stack) {
    }

    @Shadow
    static protected boolean isParentTab(CreativeModeTab.TabVisibility visibility) {
        return false;
    }

    @Shadow
    static protected boolean isSearchTab(CreativeModeTab.TabVisibility visibility) {
        return false;
    }

    @Shadow
    protected void assertTargetExists(InsertableLinkedOpenCustomHashSet<ItemStack> setToCheck, ItemStack target) {
    }

    /**
     * @author Lightning323
     * @reason Remove strict duplicate checking to prevent Every Compat/Moonlight crashes.
     */
    @Overwrite
    public void accept(ItemStack newEntry, CreativeModeTab.TabVisibility visibility) {
        assertStackCount(newEntry);
        if (isParentTab(visibility)) {
            if (!this.doesNewEntryAlreadyExist(this.parentEntries, newEntry)) this.parentEntries.add(newEntry);
        }

        if (isSearchTab(visibility)) {
            if (!this.doesNewEntryAlreadyExist(this.searchEntries, newEntry)) this.searchEntries.add(newEntry);
        }
    }

    /**
     * @author Lightning323
     * @reason Remove strict duplicate checking to prevent Every Compat/Moonlight crashes.
     */
    @Overwrite
    public void insertAfter(ItemStack existingEntry, ItemStack newEntry, CreativeModeTab.TabVisibility visibility) {
        assertStackCount(newEntry);
        if (isParentTab(visibility)) {
            if (this.doesTargetExist(this.parentEntries, existingEntry)
                    && !this.doesNewEntryAlreadyExist(this.parentEntries, newEntry))
                this.parentEntries.addAfter(existingEntry, newEntry);
        }

        if (isSearchTab(visibility)) {
            if (this.doesTargetExist(this.searchEntries, existingEntry)
                    && !this.doesNewEntryAlreadyExist(this.searchEntries, newEntry))
                this.searchEntries.addAfter(existingEntry, newEntry);
        }
    }

    //non error throwing Replacement for assertNewEntryDoesNotAlreadyExists
    @Unique
    private boolean doesNewEntryAlreadyExist(InsertableLinkedOpenCustomHashSet<ItemStack> setToCheck, ItemStack existingEntry) {
        return setToCheck.contains(existingEntry);
    }

    @Unique
    private boolean doesTargetExist(InsertableLinkedOpenCustomHashSet<ItemStack> setToCheck, ItemStack existingEntry) {
        return setToCheck.contains(existingEntry);
    }

//    @Inject(
//            method = "assertNewEntryDoesNotAlreadyExists",
//            at = @At("HEAD"),
//            cancellable = true
//    )
//    private void hideDuplicateItemException(InsertableLinkedOpenCustomHashSet<ItemStack> setToCheck, ItemStack newEntry, CallbackInfo ci) {
//        if (setToCheck.contains(newEntry)) {
//            // Optional: Log it so you know which items are being duplicated
//            // System.out.println("Suppressed duplicate creative tab entry: " + newEntry);
//
//            ci.cancel(); // Stop the method here before it throws the IllegalArgumentException
//        }
//    }
}