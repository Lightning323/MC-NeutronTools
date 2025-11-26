package org.zipcoder.neutrontools.config;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.nio.file.Path;

import static org.zipcoder.neutrontools.NeutronTools.LOGGER;

public class PreInitConfig {


    public PreInitConfig() {
        try {
            Path path = FMLPaths.CONFIGDIR.get();
            File configFile = new File(path.toFile(), "neutron-tools-config.toml");
            try (FileConfig config = FileConfig.builder(configFile, TomlFormat.instance()).build()) {
                if (configFile.exists()) {
                    loadConfig(config);
                } else {
                    writeConfig(config);
                }
            }
        } catch (Exception e) {
            LOGGER.error("An error occurred initializing pre-init config!", e);
        }
    }

    /**
     * Default values go here
     * Only boolean, int, double and string are supported
     * NO FLOATS ALLOWED!
     */
    //--------------------------------------------------------------------
    public int portalWaitTime = 80;
    public boolean crashCommands = false;
    public boolean setHungerMultiplier = false;
    public int hungerMultiplier = 1;
    //--------------------------------------------------------------------

    /**
     * Write a new config
     */
    private void writeConfig(FileConfig config) {
        //--------------------------------------------------------------------
        config.set("common.crash_commands", crashCommands);
        config.set("common.portal_wait_time", portalWaitTime);
        config.set("common.set_default_hunger_multiplier",setHungerMultiplier);
        config.set("common.hunger_multiplier",hungerMultiplier);
        //--------------------------------------------------------------------
        config.save();
    }

    /**
     * Load the config
     * NOTE that doubles in the config MUST have .0 at the end otherwise it will be read as an int
     */
    private void loadConfig(FileConfig config) {
        config.load();
        //--------------------------------------------------------------------
        crashCommands = config.getOrElse("common.crash_commands", crashCommands);
        portalWaitTime = config.getOrElse("common.portal_wait_time", portalWaitTime);
        setHungerMultiplier = config.getOrElse("common.set_default_hunger_multiplier", setHungerMultiplier);
        hungerMultiplier = config.getOrElse("common.hunger_multiplier", hungerMultiplier);
        //--------------------------------------------------------------------
    }
}
