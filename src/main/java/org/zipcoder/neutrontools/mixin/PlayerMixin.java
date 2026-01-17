package org.zipcoder.neutrontools.mixin;

import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.zipcoder.neutrontools.NeutronTools;

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
        float level = exhaustion * NeutronTools.CONFIG.hungerMultiplier;
//        System.out.println("Exaustion level: "+level+ " Hunger: "+NeutronTools.CONFIG.hungerMultiplier);
        return level;
    }

    //TODO: Add sprint speed boost
//    // Change this to whatever multiplier your goofy heart desires
//    private static final float SPEED_MULTIPLIER = 2.0f; // 2x sprint speed
//
//    @Inject(method = "getMovementSpeed", at = @At("RETURN"), cancellable = true)
//    private void injectedSpeedBooster(CallbackInfoReturnable<Float> cir) {
//        float base = cir.getReturnValue();
//        cir.setReturnValue(base * SPEED_MULTIPLIER);
//    }

    @Shadow
    @Final
    private Abilities abilities;

    @Inject(method = "getPortalWaitTime", at = @At("HEAD"), cancellable = true)
    public void getPortalWaitTime(CallbackInfoReturnable<Integer> cir) {
        int time = abilities.invulnerable ? 1 : NeutronTools.CONFIG.portalWaitTime;
//        System.out.println("Portal Wait time: "+time);
        cir.setReturnValue(time);
    }

}
