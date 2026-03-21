package consulo.cmake.setting;

import consulo.externalSystem.setting.ExternalProjectSettings;

import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 */
public class CMakeProjectSettings extends ExternalProjectSettings {
    @Nullable
    private String myBuildDirectory;

    @Nullable
    public String getBuildDirectory() {
        return myBuildDirectory;
    }

    public void setBuildDirectory(@Nullable String buildDirectory) {
        myBuildDirectory = buildDirectory;
    }

    @Override
    public CMakeProjectSettings clone() {
        CMakeProjectSettings clone = new CMakeProjectSettings();
        copyTo(clone);
        clone.myBuildDirectory = myBuildDirectory;
        return clone;
    }
}
