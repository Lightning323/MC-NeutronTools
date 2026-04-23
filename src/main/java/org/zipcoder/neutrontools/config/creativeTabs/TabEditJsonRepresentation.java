package org.zipcoder.neutrontools.config.creativeTabs;

import com.google.gson.*;
import org.zipcoder.neutrontools.NeutronTools;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

 class TabEditJsonRepresentation {
    public TabIcon tab_icon;
    public String tab_name_key;
    public ArrayList<ItemAdditionEntry> items_to_add;
    public ArrayList<ItemRemovalEntry> items_to_remove;

    public static void load(File jsonFile, HashMap<String, TabEditJsonRepresentation> tabMap) {
        Gson gson = new Gson();

        try {
            // 1. Read JSON string as a JsonObject
            JsonObject rootObject = JsonParser.parseString(Files.readString(jsonFile.toPath())).getAsJsonObject();

            // 2. Get each element of the root object
            for (Map.Entry<String, JsonElement> entry : rootObject.entrySet()) {
                String tabId = entry.getKey();
                JsonElement tabData = entry.getValue();

                try {
                    // 3. Serialize (Deserialize) and add to the hashmap
                    // This will throw a JsonSyntaxException if the inner JSON is invalid
                    TabEditJsonRepresentation tabObject = gson.fromJson(tabData, TabEditJsonRepresentation.class);
                    tabMap.put(tabId, tabObject);
                } catch (JsonSyntaxException e) {
                    NeutronTools.LOGGER.error("Failed to parse tab: " + tabId, e);
                }
            }

        } catch (Exception e) {
            NeutronTools.LOGGER.error("Failed to read tab edit json structure", e);
        }
    }

    public static class ItemAdditionEntry {
        public int index;
        public ArrayList<String> names;
        public String match_name;
        public ArrayList<String> match_tags;
        public String match_tab;
        public String nbt;
    }

    public static class ItemRemovalEntry {
        public ArrayList<String> names;
        public String match_name;
        public ArrayList<String> match_tags;
    }

    public static class TabIcon {
        public String name;
        public String nbt;
    }
}
