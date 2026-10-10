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
package consulo.cmake.module;

import consulo.annotation.access.RequiredReadAction;
import consulo.cmake.externalSystem.impl.CMakeConstants;
import consulo.externalSystem.service.module.extension.ExternalSystemModuleExtension;
import consulo.externalSystem.service.module.extension.ExternalSystemMutableModuleExtension;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import jakarta.annotation.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
public record CMakeTarget(String name, String type, @Nullable Path artifact, Path buildDirectory) {
    public static final String EXTENSION_ID = "CMAKE";
    public static final String EXECUTABLE = "EXECUTABLE";

    private static final String OPTION_TARGETS = "cmake.targets";
    private static final String OPTION_BUILD_DIRECTORY = "cmake.build.directory";
    private static final String OPTION_TARGET_PREFIX = "cmake.target.";
    private static final String TYPE_SUFFIX = ".type";
    private static final String ARTIFACT_SUFFIX = ".artifact";
    private static final String SEPARATOR = ";";

    public boolean isExecutable() {
        return EXECUTABLE.equals(type);
    }

    @RequiredReadAction
    public static List<CMakeTarget> list(Module module) {
        ExternalSystemModuleExtension<?> extension = findExtension(module);
        if (extension == null) {
            return List.of();
        }
        String names = extension.getOption(OPTION_TARGETS);
        String buildDirectory = extension.getOption(OPTION_BUILD_DIRECTORY);
        if (names == null || names.isEmpty() || buildDirectory == null) {
            return List.of();
        }
        List<CMakeTarget> targets = new ArrayList<>();
        for (String name : names.split(SEPARATOR)) {
            String type = extension.getOption(OPTION_TARGET_PREFIX + name + TYPE_SUFFIX);
            if (type == null) {
                continue;
            }
            String artifact = extension.getOption(OPTION_TARGET_PREFIX + name + ARTIFACT_SUFFIX);
            targets.add(new CMakeTarget(name, type, artifact == null ? null : Path.of(artifact), Path.of(buildDirectory)));
        }
        return targets;
    }

    @Nullable
    @RequiredReadAction
    public static CMakeTarget find(Module module, String name) {
        for (CMakeTarget target : list(module)) {
            if (target.name().equals(name)) {
                return target;
            }
        }
        return null;
    }

    public static void store(ExternalSystemMutableModuleExtension<?> extension, Collection<CMakeTarget> targets) {
        String previous = extension.getOption(OPTION_TARGETS);
        if (previous != null) {
            for (String name : previous.split(SEPARATOR)) {
                extension.removeOption(OPTION_TARGET_PREFIX + name + TYPE_SUFFIX);
                extension.removeOption(OPTION_TARGET_PREFIX + name + ARTIFACT_SUFFIX);
            }
        }
        extension.removeOption(OPTION_TARGETS);
        extension.removeOption(OPTION_BUILD_DIRECTORY);
        if (targets.isEmpty()) {
            return;
        }

        List<String> names = new ArrayList<>();
        for (CMakeTarget target : targets) {
            names.add(target.name());
            extension.setOption(OPTION_TARGET_PREFIX + target.name() + TYPE_SUFFIX, target.type());
            Path artifact = target.artifact();
            if (artifact != null) {
                extension.setOption(OPTION_TARGET_PREFIX + target.name() + ARTIFACT_SUFFIX, artifact.toString());
            }
            extension.setOption(OPTION_BUILD_DIRECTORY, target.buildDirectory().toString());
        }
        extension.setOption(OPTION_TARGETS, String.join(SEPARATOR, names));
    }

    @RequiredReadAction
    public static boolean isCMakeModule(Module module) {
        return findExtension(module) != null;
    }

    @Nullable
    private static ExternalSystemModuleExtension<?> findExtension(Module module) {
        if (module.isDisposed()) {
            return null;
        }
        ExternalSystemModuleExtension<?> extension = ModuleRootManager.getInstance(module).getExtension(EXTENSION_ID);
        if (extension == null || !CMakeConstants.SYSTEM_ID.equals(extension.getProjectSystemId())) {
            return null;
        }
        return extension;
    }
}
