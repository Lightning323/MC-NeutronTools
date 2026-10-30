package org.lightning.neutrontools.mixin.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.lightning.neutrontools.NeutronTools;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {ItemEntity.class})
public class ItemEntityMixin {
    @Inject(at = {@At(value = "TAIL")}, method = {"readAdditionalSaveData"}, cancellable = true)
    public void item_obliterator$discardItemEntities(CompoundTag nbt, CallbackInfo info) {
        ItemEntity self = ((ItemEntity) (Object) this);
        if (NeutronTools.CONFIG_DISABLED_ITEMS.matches(self.getItem())) {
            self.remove(Entity.RemovalReason.DISCARDED);
        }
    }
}
