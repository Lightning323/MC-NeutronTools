package org.lightning.neutrontools.packs;

import net.minecraft.FileUtil;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.level.validation.DirectoryValidator;
import org.lightning.neutrontools.NeutronTools;
import org.lightning.neutrontools.mixin.FolderRepositorySourceAccessor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

public class ForcedFolderRepositorySource extends FolderRepositorySource {
    
    private final PackType type;

    // Forces the pack to be active and un-removable
    private static final PackSelectionConfig FORCED_CONFIG = new PackSelectionConfig(
        true,             // required = true (FORCED)
        Pack.Position.TOP, // Default position
        true              // fixedPosition = true (Locked position)
    );

    public ForcedFolderRepositorySource(Path folder, PackType packType, PackSource packSource, DirectoryValidator validator) {
        super(folder, packType, packSource, validator);
        this.type = packType;
    }

    @Override
    public void loadPacks(Consumer<Pack> consumer) {
        FolderRepositorySourceAccessor self = (FolderRepositorySourceAccessor) this;
        try {

            FileUtil.createDirectoriesSafe(self.getFolder());
            // Re-use vanilla's discoverPacks method from your decompiled class
            discoverPacks(self.getFolder(), self.getValidator(), (path, resourcesSupplier) -> {
                // Generate the Location Info wrapper
                String name = path.getFileName().toString();
                PackLocationInfo locationInfo = new PackLocationInfo(
                    "file/" + name, 
                    net.minecraft.network.chat.Component.literal(name),
                    self.getPackSource(),
                    java.util.Optional.empty()
                );
                
                // Read the meta but pass our FORCED_CONFIG instead of vanilla's default
                Pack pack = Pack.readMetaAndCreate(locationInfo, resourcesSupplier, self.getPackType(), FORCED_CONFIG);
                if (pack != null) {
                    consumer.accept(pack);
                }
            });
        } catch (IOException e) {
            NeutronTools.LOG.warn("Failed to list forced packs in {}", self.getFolder(), e);
        }
    }
}