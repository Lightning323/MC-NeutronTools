package org.lightning.neutrontools.utils;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

import static org.lightning.neutrontools.NeutronTools.CONFIG_KEYBINDS;

public class KeybindUtils {

    public static KeyMapping[] getVisibleKeyBindings() {
        KeyMapping[] all = Minecraft.getInstance().options.keyMappings;
        List<KeyMapping> visible = new ArrayList<>();
        for (KeyMapping keyMapping : all) {
            if (!isKeybindHidden(keyMapping)) {
                visible.add(keyMapping);
            }
        }
        KeyMapping.resetMapping();
        return visible.toArray(new KeyMapping[0]);
    }

    public static boolean isKeybindHidden(KeyMapping keyMapping) {
        return CONFIG_KEYBINDS.hiddenCategories.contains(keyMapping.getCategory())
                || CONFIG_KEYBINDS.hiddenKeybinds.contains(keyMapping.getName());
    }

    public static List<KeyMapping> getHiddenKeyBindings() {
        List<KeyMapping> hidden = new ArrayList<>();
        for (KeyMapping keyMapping : Minecraft.getInstance().options.keyMappings) {
            if (isKeybindHidden(keyMapping)) {
                hidden.add(keyMapping);
            }
        }
        return hidden;
    }

    public static void unbindHiddenKeyBinds() {
        if (!CONFIG_KEYBINDS.disabledKeybinds) return;
        for (KeyMapping keyMapping : getHiddenKeyBindings()) {
            if (!keyMapping.isUnbound()) {
                keyMapping.setKey(InputConstants.UNKNOWN);
                KeyMapping.resetMapping();
            }
        }
    }
}