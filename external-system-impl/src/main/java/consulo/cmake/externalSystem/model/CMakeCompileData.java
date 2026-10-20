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
package consulo.cmake.externalSystem.model;

import consulo.cmake.externalSystem.impl.CMakeConstants;
import consulo.externalSystem.model.Key;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.service.project.AbstractExternalEntityData;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
public class CMakeCompileData extends AbstractExternalEntityData {
    private static final long serialVersionUID = 1L;

    public static final String LANGUAGE_C = "C";
    public static final String LANGUAGE_CXX = "CXX";

    public static final Key<CMakeCompileData> KEY = Key.create(CMakeCompileData.class, ProjectKeys.MODULE.getProcessingWeight() + 1);

    private final String myTargetName;
    private final String myLanguage;
    private final List<String> myIncludePaths;
    private final List<String> mySystemIncludePaths;
    private final Map<String, String> myDefines;
    @Nullable
    private final String myStandard;
    @Nullable
    private final String myCompilerTarget;

    public CMakeCompileData(String targetName,
                            String language,
                            List<String> includePaths,
                            List<String> systemIncludePaths,
                            Map<String, String> defines,
                            @Nullable String standard) {
        this(targetName, language, includePaths, systemIncludePaths, defines, standard, null);
    }

    /**
     * @param compilerTarget the target triple the sources compile for - {@code x86_64-pc-linux-gnu},
     *                       {@code arm-none-eabi} - {@code null} when unknown
     */
    public CMakeCompileData(String targetName,
                            String language,
                            List<String> includePaths,
                            List<String> systemIncludePaths,
                            Map<String, String> defines,
                            @Nullable String standard,
                            @Nullable String compilerTarget) {
        super(CMakeConstants.SYSTEM_ID);
        myTargetName = targetName;
        myLanguage = language;
        myIncludePaths = new ArrayList<>(includePaths);
        mySystemIncludePaths = new ArrayList<>(systemIncludePaths);
        myDefines = new LinkedHashMap<>(defines);
        myStandard = standard;
        myCompilerTarget = compilerTarget;
    }

    public String getTargetName() {
        return myTargetName;
    }

    public String getLanguage() {
        return myLanguage;
    }

    public List<String> getIncludePaths() {
        return List.copyOf(myIncludePaths);
    }

    public List<String> getSystemIncludePaths() {
        return List.copyOf(mySystemIncludePaths);
    }

    public Map<String, String> getDefines() {
        return Collections.unmodifiableMap(myDefines);
    }

    @Nullable
    public String getStandard() {
        return myStandard;
    }

    /**
     * The target triple the sources compile for - {@code x86_64-pc-linux-gnu}, {@code arm-none-eabi} - not to be confused
     * with the CMake {@linkplain #getTargetName() target}; {@code null} when unknown.
     */
    @Nullable
    public String getCompilerTarget() {
        return myCompilerTarget;
    }
}
