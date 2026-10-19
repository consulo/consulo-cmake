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

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * What the flags of a compile command say about its sources: defines, include directories and the language standard.
 * <p>
 * The CMake file API reports these in structured form only when they come from {@code target_compile_definitions},
 * {@code target_include_directories} and {@code <LANG>_STANDARD}. Set with {@code add_compile_options} or
 * {@code CMAKE_<LANG>_FLAGS} they arrive as raw command fragments, which this reads the way the compiler would.
 *
 * @param defineChanges the defines in command order: a {@code null} value undefines ({@code -U}); a later change of the
 *                      same name wins
 * @param standard      the standard in CMake's notation - {@code 11}, {@code 17}, {@code 23} - {@code null} when none
 *                      is given
 * @author VISTALL
 * @since 2026-10-19
 */
record CMakeCompileFlags(List<String> includePaths,
                         List<String> systemIncludePaths,
                         Map<String, @Nullable String> defineChanges,
                         @Nullable String standard) {
    /**
     * Options whose value is the next argument and must not be read as an option itself.
     */
    private static final Set<String> OPTIONS_WITH_ARGUMENT = Set.of(
        "-include", "-imacros", "-x", "-o", "-MF", "-MT", "-MQ", "-Xclang", "-Xpreprocessor", "-arch", "-target", "-isysroot"
    );

    static CMakeCompileFlags parse(List<String> fragments, boolean msvc) {
        List<String> arguments = new ArrayList<>();
        for (String fragment : fragments) {
            arguments.addAll(splitArguments(fragment, msvc));
        }

        List<String> includePaths = new ArrayList<>();
        List<String> systemIncludePaths = new ArrayList<>();
        Map<String, @Nullable String> defineChanges = new LinkedHashMap<>();
        String standard = null;

        for (int i = 0; i < arguments.size(); i++) {
            String argument = arguments.get(i);
            String next = i + 1 < arguments.size() ? arguments.get(i + 1) : null;

            if (OPTIONS_WITH_ARGUMENT.contains(argument)) {
                i++;
                continue;
            }

            Option option = Option.match(argument, msvc);
            if (option == null) {
                continue;
            }

            String value;
            if (argument.length() > option.myPrefix.length()) {
                value = argument.substring(option.myPrefix.length());
            }
            else if (option.mySeparateValue && next != null) {
                value = next;
                i++;
            }
            else {
                continue;
            }

            switch (option) {
                case DEFINE, MSVC_DEFINE -> {
                    int eq = value.indexOf('=');
                    // -DNAME defines NAME as 1, like the compiler does
                    if (eq < 0) {
                        defineChanges.put(value, "1");
                    }
                    else {
                        defineChanges.put(value.substring(0, eq), value.substring(eq + 1));
                    }
                }
                case UNDEFINE, MSVC_UNDEFINE -> defineChanges.put(value, null);
                case INCLUDE, INCLUDE_QUOTE, INCLUDE_AFTER, MSVC_INCLUDE -> includePaths.add(value);
                case INCLUDE_SYSTEM, MSVC_INCLUDE_EXTERNAL, CLANG_INCLUDE_MSVC -> systemIncludePaths.add(value);
                case STANDARD, MSVC_STANDARD -> {
                    String parsed = parseStandard(value);
                    if (parsed != null) {
                        standard = parsed;
                    }
                }
            }
        }
        return new CMakeCompileFlags(includePaths, systemIncludePaths, defineChanges, standard);
    }

    /**
     * Applies the define changes, in order, to the defines CMake reported in structured form - which come first on the
     * command line.
     */
    void applyDefines(Map<String, String> defines) {
        for (Map.Entry<String, @Nullable String> change : defineChanges.entrySet()) {
            String value = change.getValue();
            if (value == null) {
                defines.remove(change.getKey());
            }
            else {
                defines.put(change.getKey(), value);
            }
        }
    }

    private enum Option {
        DEFINE("-D", true, false),
        UNDEFINE("-U", true, false),
        INCLUDE_SYSTEM("-isystem", true, false),
        INCLUDE_QUOTE("-iquote", true, false),
        INCLUDE_AFTER("-idirafter", true, false),
        CLANG_INCLUDE_MSVC("-imsvc", true, false),
        INCLUDE("-I", true, false),
        STANDARD("-std=", false, false),
        MSVC_DEFINE("/D", true, true),
        MSVC_UNDEFINE("/U", true, true),
        MSVC_INCLUDE_EXTERNAL("/external:I", true, true),
        MSVC_INCLUDE("/I", true, true),
        MSVC_STANDARD("/std:", false, true);

        private final String myPrefix;
        private final boolean mySeparateValue;
        private final boolean myMsvc;

        Option(String prefix, boolean separateValue, boolean msvc) {
            myPrefix = prefix;
            mySeparateValue = separateValue;
            myMsvc = msvc;
        }

        /**
         * The option an argument is; MSVC's slash options only for an MSVC compiler, where a path could not start an
         * argument.
         */
        private static @Nullable Option match(String argument, boolean msvc) {
            for (Option option : values()) {
                if ((!option.myMsvc || msvc) && argument.startsWith(option.myPrefix)) {
                    return option;
                }
            }
            return null;
        }
    }

    /**
     * {@code c11}, {@code gnu++17}, {@code c++2a}, {@code iso9899:1999}... in CMake's notation.
     */
    static @Nullable String parseStandard(String value) {
        String standard = value.toLowerCase(Locale.ROOT);
        if (standard.startsWith("iso9899:")) {
            standard = switch (standard.substring("iso9899:".length())) {
                case "1990", "199409" -> "c90";
                case "1999" -> "c99";
                case "2011" -> "c11";
                case "2017", "2018" -> "c17";
                case "2024" -> "c23";
                default -> standard;
            };
        }
        if (standard.startsWith("gnu")) {
            standard = "c" + standard.substring("gnu".length());
        }

        return switch (standard) {
            case "c89", "c90", "ansi" -> "90";
            case "c99", "c9x" -> "99";
            case "c11", "c1x" -> "11";
            case "c17", "c18" -> "17";
            case "c23", "c2x" -> "23";
            case "c++98", "c++03" -> "98";
            case "c++11", "c++0x" -> "11";
            case "c++14", "c++1y" -> "14";
            case "c++17", "c++1z" -> "17";
            case "c++20", "c++2a" -> "20";
            case "c++23", "c++2b", "c++latest", "clatest" -> "23";
            case "c++26", "c++2c" -> "26";
            default -> null;
        };
    }

    /**
     * Splits a command fragment into arguments: whitespace separates, quotes group. A backslash escapes like in a POSIX
     * shell, except for MSVC, where it separates path components and only escapes a quote.
     */
    static List<String> splitArguments(String fragment, boolean msvc) {
        List<String> arguments = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inArgument = false;
        char quote = 0;

        for (int i = 0; i < fragment.length(); i++) {
            char c = fragment.charAt(i);
            if (quote != 0) {
                if (c == quote) {
                    quote = 0;
                }
                else if (c == '\\' && quote == '"' && i + 1 < fragment.length() && (msvc ? "\"" : "\"\\$`").indexOf(fragment.charAt(i + 1)) >= 0) {
                    current.append(fragment.charAt(++i));
                }
                else {
                    current.append(c);
                }
            }
            else if (c == '"' || c == '\'') {
                quote = c;
                inArgument = true;
            }
            else if (c == '\\' && i + 1 < fragment.length() && (!msvc || fragment.charAt(i + 1) == '"')) {
                current.append(fragment.charAt(++i));
                inArgument = true;
            }
            else if (Character.isWhitespace(c)) {
                if (inArgument) {
                    arguments.add(current.toString());
                    current.setLength(0);
                    inArgument = false;
                }
            }
            else {
                current.append(c);
                inArgument = true;
            }
        }
        if (inArgument) {
            arguments.add(current.toString());
        }
        return arguments;
    }
}
