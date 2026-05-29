package org.lightning.neutrontools.mixin;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.level.validation.DirectoryValidator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.file.Path;

@Mixin(FolderRepositorySource.class)
public interface FolderRepositorySourceAccessor {
    @Accessor
    PackType getPackType();

    @Accessor
    Path getFolder();

    @Accessor
    PackSource getPackSource();

    @Accessor
    DirectoryValidator getValidator();
}
