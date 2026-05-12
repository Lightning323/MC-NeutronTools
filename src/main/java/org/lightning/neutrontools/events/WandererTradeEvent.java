package org.lightning.neutrontools.events;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;
import org.lightning.neutrontools.utils.ItemUtils;

public class WandererTradeEvent {
    @SubscribeEvent
    public static void onWandererTrades(WandererTradesEvent event) {
        event.getGenericTrades().removeIf(itemListing -> {
            try {
                MerchantOffer offer = itemListing.getOffer(null, RandomSource.create());
                if (offer == null) {
                    return false;
                }
                return ItemUtils.isDisabled(offer);
            }
            catch (NullPointerException e) {
                return false;
            }
        });
        event.getRareTrades().removeIf(itemListing -> {
            try {
                MerchantOffer offer = itemListing.getOffer(null, RandomSource.create());
                if (offer == null) {
                    return false;
                }
                return ItemUtils.isDisabled(offer);
            }
            catch (NullPointerException e) {
                return false;
            }
        });
    }
}
