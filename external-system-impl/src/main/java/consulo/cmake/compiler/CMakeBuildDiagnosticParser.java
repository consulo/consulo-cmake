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
package consulo.cmake.compiler;

import consulo.compiler.CompileContext;
import consulo.compiler.CompilerMessageCategory;
import consulo.localize.LocalizeValue;
import consulo.virtualFileSystem.util.VirtualFileUtil;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
final class CMakeBuildDiagnosticParser {
    private static final Pattern GCC = Pattern.compile("^(.+?):(\\d+):(?:(\\d+):)? (fatal error|error|warning|note): (.*)$");
    private static final Pattern MSVC =
        Pattern.compile("^\\s*(.+?)\\((\\d+)(?:,(\\d+))?\\)\\s*:\\s*(fatal error|error|warning)\\s*[A-Za-z]*\\d*\\s*:\\s*(.*?)(?:\\s+\\[[^\\]]+\\])?$");

    private final CompileContext myContext;
    private final File myBaseDirectory;
    private final StringBuilder myStdout = new StringBuilder();
    private final StringBuilder myStderr = new StringBuilder();
    private final Set<String> myReported = new HashSet<>();
    private int myErrorCount;

    CMakeBuildDiagnosticParser(CompileContext context, File baseDirectory) {
        myContext = context;
        myBaseDirectory = baseDirectory;
    }

    synchronized void accept(String text, boolean stdout) {
        StringBuilder buffer = stdout ? myStdout : myStderr;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                parse(buffer.toString());
                buffer.setLength(0);
            }
            else if (c != '\r') {
                buffer.append(c);
            }
        }
    }

    synchronized void flush() {
        parse(myStdout.toString());
        parse(myStderr.toString());
        myStdout.setLength(0);
        myStderr.setLength(0);
    }

    synchronized int getErrorCount() {
        return myErrorCount;
    }

    private void parse(String line) {
        if (line.isBlank() || !myReported.add(line)) {
            return;
        }

        Matcher gcc = GCC.matcher(line);
        if (gcc.matches()) {
            report(gcc.group(1), gcc.group(2), gcc.group(3), gcc.group(4), gcc.group(5));
            return;
        }

        Matcher msvc = MSVC.matcher(line);
        if (msvc.matches()) {
            report(msvc.group(1), msvc.group(2), msvc.group(3), msvc.group(4), msvc.group(5));
        }
    }

    private void report(String path, String line, String column, String kind, String message) {
        File file = new File(path);
        if (!file.isAbsolute()) {
            file = new File(myBaseDirectory, path);
        }
        if (!file.isFile()) {
            return;
        }

        CompilerMessageCategory category = switch (kind) {
            case "error", "fatal error" -> CompilerMessageCategory.ERROR;
            case "warning" -> CompilerMessageCategory.WARNING;
            default -> CompilerMessageCategory.INFORMATION;
        };
        if (category == CompilerMessageCategory.ERROR) {
            myErrorCount++;
        }

        myContext.newMessage(category, LocalizeValue.of(message))
            .url(VirtualFileUtil.pathToUrl(file.getAbsolutePath()))
            .position(Integer.parseInt(line), column == null ? 1 : Integer.parseInt(column))
            .add();
    }
}
