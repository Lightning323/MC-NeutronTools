package org.lightning.neutrontools.config;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import org.lightning.neutrontools.NeutronTools;
import org.lightning.neutrontools.config.creativeTabs.NeutronCreativeTabConfig;

import java.io.File;
import java.nio.file.Files;

import static org.lightning.neutrontools.NeutronTools.CONFIG_PATH;

public class NeutronConfig {

    public static void plantStarterFiles() {
        try {
            Files.writeString(new File(CONFIG_PATH, "disabled_tabs.json").toPath(),
                    "{\n\"disabled_tabs\":[]\n}");
            Files.writeString(new File(CONFIG_PATH, "disabled_items.json").toPath(),
                    "{\n\"disabled_items\":[]\n}");
            Files.writeString(new File(CONFIG_PATH, "ordered_tabs.json").toPath(),
                    "{\n\"ordered_tabs\":[]\n}");
            new File(CONFIG_PATH, "new_tabs").mkdirs();
            new File(CONFIG_PATH, "tab_edits").mkdirs();
        } catch (Exception e) {
            NeutronTools.LOGGER.error("Failed to plant starter files", e);
        }
    }

    public NeutronConfig() {
        try {
            if (!NeutronTools.CONFIG_PATH.exists()) {
                NeutronTools.LOGGER.info("Config Dir: {}", NeutronTools.CONFIG_PATH);
                NeutronTools.CONFIG_PATH.mkdirs();
                plantStarterFiles();
            }
            File configFile = new File(NeutronTools.CONFIG_PATH, "neutron-tools-config.toml");
            try (FileConfig config = FileConfig.builder(configFile, TomlFormat.instance()).build()) {
                if (configFile.exists()) {
                    writeReadConfig(config, true);
                } else {
                    writeReadConfig(config, false);
                }
            }
        } catch (Exception e) {
            NeutronTools.LOGGER.error("An error occurred initializing pre-init config!", e);
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
    //--------------------------------------------------------------------

    private void writeReadConfig(FileConfig config, boolean isReading) {
        if (isReading) config.load();

        //Common
        crashCommands = getAndSet(config, isReading, "common.crash_commands", crashCommands);
        hungerMultiplier = (float) (double) getAndSet(config, isReading, "common.hunger_multiplier", (double) hungerMultiplier);
        disableExperementalSettings = getAndSet(config, isReading, "common.disable_experemental_settings_popup", disableExperementalSettings);
        verboseMode = getAndSet(config, isReading, "common.verbose_mode", verboseMode);

        // Client
        hideCreativeTabItemsFromJEIBlacklist = getAndSet(config, isReading, "client.hide_creative_tab_items_from_jei_blacklist", hideCreativeTabItemsFromJEIBlacklist);
        hideRecipeToasts = getAndSet(config, isReading, "client.hide_recipe_toasts", hideRecipeToasts);
        hideTutorialToasts = getAndSet(config, isReading, "client.hide_tutorial_toasts", hideTutorialToasts);

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
