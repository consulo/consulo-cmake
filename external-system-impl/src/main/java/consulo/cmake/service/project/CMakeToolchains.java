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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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

    /**
     * @param compilerTarget the target triple CMake reports - only for a compiler told its target, a cross compiling Clang
     */
    private record Toolchain(@Nullable String compilerId,
                             @Nullable String compilerPath,
                             @Nullable String compilerTarget,
                             List<String> implicitIncludeDirectories) {
    }

    /**
     * Compilers which report the target they compile for with {@code -dumpmachine}.
     */
    private static final Set<String> DUMP_MACHINE_COMPILERS = Set.of("GNU", "Clang", "AppleClang");

    private final Map<String, Toolchain> myToolchains;
    private final Map<String, Optional<String>> myAskedTargets = new ConcurrentHashMap<>();

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
            result.put(language, new Toolchain(CMakeFileApi.getString(compiler, "id"),
                CMakeFileApi.getString(compiler, "path"),
                CMakeFileApi.getString(compiler, "target"),
                List.copyOf(includeDirectories)));
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
     * The target triple the compiler of {@code language} compiles for - {@code x86_64-pc-linux-gnu}, {@code arm-none-eabi}:
     * the one CMake reports, else the one GCC or Clang answers to {@code -dumpmachine}; {@code null} when neither knows.
     */
    @Nullable
    String getTarget(String language) {
        Toolchain toolchain = myToolchains.get(language);
        if (toolchain == null) {
            return null;
        }
        if (toolchain.compilerTarget() != null) {
            return toolchain.compilerTarget();
        }
        String path = toolchain.compilerPath();
        // CMake identifies no compiler for some languages - ASM of a GCC build has none
        String compilerId = toolchain.compilerId();
        if (path == null || compilerId == null || !DUMP_MACHINE_COMPILERS.contains(compilerId)) {
            return null;
        }
        return myAskedTargets.computeIfAbsent(path, CMakeToolchains::dumpMachine).orElse(null);
    }

    private static Optional<String> dumpMachine(String compilerPath) {
        try {
            Process process = new ProcessBuilder(compilerPath, "-dumpmachine").redirectErrorStream(true).start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            if (!process.waitFor(10, TimeUnit.SECONDS) || process.exitValue() != 0 || output.isEmpty() || output.contains(" ")) {
                process.destroy();
                return Optional.empty();
            }
            return Optional.of(output);
        }
        catch (IOException e) {
            return Optional.empty();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    /**
     * Whether {@code language} is compiled by MSVC, whose options start with a slash.
     */
    boolean isMsvc(String language) {
        Toolchain toolchain = myToolchains.get(language);
        return toolchain != null && "MSVC".equals(toolchain.compilerId());
    }
}
