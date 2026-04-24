package org.lightning.neutrontools.mixin.disableExpSettings;

import com.mojang.serialization.Lifecycle;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import org.lightning.neutrontools.NeutronTools;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Debug(
   export = true
)
@Mixin({WorldOpenFlows.class})
public class WorldOpenFlowsMixin {
   @ModifyVariable(
      method = {"confirmWorldCreation(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/screens/worldselection/CreateWorldScreen;Lcom/mojang/serialization/Lifecycle;Ljava/lang/Runnable;Z)V"},
      at = @At("HEAD"),
      argsOnly = true
   )
   private static Lifecycle alwaysStable(Lifecycle cycle) {
      return Lifecycle.stable();
   }

   @ModifyVariable(
      method = {"openWorldCheckWorldStemCompatibility(Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lnet/minecraft/server/WorldStem;Lnet/minecraft/server/packs/repository/PackRepository;Ljava/lang/Runnable;)V"},
      at = @At("STORE"),
      ordinal = 1
   )
   public boolean no(boolean a) {
      return !NeutronTools.CONFIG.disableExperementalSettings;
   }
}