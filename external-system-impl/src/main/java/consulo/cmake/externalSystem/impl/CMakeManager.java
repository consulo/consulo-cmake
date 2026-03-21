package consulo.cmake.externalSystem.impl;

import consulo.annotation.component.ExtensionImpl;
import consulo.cmake.service.project.CMakeProjectResolver;
import consulo.cmake.service.task.CMakeTaskManager;
import consulo.cmake.setting.*;
import consulo.externalSystem.ExternalSystemManager;
import consulo.externalSystem.model.ProjectSystemId;
import consulo.externalSystem.service.project.ExternalSystemProjectResolver;
import consulo.externalSystem.task.ExternalSystemTaskManager;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.project.Project;
import consulo.util.lang.Pair;

import jakarta.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author VISTALL
 */
@ExtensionImpl
public class CMakeManager implements ExternalSystemManager<CMakeProjectSettings, CMakeSettingsListener, CMakeSettings, CMakeLocalSettings, CMakeExecutionSettings> {
    @Nonnull
    @Override
    public ProjectSystemId getSystemId() {
        return CMakeConstants.SYSTEM_ID;
    }

    @Nonnull
    @Override
    public Function<Project, CMakeSettings> getSettingsProvider() {
        return CMakeSettings::getInstance;
    }

    @Nonnull
    @Override
    public Function<Project, CMakeLocalSettings> getLocalSettingsProvider() {
        return CMakeLocalSettings::getInstance;
    }

    @Nonnull
    @Override
    public Function<Pair<Project, String>, CMakeExecutionSettings> getExecutionSettingsProvider() {
        return pair -> {
            Project project = pair.getFirst();
            String projectPath = pair.getSecond();
            CMakeSettings settings = CMakeSettings.getInstance(project);
            CMakeProjectSettings projectSettings = settings.getLinkedProjectSettings(projectPath);
            String buildDir = projectSettings != null ? projectSettings.getBuildDirectory() : null;
            return new CMakeExecutionSettings(projectPath, buildDir);
        };
    }

    @Nonnull
    @Override
    public Supplier<? extends ExternalSystemProjectResolver<CMakeExecutionSettings>> getProjectResolverFactory() {
        return CMakeProjectResolver::new;
    }

    @Nonnull
    @Override
    public Supplier<? extends ExternalSystemTaskManager<CMakeExecutionSettings>> getTaskManagerFactory() {
        return CMakeTaskManager::new;
    }

    @Nonnull
    @Override
    public FileChooserDescriptor getExternalProjectDescriptor() {
        // chooseFiles=true, chooseFolders=true - accept both CMakeLists.txt and directories
        return new FileChooserDescriptor(true, true, false, false, false, false) {
            @Override
            public boolean isFileSelectable(consulo.virtualFileSystem.VirtualFile file) {
                if (file.isDirectory()) {
                    return true;
                }
                return CMakeConstants.CMAKE_LISTS_TXT.equals(file.getName());
            }
        };
    }
}
