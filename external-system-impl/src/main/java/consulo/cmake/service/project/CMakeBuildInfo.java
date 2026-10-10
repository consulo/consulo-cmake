/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package consulo.cmake.service.project;

import consulo.cmake.setting.CMakeProjectSettings;
import consulo.cmake.setting.CMakeSettings;
import consulo.project.Project;

import java.io.File;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
public record CMakeBuildInfo(File sourceDirectory, File buildDirectory, String buildType) {
    public static CMakeBuildInfo of(Project project, String linkedProjectPath) {
        CMakeProjectSettings settings = CMakeSettings.getInstance(project).getLinkedProjectSettings(linkedProjectPath);
        File sourceDirectory = CMakeProjectPaths.getSourceDir(linkedProjectPath);
        File buildDirectory = CMakeProjectPaths.getBuildDir(sourceDirectory, settings != null ? settings.getBuildDirectory() : null);
        String buildType = settings != null ? settings.getBuildTypeOrDefault() : CMakeProjectSettings.DEFAULT_BUILD_TYPE;
        return new CMakeBuildInfo(sourceDirectory, buildDirectory, buildType);
    }

    public boolean isConfigured() {
        return new File(buildDirectory, "CMakeCache.txt").isFile();
    }
}
