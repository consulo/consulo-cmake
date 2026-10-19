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
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author VISTALL
 * @since 2026-10-19
 */
public class CMakeToolchainsTest {
    /**
     * Shaped like the toolchains-v1 reply of CMake 4 with GCC and MSVC.
     */
    private static final String REPLY = """
        {
          "kind": "toolchains",
          "toolchains": [
            {
              "language": "C",
              "compiler": {
                "id": "GNU",
                "path": "/usr/bin/cc",
                "version": "16.2.1",
                "implicit": {
                  "includeDirectories": ["/usr/lib/gcc/x86_64-pc-linux-gnu/16/include", "/usr/local/include", "/usr/include"],
                  "linkDirectories": ["/usr/lib"]
                }
              },
              "sourceFileExtensions": ["c", "m"]
            },
            {
              "language": "CXX",
              "compiler": {
                "id": "MSVC",
                "path": "C:/VS/cl.exe",
                "implicit": {}
              }
            }
          ],
          "version": {"major": 1, "minor": 1}
        }
        """;

    @Test
    public void testImplicitIncludeDirectories() {
        CMakeToolchains toolchains = CMakeToolchains.read(new Gson().fromJson(REPLY, JsonObject.class));

        assertEquals(List.of("/usr/lib/gcc/x86_64-pc-linux-gnu/16/include", "/usr/local/include", "/usr/include"),
            toolchains.getImplicitIncludeDirectories("C"));
        assertEquals(List.of(), toolchains.getImplicitIncludeDirectories("CXX"));
        assertEquals(List.of(), toolchains.getImplicitIncludeDirectories("CUDA"));
    }

    @Test
    public void testMsvc() {
        CMakeToolchains toolchains = CMakeToolchains.read(new Gson().fromJson(REPLY, JsonObject.class));

        assertFalse(toolchains.isMsvc("C"));
        assertTrue(toolchains.isMsvc("CXX"));
    }

    @Test
    public void testNoReply() {
        // CMake before 3.20 has no toolchains object
        assertEquals(List.of(), CMakeToolchains.read(null).getImplicitIncludeDirectories("C"));
    }
}
