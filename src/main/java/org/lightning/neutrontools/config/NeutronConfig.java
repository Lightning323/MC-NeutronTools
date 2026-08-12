package org.lightning.neutrontools.config;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import org.lightning.neutrontools.NeutronTools;

import java.io.File;
import java.nio.file.Files;
import java.time.ZoneOffset;

import static org.lightning.neutrontools.NeutronTools.BASE_CONFIG_DIRECTORY;

public class NeutronConfig {

    public static final File DISABLED_TABS_FILE = new File(BASE_CONFIG_DIRECTORY.toFile(), "disabled_tabs.json");
    public static final File DISABLED_ITEMS_FILE = new File(BASE_CONFIG_DIRECTORY.toFile(), "disabled_items.json");
    public static final File ORDERED_TABS_FILE = new File(BASE_CONFIG_DIRECTORY.toFile(), "ordered_tabs.json");
    public static final File NEW_TABS_DIRECTORY = new File(BASE_CONFIG_DIRECTORY.toFile(), "new_tabs");
    public static final File TAB_EDITS_DIRECTORY = new File(BASE_CONFIG_DIRECTORY.toFile(), "tab_edits");


    public static void plantStarterFiles() {
        NeutronTools.BASE_CONFIG_DIRECTORY.toFile().mkdirs();
        try {
            if (!DISABLED_TABS_FILE.exists()) Files.writeString(DISABLED_TABS_FILE.toPath(), "{\n\"disabled_tabs\":[]\n}");
            if (!ORDERED_TABS_FILE.exists()) Files.writeString(ORDERED_TABS_FILE.toPath(), "{\n\"ordered_tabs\":[]\n}");

            NEW_TABS_DIRECTORY.mkdirs();
            TAB_EDITS_DIRECTORY.mkdirs();
        } catch (Exception e) {
            NeutronTools.LOG.error("Failed to plant starter files", e);
        }
    }

    public NeutronConfig() {
        plantStarterFiles();
        try {
            File configFile = new File(NeutronTools.BASE_CONFIG_DIRECTORY.toFile(), "neutron-tools-config.toml");
            try (FileConfig config = FileConfig.builder(configFile, TomlFormat.instance()).build()) {
                if (configFile.exists()) {
                    writeReadConfig(config, true);
                } else {
                    writeReadConfig(config, false);
                }
            }
        } catch (Exception e) {
            NeutronTools.LOG.error("An error occurred initializing pre-init config!", e);
        }
    }

    /**
     * Default values go here
     * Only boolean, int, double and string are supported
     * NO FLOATS ALLOWED!
     */
    //--------------------------------------------------------------------
    public boolean crashCommands = false;
    public float hungerMultiplier = 1.0f;//Casting from double to float
    public boolean hideCreativeTabItemsFromJEIBlacklist = true;
    public boolean disableExperementalSettings = true;
    public boolean hideRecipeToasts = true;
    public boolean hideTutorialToasts = false;
    public boolean verboseMode = false;
    public boolean loadFromBaseDatapacksDirectory = true;
    public boolean allowSimulatedCreativeTabEdits = false;
    //--------------------------------------------------------------------

    private void writeReadConfig(FileConfig config, boolean isReading) {
        if (isReading) config.load();

        //Common
        crashCommands = getAndSet(config, isReading, "common.crash_commands", crashCommands);
        hungerMultiplier = (float) (double) getAndSet(config, isReading, "common.hunger_multiplier", (double) hungerMultiplier);
        disableExperementalSettings = getAndSet(config, isReading, "common.disable_experemental_settings_popup", disableExperementalSettings);
        verboseMode = getAndSet(config, isReading, "common.verbose_mode", verboseMode);
        loadFromBaseDatapacksDirectory = getAndSet(config, isReading, "common.loadBaseDatapackDirectory", loadFromBaseDatapacksDirectory);

        // Client
        hideCreativeTabItemsFromJEIBlacklist = getAndSet(config, isReading, "client.hide_creative_tab_items_from_jei_blacklist", hideCreativeTabItemsFromJEIBlacklist);
        hideRecipeToasts = getAndSet(config, isReading, "client.hide_recipe_toasts", hideRecipeToasts);
        hideTutorialToasts = getAndSet(config, isReading, "client.hide_tutorial_toasts", hideTutorialToasts);
        allowSimulatedCreativeTabEdits = getAndSet(config, isReading, "client.allow_simulated_creative_tab_edits", allowSimulatedCreativeTabEdits);

        if (!isReading) config.save();
    }

    private <T> T getAndSet(FileConfig config, boolean isReading, String key, T value) {
        if (isReading) {
            return config.getOrElse(key, value);
        } else {
            config.set(key, value);
            return value;
        }
    }
}
