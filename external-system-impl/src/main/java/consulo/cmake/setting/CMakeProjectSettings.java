package consulo.cmake.setting;

import consulo.externalSystem.setting.ExternalProjectSettings;

import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 */
public class CMakeProjectSettings extends ExternalProjectSettings {
    public static final String DEFAULT_BUILD_TYPE = "Debug";

    @Nullable
    private String myBuildDirectory;
    @Nullable
    private String myBuildType;

    @Nullable
    public String getBuildDirectory() {
        return myBuildDirectory;
    }

    public void setBuildDirectory(@Nullable String buildDirectory) {
        myBuildDirectory = buildDirectory;
    }

    @Nullable
    public String getBuildType() {
        return myBuildType;
    }

    public void setBuildType(@Nullable String buildType) {
        myBuildType = buildType;
    }

    public String getBuildTypeOrDefault() {
        String buildType = myBuildType;
        return buildType == null || buildType.isBlank() ? DEFAULT_BUILD_TYPE : buildType;
    }

    @Override
    public CMakeProjectSettings clone() {
        CMakeProjectSettings clone = new CMakeProjectSettings();
        copyTo(clone);
        clone.myBuildDirectory = myBuildDirectory;
        clone.myBuildType = myBuildType;
        return clone;
    }
}
