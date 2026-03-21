package consulo.cmake.module.extension;

import consulo.annotation.component.ExtensionImpl;
import consulo.cmake.externalSystem.impl.CMakeConstants;
import consulo.cmake.icon.CMakeIconGroup;
import consulo.cmake.localize.CMakeLocalize;
import consulo.externalSystem.service.module.extension.ExternalSystemModuleExtensionImpl;
import consulo.externalSystem.service.module.extension.ExternalSystemMutableModuleExtensionImpl;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;
import consulo.ui.image.Image;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 */
@ExtensionImpl
public class CMakeModuleExtensionProvider implements ModuleExtensionProvider<ExternalSystemModuleExtensionImpl> {
    @Nonnull
    @Override
    public String getId() {
        return "CMAKE";
    }

    @Nullable
    @Override
    public String getParentId() {
        return null;
    }

    @Override
    public boolean isSystemOnly() {
        return true;
    }

    @Nonnull
    @Override
    public LocalizeValue getName() {
        return CMakeLocalize.cmakeSystemName();
    }

    @Nonnull
    @Override
    public Image getIcon() {
        return CMakeIconGroup.cmake();
    }

    @Nonnull
    @Override
    public ModuleExtension<ExternalSystemModuleExtensionImpl> createImmutableExtension(@Nonnull ModuleRootLayer moduleRootLayer) {
        return new ExternalSystemModuleExtensionImpl(getId(), moduleRootLayer, CMakeConstants.SYSTEM_ID);
    }

    @Nonnull
    @Override
    public MutableModuleExtension<ExternalSystemModuleExtensionImpl> createMutableExtension(@Nonnull ModuleRootLayer moduleRootLayer) {
        return new ExternalSystemMutableModuleExtensionImpl(getId(), moduleRootLayer, CMakeConstants.SYSTEM_ID);
    }
}
