package org.lightning.neutrontools.packs;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.neoforged.fml.loading.FMLPaths;
import org.lightning.neutrontools.NeutronTools;

import java.nio.file.Files;
import java.nio.file.Path;

public class GlobalPackHandler {
    // Allows all paths/symlinks inside the directory without tripping errors
    private static final DirectoryValidator NO_OP_VALIDATOR = new DirectoryValidator((path) -> false);

    /**
     * Creates a fresh RepositorySource instance targeting our folder.
     */
    public static FolderRepositorySource createRepositorySource(PackType type, Path packDirectory) {
        if (!packDirectory.toFile().exists()) packDirectory.toFile().mkdirs();
        return new FolderRepositorySource(
                packDirectory,
                type,
                PackSource.BUILT_IN,
                NO_OP_VALIDATOR
        );
    }

    public static ForcedFolderRepositorySource createForcedRepositorySource(PackType type, Path packDirectory) {
        if (!packDirectory.toFile().exists()) packDirectory.toFile().mkdirs();
        return new ForcedFolderRepositorySource(
                packDirectory,
                type,
                PackSource.BUILT_IN,
                NO_OP_VALIDATOR
        );
    }
}