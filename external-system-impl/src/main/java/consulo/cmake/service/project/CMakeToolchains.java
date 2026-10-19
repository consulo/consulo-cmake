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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The compilers CMake configured, from the {@code toolchains} object of its file API: per language, which compiler,
 * and the include directories it searches on its own - where the standard library headers are, which no compile
 * command names.
 *
 * @author VISTALL
 * @since 2026-10-19
 */
final class CMakeToolchains {
    static final CMakeToolchains EMPTY = new CMakeToolchains(Map.of());

    private record Toolchain(@Nullable String compilerId, List<String> implicitIncludeDirectories) {
    }

    private final Map<String, Toolchain> myToolchains;

    private CMakeToolchains(Map<String, Toolchain> toolchains) {
        myToolchains = toolchains;
    }

    static CMakeToolchains read(@Nullable JsonObject reply) {
        JsonArray toolchains = reply == null ? null : reply.getAsJsonArray("toolchains");
        if (toolchains == null) {
            return EMPTY;
        }

        Map<String, Toolchain> result = new HashMap<>();
        for (JsonElement element : toolchains) {
            JsonObject toolchain = element.getAsJsonObject();
            String language = CMakeFileApi.getString(toolchain, "language");
            JsonObject compiler = toolchain.getAsJsonObject("compiler");
            if (language == null || compiler == null) {
                continue;
            }

            List<String> includeDirectories = new ArrayList<>();
            JsonObject implicit = compiler.getAsJsonObject("implicit");
            JsonArray directories = implicit == null ? null : implicit.getAsJsonArray("includeDirectories");
            if (directories != null) {
                for (JsonElement directory : directories) {
                    includeDirectories.add(directory.getAsString());
                }
            }
            result.put(language, new Toolchain(CMakeFileApi.getString(compiler, "id"), List.copyOf(includeDirectories)));
        }
        return new CMakeToolchains(result);
    }

    /**
     * The include directories the compiler of {@code language} searches without being told, in its search order.
     */
    List<String> getImplicitIncludeDirectories(String language) {
        Toolchain toolchain = myToolchains.get(language);
        return toolchain == null ? List.of() : toolchain.implicitIncludeDirectories();
    }

    /**
     * Whether {@code language} is compiled by MSVC, whose options start with a slash.
     */
    boolean isMsvc(String language) {
        Toolchain toolchain = myToolchains.get(language);
        return toolchain != null && "MSVC".equals(toolchain.compilerId());
    }
}
