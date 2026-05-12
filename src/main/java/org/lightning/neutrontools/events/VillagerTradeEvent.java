package org.lightning.neutrontools.events;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import org.lightning.neutrontools.utils.ItemUtils;

public class VillagerTradeEvent {
    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        event.getTrades().forEach((level, trades) -> trades.removeIf(itemListing -> {
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
        }));
    }
}
