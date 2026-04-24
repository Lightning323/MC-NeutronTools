package org.lightning.neutrontools.config.creativeTabs;

import com.google.gson.*;
import org.lightning.neutrontools.NeutronTools;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.BiConsumer;

class TabEditJsonRepresentation {
    public TabIcon tab_icon;
    public String tab_name_key;
    public ArrayList<ItemAdditionEntry> items_to_add;
    public ArrayList<ItemRemovalEntry> items_to_remove;
    private final static Gson GSON = new Gson();


//    public static TabEditJsonRepresentation load(File jsonFile) {
//        if (!jsonFile.exists()) return null;
//        try (JsonReader jsonReader = new JsonReader(new FileReader(jsonFile))) {
//            return GSON.fromJson(jsonReader, TabEditJsonRepresentation.class);
//        } catch (Exception e) {
//            NeutronTools.LOGGER.error("Failed to parse tab", e);
//        }
//        return null;
//    }

    public static void load(File jsonFile,BiConsumer<String,TabEditJsonRepresentation> consumer) {

        if (!jsonFile.exists()) return;
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
                    TabEditJsonRepresentation tabObject = GSON.fromJson(tabData, TabEditJsonRepresentation.class);
                    consumer.accept(tabId, tabObject);
                } catch (JsonSyntaxException e) {
                    NeutronTools.LOGGER.error("Failed to parse tab: " + tabId, e);
                }
            }

        } catch (Exception e) {
            NeutronTools.LOGGER.error("Failed to read tab edit json structure", e);
        }
    }

    public static class ItemAdditionEntry {
        public int index = -1;
        public ArrayList<String> names;
        public String match_name;
        public ArrayList<String> match_tags;
        public String match_tab;
        public String nbt;


        @Override
        public String toString() {
            return "ItemAdditionEntry{" +
                    "index=" + index +
                    ", names=" + names +
                    ", match_name='" + match_name + '\'' +
                    ", match_tags=" + match_tags +
                    ", match_tab='" + match_tab + '\'' +
                    ", nbt='" + nbt + '\'' +
                    '}';
        }
    }

    public static class ItemRemovalEntry {
        public ArrayList<String> names;
        public String match_name;
        public ArrayList<String> match_tags;
        public String match_tab;


        @Override
        public String toString() {
            return "ItemRemovalEntry{" +
                    "names=" + names +
                    ", match_name='" + match_name + '\'' +
                    ", match_tags=" + match_tags +
                    '}';
        }
    }

    public static class TabIcon {
        public String name;
        public String nbt;
    }


    @Override
    public String toString() {
        return "TabEditJsonRepresentation{" +
                "\ntab_icon=" + tab_icon +
                ",\n tab_name_key='" + tab_name_key + '\'' +
                ",\n items_to_add=" + items_to_add +
                ",\n items_to_remove=" + items_to_remove +
                '}';
    }
}
