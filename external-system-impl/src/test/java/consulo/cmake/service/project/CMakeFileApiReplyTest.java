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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Configures a real project with CMake and reads how its sources compile from the file API reply, with the code a
 * project refresh uses. Skipped without a {@code cmake} on the path, and on Windows, where MSVC takes its include
 * directories from the environment.
 *
 * @author VISTALL
 * @since 2026-10-19
 */
public class CMakeFileApiReplyTest {
    @TempDir
    Path myTempDir;

    @Test
    public void testCompileDataOfConfiguredProject() throws Exception {
        Assumptions.assumeFalse(System.getProperty("os.name").startsWith("Windows"), "MSVC has no implicit include directories");
        Assumptions.assumeTrue(isCMakeAvailable(), "cmake is not on the path");

        Path source = myTempDir.resolve("src").toAbsolutePath();
        Files.createDirectories(source.resolve("include"));
        Files.writeString(source.resolve("CMakeLists.txt"), """
            cmake_minimum_required(VERSION 3.20)
            project(Sample C)
            add_compile_options(-std=c11 -D_DEFAULT_SOURCE)
            add_executable(sample main.c)
            target_include_directories(sample PRIVATE include)
            target_compile_definitions(sample PRIVATE STRUCTURED=2)
            """, StandardCharsets.UTF_8);
        Files.writeString(source.resolve("main.c"), "#include <stdio.h>\nint main(void) { return 0; }\n", StandardCharsets.UTF_8);
        source = source.toRealPath();

        Path build = myTempDir.resolve("build");
        CMakeFileApi.writeQueryFiles(build.toFile());

        Process process = new ProcessBuilder("cmake", "-S", source.toString(), "-B", build.toString(), "-DCMAKE_BUILD_TYPE=Debug")
            .redirectErrorStream(true)
            .start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, process.waitFor(), output);

        // the reply as the resolver reads it: index, codemodel, the target, its compile group, the toolchains
        Gson gson = new Gson();
        File replyDir = CMakeFileApi.getReplyDir(build.toFile());
        File indexFile = CMakeFileApi.findLatestIndex(replyDir);
        assertNotNull(indexFile, "no reply index");
        JsonObject index = CMakeFileApi.readJson(gson, indexFile);

        String toolchainsFile = CMakeFileApi.findObjectJsonFile(index, CMakeFileApi.TOOLCHAINS_KIND);
        assertNotNull(toolchainsFile, "toolchains-v1 was not answered");
        CMakeToolchains toolchains = CMakeToolchains.read(CMakeFileApi.readJson(gson, new File(replyDir, toolchainsFile)));

        JsonObject codemodel = CMakeFileApi.readJson(gson, new File(replyDir, CMakeFileApi.findObjectJsonFile(index, CMakeFileApi.CODEMODEL_KIND)));
        JsonObject configuration = codemodel.getAsJsonArray("configurations").get(0).getAsJsonObject();
        String targetFile = CMakeFileApi.getString(configuration.getAsJsonArray("targets").get(0).getAsJsonObject(), "jsonFile");
        JsonObject target = CMakeFileApi.readJson(gson, new File(replyDir, targetFile));
        JsonObject compileGroup = target.getAsJsonArray("compileGroups").get(0).getAsJsonObject();

        CMakeCompileGroup group = CMakeCompileGroup.read(compileGroup, toolchains);
        assertNotNull(group, "no compile group");

        assertEquals("C", group.language());
        // -std=c11 of add_compile_options
        assertEquals("11", group.standard());
        // -D_DEFAULT_SOURCE of add_compile_options, and target_compile_definitions
        assertEquals("1", group.defines().get("_DEFAULT_SOURCE"));
        assertEquals("2", group.defines().get("STRUCTURED"));
        assertTrue(group.includePaths().contains(source.resolve("include").toString()), group.includePaths().toString());
        // a native build compiles for the machine it runs on: the compiler answers -dumpmachine
        assertNotNull(group.compilerTarget(), "no compiler target");
        assertTrue(group.compilerTarget().contains("-"), group.compilerTarget());
        // the compiler's own directories, where the standard headers are
        assertTrue(group.systemIncludePaths().stream().anyMatch(directory -> Files.exists(Path.of(directory, "stdio.h"))),
            group.systemIncludePaths().toString());
    }

    private static boolean isCMakeAvailable() {
        try {
            return new ProcessBuilder("cmake", "--version").redirectErrorStream(true).start().waitFor() == 0;
        }
        catch (IOException e) {
            return false;
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
