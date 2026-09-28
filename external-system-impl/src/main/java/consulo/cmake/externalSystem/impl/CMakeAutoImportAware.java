package consulo.cmake.externalSystem.impl;

import consulo.cmake.service.project.CMakeFileApi;
import consulo.cmake.service.project.CMakeProjectPaths;
import consulo.cmake.setting.CMakeProjectSettings;
import consulo.cmake.setting.CMakeSettings;
import consulo.externalSystem.service.project.autoimport.ExternalSystemAutoImportAware;
import consulo.project.Project;
import consulo.util.io.FileUtil;
import consulo.util.io.PathUtil;
import jakarta.annotation.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author VISTALL
 */
public class CMakeAutoImportAware implements ExternalSystemAutoImportAware {
    private static final String CMAKE_SCRIPT_EXTENSION = ".cmake";

    @Nullable
    @Override
    public String getAffectedExternalProjectPath(String changedFileOrDirPath, Project project) {
        String fileName = PathUtil.getFileName(changedFileOrDirPath);
        if (!CMakeConstants.CMAKE_LISTS_TXT.equals(fileName) && !fileName.endsWith(CMAKE_SCRIPT_EXTENSION)) {
            return null;
        }

        File file = new File(changedFileOrDirPath);
        if (file.isDirectory()) {
            return null;
        }

        Map<File, CMakeProjectSettings> roots = new HashMap<>();
        for (CMakeProjectSettings settings : CMakeSettings.getInstance(project).getLinkedProjectsSettings()) {
            String externalProjectPath = settings.getExternalProjectPath();
            if (externalProjectPath == null) {
                continue;
            }
            roots.put(CMakeProjectPaths.getSourceDir(externalProjectPath).getAbsoluteFile(), settings);
        }

        for (File dir = file.getAbsoluteFile().getParentFile(); dir != null; dir = dir.getParentFile()) {
            CMakeProjectSettings settings = roots.get(dir);
            if (settings == null) {
                continue;
            }
            File buildDir = CMakeProjectPaths.getBuildDir(dir, settings.getBuildDirectory());
            if (FileUtil.isAncestor(buildDir, file, false)) {
                return null;
            }
            return settings.getExternalProjectPath();
        }
        return null;
    }

    @Override
    public List<Path> getAffectedExternalProjectFilePaths(String projectPath, Project project) {
        CMakeProjectSettings settings = CMakeSettings.getInstance(project).getLinkedProjectSettings(projectPath);
        if (settings == null) {
            return List.of();
        }

        File sourceDir = CMakeProjectPaths.getSourceDir(projectPath);
        File buildDir = CMakeProjectPaths.getBuildDir(sourceDir, settings.getBuildDirectory());

        Set<Path> result = new LinkedHashSet<>();
        result.add(sourceDir.toPath().resolve(CMakeConstants.CMAKE_LISTS_TXT).normalize());
        result.addAll(CMakeFileApi.readInputFiles(sourceDir, buildDir));
        return new ArrayList<>(result);
    }
}
