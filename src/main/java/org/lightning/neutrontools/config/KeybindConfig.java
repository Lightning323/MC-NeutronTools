package org.lightning.neutrontools.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.lightning.neutrontools.NeutronTools;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.lightning.neutrontools.NeutronTools.BASE_CONFIG_DIRECTORY;

public class KeybindConfig {

    public static final File FILE = new File(BASE_CONFIG_DIRECTORY.toFile(), "keybinds.json");
    public static final String DEFAULT_JSON = "{\n" +
            "  \"hidden_categories\": [],\n" +
            "  \"hidden_keybinds\": [],\n" +
            "  \"disabled_keybinds\": false\n" +
            "}";

    public final List<String> hiddenCategories = new ArrayList<>();
    public final List<String> hiddenKeybinds = new ArrayList<>();
    public boolean disabledKeybinds = true;

    public KeybindConfig() {
        loadFromDisk();
    }

    public void loadFromDisk() {
        hiddenCategories.clear();
        hiddenKeybinds.clear();
        disabledKeybinds = false;

        if (!FILE.exists()) {
            plantStarterFile();
            return;
        }

        try (Reader reader = Files.newBufferedReader(FILE.toPath())) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            readStringArray(json, "hidden_categories", hiddenCategories);
            readStringArray(json, "hidden_keybinds", hiddenKeybinds);
            if (json.has("disabled_keybinds")) {
                disabledKeybinds = json.get("disabled_keybinds").getAsBoolean();
            }
        } catch (Exception e) {
            NeutronTools.LOG.warn("Failed to parse keybind config: {}", FILE, e);
        }
    }

    private void readStringArray(JsonObject json, String key, List<String> target) {
        if (!json.has(key) || !json.get(key).isJsonArray()) return;
        json.getAsJsonArray(key).forEach(element -> {
            if (element.isJsonPrimitive()) target.add(element.getAsString());
        });
    }

    private void plantStarterFile() {
        BASE_CONFIG_DIRECTORY.toFile().mkdirs();
        try {
            Files.writeString(FILE.toPath(), DEFAULT_JSON);
        } catch (IOException e) {
            NeutronTools.LOG.error("Failed to create keybind config: {}", FILE, e);
        }
    }
}