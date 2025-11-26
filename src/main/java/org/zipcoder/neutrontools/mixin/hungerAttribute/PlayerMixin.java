package org.zipcoder.neutrontools.mixin.hungerAttribute;

import me.Masonhades.hungerattribute.attribute.ModAttributes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerMixin {
    @ModifyArg(
            method = "causeFoodExhaustion",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/food/FoodData;addExhaustion(F)V"
            ),
            index = 0
    )
    private float modifyExhaustion(float exhaustion) {
        Player player = (Player)(Object)this;
        var attr = player.getAttribute(ModAttributes.HUNGER_MULTIPLIER.get());
        if (attr != null) {
            return exhaustion * (float) attr.getValue();
        }
        return exhaustion;
    }

}
