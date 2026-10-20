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

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author VISTALL
 * @since 2026-10-19
 */
public class CMakeCompileFlagsTest {
    @Test
    public void testFlagsOfAddCompileOptions() {
        // what CMake reports for add_compile_options("-std=c11" "-D_DEFAULT_SOURCE") - one fragment per flag
        CMakeCompileFlags flags = CMakeCompileFlags.parse(List.of("-g", "-O2", "-Wall", "-std=c11", "-D_DEFAULT_SOURCE"), false);

        assertEquals("11", flags.standard());
        assertEquals(Map.of("_DEFAULT_SOURCE", "1"), defines(flags));
        assertEquals(List.of(), flags.includePaths());
    }

    @Test
    public void testDefines() {
        // a quoted define as CMake escapes it: "-DMESSAGE=\"hello world\""
        CMakeCompileFlags flags = CMakeCompileFlags.parse(List.of("-DA -D B=2 \"-DMESSAGE=\\\"hello world\\\"\" -DC -UC"), false);

        assertEquals(Map.of("A", "1", "B", "2", "MESSAGE", "\"hello world\""), defines(flags));
    }

    @Test
    public void testUndefineRemovesStructuredDefine() {
        Map<String, String> defines = new LinkedHashMap<>(Map.of("NDEBUG", "1", "KEEP", "1"));

        CMakeCompileFlags.parse(List.of("-UNDEBUG"), false).applyDefines(defines);

        assertEquals(Map.of("KEEP", "1"), defines);
    }

    @Test
    public void testIncludeDirectories() {
        CMakeCompileFlags flags = CMakeCompileFlags.parse(List.of("-I/a -I /b -iquote /c -idirafter /d -isystem /sys -isystem/sys2"), false);

        assertEquals(List.of("/a", "/b", "/c", "/d"), flags.includePaths());
        assertEquals(List.of("/sys", "/sys2"), flags.systemIncludePaths());
    }

    @Test
    public void testOptionArgumentIsNoOption() {
        // the file of -include starts with /D, which is no define outside MSVC
        CMakeCompileFlags flags = CMakeCompileFlags.parse(List.of("-include /Dir/prefix.h -x c"), false);

        assertEquals(Map.of(), defines(flags));
        assertEquals(List.of(), flags.includePaths());
    }

    @Test
    public void testMsvc() {
        CMakeCompileFlags flags = CMakeCompileFlags.parse(List.of("/DWIN32 /D _DEBUG /I C:\\work\\include /external:I C:\\sdk /std:c++17"), true);

        assertEquals(Map.of("WIN32", "1", "_DEBUG", "1"), defines(flags));
        assertEquals(List.of("C:\\work\\include"), flags.includePaths());
        assertEquals(List.of("C:\\sdk"), flags.systemIncludePaths());
        assertEquals("17", flags.standard());
    }

    @Test
    public void testSlashOptionsOnlyForMsvc() {
        CMakeCompileFlags flags = CMakeCompileFlags.parse(List.of("/DWIN32"), false);

        assertEquals(Map.of(), defines(flags));
    }

    @Test
    public void testTarget() {
        assertEquals("aarch64-linux-gnu", CMakeCompileFlags.parse(List.of("--target=aarch64-linux-gnu -O2"), false).target());
        assertEquals("arm-none-eabi", CMakeCompileFlags.parse(List.of("-target arm-none-eabi"), false).target());
        assertEquals("riscv64-unknown-elf", CMakeCompileFlags.parse(List.of("--target", "riscv64-unknown-elf"), false).target());
        assertNull(CMakeCompileFlags.parse(List.of("-O2 -Wall"), false).target());
    }

    @Test
    public void testStandards() {
        assertEquals("90", CMakeCompileFlags.parseStandard("c89"));
        assertEquals("99", CMakeCompileFlags.parseStandard("gnu99"));
        assertEquals("99", CMakeCompileFlags.parseStandard("iso9899:1999"));
        assertEquals("17", CMakeCompileFlags.parseStandard("c18"));
        assertEquals("23", CMakeCompileFlags.parseStandard("c2x"));
        assertEquals("98", CMakeCompileFlags.parseStandard("c++03"));
        assertEquals("14", CMakeCompileFlags.parseStandard("gnu++1y"));
        assertEquals("20", CMakeCompileFlags.parseStandard("c++2a"));
        assertEquals("23", CMakeCompileFlags.parseStandard("c++latest"));
        assertNull(CMakeCompileFlags.parseStandard("pedantic"));
    }

    @Test
    public void testSplitArguments() {
        assertEquals(List.of("-DA=a b", "-I/x y", "-Dq='"), CMakeCompileFlags.splitArguments("\"-DA=a b\" -I/x\\ y -Dq=\\'", false));
        assertEquals(List.of("/IC:\\a\\b", "/DX=\"y\""), CMakeCompileFlags.splitArguments("/IC:\\a\\b /DX=\\\"y\\\"", true));
    }

    private static Map<String, String> defines(CMakeCompileFlags flags) {
        Map<String, String> defines = new LinkedHashMap<>();
        flags.applyDefines(defines);
        return defines;
    }
}
