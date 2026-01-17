package org.zipcoder.creativetabs.client.tabs;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.zipcoder.creativetabs.ModConstants;
import org.zipcoder.creativetabs.client.data.CustomCreativeTabJsonHelper;
import org.zipcoder.creativetabs.client.data.ItemTabJsonHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.ItemStack;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.zipcoder.creativetabs.utils.CreativeTabUtils.getItemStack;

/**
 * TODO: There is an issue where some items (like a light item) dont get added to creative tab
 */
public class CreativeTabRegistryAddon {
    CustomCreativeTabRegistry registry;

    public final HashMap<String, List<ItemStack>> itemsToAdd = new HashMap<>();
    public final HashMap<String, List<String>> itemsToDelete = new HashMap<>();

    public void clear() {
        itemsToAdd.clear();
        itemsToDelete.clear();
    }

    public CreativeTabRegistryAddon(CustomCreativeTabRegistry registry) {
        this.registry = registry;
    }

    public ItemStack makeStack(CustomCreativeTabJsonHelper.TabItem item) {
        ItemStack stack = getItemStack(item.getName());
        if (stack.isEmpty()) return stack;

        if (item.getNbt() != null && !item.getNbt().isEmpty()) {
            try {
                CompoundTag tag = TagParser.parseTag(item.getNbt());
                stack.setTag(tag);

                if (tag.contains("customName"))
                    stack.setHoverName(Component.literal(tag.getString("customName")));
            } catch (CommandSyntaxException e) {
                ModConstants.logger.error("Failed to Process NBT for Item {}", item.getName(), e);
            }
        }
        return stack;
    }

    public void loadItemsForTabs(Map<ResourceLocation, Resource> entries) {
        for (Map.Entry<ResourceLocation, Resource> entry : entries.entrySet()) {
            ResourceLocation location = entry.getKey();
            Resource resource = entry.getValue();

            ModConstants.logger.info("Processing {}", location.toString());

            //Iterate over each resource (JSON file)
            try (InputStream stream = resource.open()) {
                ItemTabJsonHelper json = registry.GSON.fromJson(new InputStreamReader(stream), ItemTabJsonHelper.class);

                //Iterate over each tab in the json file
                json.getTabs().forEach(tab -> {
                    String tabName = tab.tabName;

                    //Add new tab entries if they don't exist
                    if (itemsToAdd.get(tabName) == null) {
                        itemsToAdd.put(tabName, new ArrayList<>());
                    }
                    if (itemsToDelete.get(tabName) == null) {
                        itemsToDelete.put(tabName, new ArrayList<>());
                    }

                    //Add the items to the final entry
                    for (int i = 0; i < tab.itemsAdd.length; i++) {
                        ItemStack stack = makeStack(tab.itemsAdd[i]);
                        //Only add item stacks that actually exist
                        if (!stack.isEmpty()) itemsToAdd.get(tabName).add(stack);
                    }

                    for (int i = 0; i < tab.itemsRemove.length; i++) {
                        itemsToDelete.get(tabName).add(tab.itemsRemove[i]);
                    }

                });

            } catch (Exception e) {
                ModConstants.logger.error("Failed to process items in creative tab", e);
            }
        }
    }


}
