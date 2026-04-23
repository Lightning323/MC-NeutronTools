package org.zipcoder.neutrontools.config.creativeTabs;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.zipcoder.neutrontools.utils.CreativeTabUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Tab edit config is called after the tags and creative tabs have been loaded.
 * We take all that information and make a concrete set of items to add and remove.
 */
public class TabEditConfig {

    public Supplier<ItemStack> tab_icon;
    public String tab_name_key;
    public Map<Integer, List<ItemStack>> items_to_add;
    public ArrayList<Item> items_to_remove;

    public TabEditConfig(TabEditJsonRepresentation json) {
        this.tab_name_key = json.tab_name_key;
        this.tab_icon = CreativeTabUtils.makeTabIcon(json.tab_icon.name, json.tab_icon.nbt);
        items_to_add = new HashMap<>();
        items_to_remove = new ArrayList<>();

        json.items_to_add.forEach(item -> {

            int index = item.index;

            ArrayList<ItemStack> stacks = new ArrayList<>();

            if (item.names != null) {
                for (String name : item.names) {
                    stacks.add(CreativeTabUtils.makeItemStack(name, item.nbt));
                }
            }

            if (item.match_name != null) {

            }
            if (item.match_tab != null) {

            }
            if (item.match_tags != null) {

            }

        });
    }
}