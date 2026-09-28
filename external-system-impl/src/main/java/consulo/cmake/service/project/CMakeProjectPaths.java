package consulo.cmake.service.project;

import jakarta.annotation.Nullable;

import java.io.File;

/**
 * @author VISTALL
 */
public final class CMakeProjectPaths {
    private CMakeProjectPaths() {
    }

    public static File getSourceDir(String projectPath) {
        File sourceDir = new File(projectPath);
        if (sourceDir.isFile()) {
            sourceDir = sourceDir.getParentFile();
        }
        return sourceDir;
    }

    public static File getBuildDir(File sourceDir, @Nullable String buildDirectory) {
        if (buildDirectory == null || buildDirectory.isBlank()) {
            return new File(sourceDir.getAbsolutePath() + File.separator + "build");
        }
        return new File(buildDirectory);
    }
}
