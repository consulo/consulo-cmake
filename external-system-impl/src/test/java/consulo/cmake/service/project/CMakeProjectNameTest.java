package consulo.cmake.service.project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The project name read from CMakeLists.txt when there is no File API reply yet.
 */
public class CMakeProjectNameTest {
    @TempDir
    Path myTempDir;

    /**
     * Boost's CMakeLists.txt mentions "project()" in a comment before the call.
     */
    @Test
    public void testCommentBeforeCall() throws Exception {
        assertEquals("Boost", nameOf("""
            cmake_minimum_required(VERSION 3.8...3.16)

            # The default build type must be set before project()
            if(NOT CMAKE_BUILD_TYPE)
              set(CMAKE_BUILD_TYPE Release)
            endif()

            project(Boost VERSION 1.93.0 LANGUAGES CXX)
            """));
    }

    @Test
    public void testIndentedCall() throws Exception {
        assertEquals("sample", nameOf("  PROJECT ( sample C )\n"));
    }

    @Test
    public void testNoCall() throws Exception {
        assertNull(nameOf("# project() is called elsewhere\nadd_subdirectory(src)\n"));
    }

    private String nameOf(String text) throws Exception {
        File file = myTempDir.resolve("CMakeLists.txt").toFile();
        Files.writeString(file.toPath(), text, StandardCharsets.UTF_8);
        return CMakeProjectResolver.extractProjectName(file);
    }
}
