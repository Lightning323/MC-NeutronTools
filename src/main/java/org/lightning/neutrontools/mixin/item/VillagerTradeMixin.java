package org.lightning.neutrontools.mixin.item;

import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffers;
import org.lightning.neutrontools.utils.ItemUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={MerchantMenu.class}, priority=10000)
public class VillagerTradeMixin {
    @Inject(at={@At(value="RETURN")}, method={"getOffers"}, cancellable=true)
    public void getOffers(CallbackInfoReturnable<MerchantOffers> info) {
        if (info.getReturnValue() != null) {
            MerchantOffers offers = new MerchantOffers();
            ((MerchantOffers)info.getReturnValue()).forEach(offer -> {
                if (!ItemUtils.isDisabled(offer)) {
                    offers.add(offer);
                }
            });
            info.setReturnValue((MerchantOffers) offers);
        }
    }
}
