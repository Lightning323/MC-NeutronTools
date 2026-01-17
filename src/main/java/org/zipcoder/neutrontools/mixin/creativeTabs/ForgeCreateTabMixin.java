package org.zipcoder.neutrontools.mixin.creativeTabs;

import org.zipcoder.creativetabs.client.impl.ForgeTabData;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CreativeModeTab.class)
public class ForgeCreateTabMixin implements ForgeTabData {

    @Unique
    private int page_index = -1;

    @Override
    public int getPageIndex() {
        return page_index;
    }

    @Override
    public void setPageIndex(int page) {
        this.page_index = page;
    }
}
