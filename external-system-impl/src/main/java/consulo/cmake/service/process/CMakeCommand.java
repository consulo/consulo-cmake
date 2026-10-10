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
package consulo.cmake.service.process;

import consulo.cmake.externalSystem.impl.CMakeConstants;
import consulo.process.cmd.GeneralCommandLine;
import jakarta.annotation.Nullable;

import java.io.File;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
public final class CMakeCommand {
    public static final String BUILD_TASK = "build";

    private CMakeCommand() {
    }

    public static GeneralCommandLine configure(File sourceDirectory, File buildDirectory, String buildType) {
        return new GeneralCommandLine(CMakeConstants.CMAKE_EXECUTABLE)
            .withParameters("-S", sourceDirectory.getAbsolutePath(),
                "-B", buildDirectory.getAbsolutePath(),
                "-DCMAKE_BUILD_TYPE=" + buildType)
            .withWorkDirectory(sourceDirectory.getAbsolutePath());
    }

    public static GeneralCommandLine build(File buildDirectory, String buildType, @Nullable String target, boolean cleanFirst) {
        GeneralCommandLine commandLine = new GeneralCommandLine(CMakeConstants.CMAKE_EXECUTABLE)
            .withParameters("--build", buildDirectory.getAbsolutePath(), "--config", buildType);
        if (target != null) {
            commandLine.addParameters("--target", target);
        }
        if (cleanFirst) {
            commandLine.addParameter("--clean-first");
        }
        return commandLine;
    }

    @Nullable
    public static String targetOfTask(String taskName) {
        return BUILD_TASK.equals(taskName) ? null : taskName;
    }
}
