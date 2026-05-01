package consulo.cmake.importProvider;

import consulo.annotation.component.ExtensionImpl;
import consulo.cmake.externalSystem.impl.CMakeConstants;
import consulo.cmake.icon.CMakeIconGroup;
import consulo.externalSystem.importing.AbstractExternalModuleImportProvider;
import consulo.externalSystem.importing.ExternalModuleImportContext;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.service.project.ProjectData;
import consulo.externalSystem.service.project.manage.ProjectDataManager;
import consulo.project.Project;
import consulo.ui.image.Image;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;

import java.io.File;

/**
 * @author VISTALL
 */
@ExtensionImpl
public class CMakeModuleImportProvider extends AbstractExternalModuleImportProvider<ImportFromCMakeControl> {
    @Inject
    public CMakeModuleImportProvider(@Nonnull ProjectDataManager dataManager) {
        super(dataManager, new ImportFromCMakeControl(), CMakeConstants.SYSTEM_ID);
    }

    @Nullable
    @Override
    public Image getIcon() {
        return CMakeIconGroup.cmake();
    }

    @Override
    public boolean canImport(@Nonnull File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            return new File(fileOrDirectory, CMakeConstants.CMAKE_LISTS_TXT).exists();
        }
        return CMakeConstants.CMAKE_LISTS_TXT.equals(fileOrDirectory.getName());
    }

    @Override
    protected void doPrepare(@Nonnull ExternalModuleImportContext<ImportFromCMakeControl> context) {
    }

    @Override
    protected void beforeCommit(@Nonnull DataNode<ProjectData> dataNode, @Nonnull Project project) {
    }

    @Nonnull
    @Override
    protected File getExternalProjectConfigToUse(@Nonnull File file) {
        if (file.isDirectory()) {
            File cmakeLists = new File(file, CMakeConstants.CMAKE_LISTS_TXT);
            if (cmakeLists.exists()) {
                return cmakeLists;
            }
        }
        return file;
    }

    @Override
    protected void applyExtraSettings(@Nonnull ExternalModuleImportContext<ImportFromCMakeControl> context) {
    }
}
