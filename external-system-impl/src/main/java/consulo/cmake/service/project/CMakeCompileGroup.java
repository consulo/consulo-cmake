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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * How the sources of one compile group of a target are compiled - everything an editor needs to preprocess them as the
 * compiler does: the include directories in search order, the defines and the language standard.
 * <p>
 * Read from the group's structured entries, its raw command fragments, and the implicit include directories of the
 * group's compiler.
 *
 * @param standard       the standard in CMake's notation - {@code 11}, {@code 17} - {@code null} when not set
 * @param compilerTarget the target triple the sources compile for - {@code x86_64-pc-linux-gnu} - {@code null} when
 *                       unknown
 * @author VISTALL
 * @since 2026-10-19
 */
record CMakeCompileGroup(String language,
                         List<String> includePaths,
                         List<String> systemIncludePaths,
                         Map<String, String> defines,
                         @Nullable String standard,
                         @Nullable String compilerTarget) {
    /**
     * Reads a compile group of a target object of the codemodel; {@code null} for a group without a language.
     */
    static @Nullable CMakeCompileGroup read(JsonObject compileGroup, CMakeToolchains toolchains) {
        String language = CMakeFileApi.getString(compileGroup, "language");
        if (language == null) {
            return null;
        }

        List<String> includePaths = new ArrayList<>();
        List<String> systemIncludePaths = new ArrayList<>();
        JsonArray includes = compileGroup.getAsJsonArray("includes");
        if (includes != null) {
            for (JsonElement element : includes) {
                JsonObject include = element.getAsJsonObject();
                String path = CMakeFileApi.getString(include, "path");
                if (path == null) {
                    continue;
                }
                JsonElement isSystem = include.get("isSystem");
                if (isSystem != null && isSystem.getAsBoolean()) {
                    systemIncludePaths.add(path);
                }
                else {
                    includePaths.add(path);
                }
            }
        }

        // what add_compile_options and CMAKE_<LANG>_FLAGS set comes only as raw command fragments
        CMakeCompileFlags flags = CMakeCompileFlags.parse(readCommandFragments(compileGroup), toolchains.isMsvc(language));
        includePaths.addAll(flags.includePaths());
        systemIncludePaths.addAll(flags.systemIncludePaths());

        // the compiler searches its own directories last
        for (String directory : toolchains.getImplicitIncludeDirectories(language)) {
            if (!includePaths.contains(directory) && !systemIncludePaths.contains(directory)) {
                systemIncludePaths.add(directory);
            }
        }

        // structured defines come first on the command line, the flags may change them
        Map<String, String> defines = readDefines(compileGroup);
        flags.applyDefines(defines);

        JsonObject languageStandard = compileGroup.getAsJsonObject("languageStandard");
        String standard = languageStandard == null ? null : CMakeFileApi.getString(languageStandard, "standard");
        if (standard == null) {
            standard = flags.standard();
        }

        // a target given on the command line wins over the compiler's own
        String compilerTarget = flags.target() != null ? flags.target() : toolchains.getTarget(language);

        return new CMakeCompileGroup(language, includePaths, systemIncludePaths, defines, standard, compilerTarget);
    }

    private static List<String> readCommandFragments(JsonObject compileGroup) {
        List<String> fragments = new ArrayList<>();
        JsonArray array = compileGroup.getAsJsonArray("compileCommandFragments");
        if (array == null) {
            return fragments;
        }
        for (JsonElement element : array) {
            String fragment = CMakeFileApi.getString(element.getAsJsonObject(), "fragment");
            if (fragment != null) {
                fragments.add(fragment);
            }
        }
        return fragments;
    }

    private static Map<String, String> readDefines(JsonObject compileGroup) {
        Map<String, String> defines = new LinkedHashMap<>();
        JsonArray array = compileGroup.getAsJsonArray("defines");
        if (array == null) {
            return defines;
        }
        for (JsonElement element : array) {
            String define = CMakeFileApi.getString(element.getAsJsonObject(), "define");
            if (define == null || define.isEmpty()) {
                continue;
            }
            int eq = define.indexOf('=');
            if (eq < 0) {
                // like -DNAME, which defines NAME as 1
                defines.put(define, "1");
            }
            else {
                defines.put(define.substring(0, eq), define.substring(eq + 1));
            }
        }
        return defines;
    }
}
