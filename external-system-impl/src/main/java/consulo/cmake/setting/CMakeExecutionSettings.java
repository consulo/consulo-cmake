package consulo.cmake.setting;

import consulo.externalSystem.model.setting.ExternalSystemExecutionSettings;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 */
public class CMakeExecutionSettings extends ExternalSystemExecutionSettings {
    @Nonnull
    private final String mySourceDirectory;
    @Nullable
    private final String myBuildDirectory;

    public CMakeExecutionSettings(@Nonnull String sourceDirectory, @Nullable String buildDirectory) {
        mySourceDirectory = sourceDirectory;
        myBuildDirectory = buildDirectory;
    }

    @Nonnull
    public String getSourceDirectory() {
        return mySourceDirectory;
    }

    @Nullable
    public String getBuildDirectory() {
        return myBuildDirectory;
    }
}
