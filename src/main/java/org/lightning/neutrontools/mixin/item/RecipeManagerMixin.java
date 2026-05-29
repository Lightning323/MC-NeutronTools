package org.lightning.neutrontools.mixin.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.lightning.neutrontools.NeutronTools;
import org.lightning.neutrontools.utils.ItemUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={RecipeManager.class})
public class RecipeManagerMixin {
    @Inject(at={@At(value="HEAD")}, method={"apply"}, cancellable=true)
    private void neutron$apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        HashMap<ResourceLocation, JsonElement> filteredMap = new HashMap<ResourceLocation, JsonElement>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            ResourceLocation ResourceLocation2 = entry.getKey();
            JsonElement jsonElement = entry.getValue();
            try {
                boolean shouldDisable;
                block10: {
                    JsonElement resultElement;
                    block11: {
                        String itemId;
                        block9: {
                            JsonObject jsonObject = GsonHelper.convertToJsonObject((JsonElement)jsonElement, (String)"top element");
                            resultElement = jsonObject.get("result");
                            if (resultElement == null) {
                                if (jsonObject.get("output") != null) {
                                    resultElement = jsonObject.get("output");
                                } else {
                                    filteredMap.put(ResourceLocation2, jsonElement);
                                    continue;
                                }
                            }
                            shouldDisable = false;
                            if (!resultElement.isJsonObject()) break block9;
                            itemId = this.getResultItemId(resultElement.getAsJsonObject());
                            if (itemId == null || !ItemUtils.shouldRecipeBeDisabled(itemId)) break block10;
                            shouldDisable = true;
                            break block10;
                        }
                        if (!resultElement.isJsonPrimitive() || !resultElement.getAsJsonPrimitive().isString()) break block11;
                        itemId = resultElement.getAsString();
                        if (itemId == null || !ItemUtils.shouldRecipeBeDisabled(itemId)) break block10;
                        shouldDisable = true;
                        break block10;
                    }
                    if (resultElement.isJsonArray()) {
                        JsonArray resultArray = resultElement.getAsJsonArray();
                        for (JsonElement element : resultArray) {
                            String itemId;
                            if (element.isJsonObject()) {
                                itemId = this.getResultItemId(element.getAsJsonObject());
                                if (itemId == null || !ItemUtils.shouldRecipeBeDisabled(itemId)) continue;
                                shouldDisable = true;
                                break;
                            }
                            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString() || (itemId = element.getAsString()) == null || !ItemUtils.shouldRecipeBeDisabled(itemId)) continue;
                            shouldDisable = true;
                            break;
                        }
                    }
                }
                if (shouldDisable) continue;
                filteredMap.put(ResourceLocation2, jsonElement);
            }
            catch (JsonParseException | IllegalArgumentException e) {
                NeutronTools.LOG.debug("Parsing error loading recipe {}", (Object)ResourceLocation2, (Object)e);
                filteredMap.put(ResourceLocation2, jsonElement);
            }
        }
        map.clear();
        map.putAll(filteredMap);
    }

    private String getResultItemId(JsonObject resultObject) {
        if (resultObject.has("item")) {
            return GsonHelper.getAsString((JsonObject)resultObject, (String)"item");
        }
        if (resultObject.has("id")) {
            return GsonHelper.getAsString((JsonObject)resultObject, (String)"id");
        }
        if (resultObject.has("result")) {
            return GsonHelper.getAsString((JsonObject)resultObject, (String)"result");
        }
        if (resultObject.has("output")) {
            return GsonHelper.getAsString((JsonObject)resultObject, (String)"output");
        }
        return null;
    }
}