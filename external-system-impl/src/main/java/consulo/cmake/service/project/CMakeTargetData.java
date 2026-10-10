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

import consulo.cmake.externalSystem.impl.CMakeConstants;
import consulo.externalSystem.model.Key;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.service.project.AbstractExternalEntityData;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
public class CMakeTargetData extends AbstractExternalEntityData {
    private static final long serialVersionUID = 1L;

    public static final Key<CMakeTargetData> KEY = Key.create(CMakeTargetData.class, ProjectKeys.MODULE.getProcessingWeight() + 1);

    private final String myTargetName;
    private final String myType;
    @Nullable
    private final String myArtifactPath;
    private final String myBuildDirectory;

    public CMakeTargetData(String targetName, String type, @Nullable String artifactPath, String buildDirectory) {
        super(CMakeConstants.SYSTEM_ID);
        myTargetName = targetName;
        myType = type;
        myArtifactPath = artifactPath;
        myBuildDirectory = buildDirectory;
    }

    public String getTargetName() {
        return myTargetName;
    }

    public String getType() {
        return myType;
    }

    @Nullable
    public String getArtifactPath() {
        return myArtifactPath;
    }

    public String getBuildDirectory() {
        return myBuildDirectory;
    }
}
