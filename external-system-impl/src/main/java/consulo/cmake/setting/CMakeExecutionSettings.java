package consulo.cmake.setting;

import consulo.externalSystem.model.setting.ExternalSystemExecutionSettings;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Objects;

/**
 * @author VISTALL
 */
public class CMakeExecutionSettings extends ExternalSystemExecutionSettings {
    @Nonnull
    private final String mySourceDirectory;
    @Nullable
    private final String myBuildDirectory;
    @Nonnull
    private final String myBuildType;

    public CMakeExecutionSettings(@Nonnull String sourceDirectory, @Nullable String buildDirectory, @Nonnull String buildType) {
        mySourceDirectory = sourceDirectory;
        myBuildDirectory = buildDirectory;
        myBuildType = buildType;
    }

    @Nonnull
    public String getSourceDirectory() {
        return mySourceDirectory;
    }

    @Nullable
    public String getBuildDirectory() {
        return myBuildDirectory;
    }

    @Nonnull
    public String getBuildType() {
        return myBuildType;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + mySourceDirectory.hashCode();
        result = 31 * result + (myBuildDirectory != null ? myBuildDirectory.hashCode() : 0);
        result = 31 * result + myBuildType.hashCode();
        return result;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!super.equals(o)) {
            return false;
        }
        CMakeExecutionSettings that = (CMakeExecutionSettings) o;
        return mySourceDirectory.equals(that.mySourceDirectory)
            && Objects.equals(myBuildDirectory, that.myBuildDirectory)
            && myBuildType.equals(that.myBuildType);
    }
}
