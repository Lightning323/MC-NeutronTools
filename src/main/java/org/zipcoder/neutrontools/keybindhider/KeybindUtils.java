package org.zipcoder.neutrontools.keybindhider;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.zipcoder.neutrontools.NeutronTools;

import java.util.ArrayList;
import java.util.List;

public class KeybindUtils {

    public static KeyMapping[] getVisibleKeyBindings() {
        KeyMapping[] keyMappings = Minecraft.getInstance().options.keyMappings;
        ArrayList<KeyMapping> visibleKeyBindings = new ArrayList<>();
        for (KeyMapping keyMapping : keyMappings) {
            String category = keyMapping.getCategory();
            String keyName = keyMapping.getName();
            if (NeutronTools.CONFIG.keybindConsoleLogs) {
                NeutronTools.LOGGER.info("Checking keybind \"{}\" in category \"{}\".", keyName, category);
            }
            if (isKeybindHidden(keyMapping)) {
                if (!NeutronTools.CONFIG.keybindConsoleLogs) continue;
                NeutronTools.LOGGER.warn("Hiding keybind \"{}\" in category \"{}\".", keyName, category);
                continue;
            }
            visibleKeyBindings.add(keyMapping);
        }
        KeyMapping.resetMapping();
        return visibleKeyBindings.toArray(new KeyMapping[0]);
    }

    public static boolean isKeybindHidden(KeyMapping keyMapping) {
        return NeutronTools.CONFIG.hiddenKeybindCategories.contains(keyMapping.getCategory())
                || NeutronTools.CONFIG.hiddenKeybinds.contains(keyMapping.getName());
    }

    public static List<KeyMapping> getHiddenKeyBindings() {
        KeyMapping[] keyMappings = Minecraft.getInstance().options.keyMappings;
        ArrayList<KeyMapping> hiddenKeyBindings = new ArrayList<>();
        for (KeyMapping keyMapping : keyMappings) {
            if (!isKeybindHidden(keyMapping)) continue;
            hiddenKeyBindings.add(keyMapping);
        }
        return hiddenKeyBindings;
    }

    public static void disableHiddenKeyBindings() {
        for (KeyMapping keyMapping : Minecraft.getInstance().options.keyMappings) {
            if (isKeybindHidden(keyMapping)) {
                keyMapping.setKey(InputConstants.UNKNOWN);
            }
        }
        KeyMapping.resetMapping();
    }
}