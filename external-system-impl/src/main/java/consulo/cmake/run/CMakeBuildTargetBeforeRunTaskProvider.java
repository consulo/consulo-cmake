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
package consulo.cmake.run;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.dumb.DumbAware;
import consulo.cmake.externalSystem.impl.CMakeConstants;
import consulo.cmake.icon.CMakeIconGroup;
import consulo.cmake.localize.CMakeLocalize;
import consulo.cmake.module.CMakeTarget;
import consulo.dataContext.DataContext;
import consulo.execution.configuration.RunConfiguration;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.externalSystem.execution.ExternalSystemBeforeRunTask;
import consulo.externalSystem.execution.ExternalSystemBeforeRunTaskProvider;
import consulo.externalSystem.model.execution.ExternalSystemTaskExecutionSettings;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.UIAccess;
import consulo.ui.image.Image;
import consulo.util.dataholder.Key;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
@ExtensionImpl
public final class CMakeBuildTargetBeforeRunTaskProvider extends ExternalSystemBeforeRunTaskProvider implements DumbAware {
    public static final Key<ExternalSystemBeforeRunTask> ID = Key.create("CMake.BuildTarget.BeforeRunTask");

    @Inject
    public CMakeBuildTargetBeforeRunTaskProvider(Project project) {
        super(CMakeConstants.SYSTEM_ID, project, ID);
    }

    @Override
    public LocalizeValue getName() {
        return CMakeLocalize.beforeRunBuildTargetName();
    }

    @Override
    public Image getIcon(RunConfiguration runConfiguration) {
        return CMakeIconGroup.cmake();
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public ExternalSystemBeforeRunTask createTask(RunConfiguration runConfiguration) {
        ExternalSystemBeforeRunTask task = new ExternalSystemBeforeRunTask(ID, CMakeConstants.SYSTEM_ID);
        task.setEnabled(runConfiguration instanceof CMakeApplicationConfiguration);
        return task;
    }

    @Override
    public boolean canExecuteTask(RunConfiguration configuration, ExternalSystemBeforeRunTask beforeRunTask) {
        return !(configuration instanceof CMakeApplicationConfiguration cmakeConfiguration) || updateTask(cmakeConfiguration, beforeRunTask);
    }

    @Override
    public CompletableFuture<Void> executeTaskAsync(UIAccess uiAccess,
                                                    DataContext context,
                                                    RunConfiguration configuration,
                                                    ExecutionEnvironment env,
                                                    ExternalSystemBeforeRunTask beforeRunTask) {
        if (configuration instanceof CMakeApplicationConfiguration cmakeConfiguration && !updateTask(cmakeConfiguration, beforeRunTask)) {
            return CompletableFuture.completedFuture(null);
        }
        return super.executeTaskAsync(uiAccess, context, configuration, env, beforeRunTask);
    }

    @Override
    public LocalizeValue getDescription(ExternalSystemBeforeRunTask task) {
        List<String> taskNames = task.getTaskExecutionSettings().getTaskNames();
        return taskNames.isEmpty() ? getName() : CMakeLocalize.beforeRunBuildTargetDescription(String.join(" ", taskNames));
    }

    private static boolean updateTask(CMakeApplicationConfiguration configuration, ExternalSystemBeforeRunTask task) {
        CMakeTarget target = configuration.getTarget();
        String linkedProjectPath = configuration.getLinkedProjectPath();
        if (target == null || linkedProjectPath == null) {
            return false;
        }
        ExternalSystemTaskExecutionSettings settings = task.getTaskExecutionSettings();
        settings.setExternalProjectPath(linkedProjectPath);
        settings.setTaskNames(List.of(target.name()));
        return true;
    }
}
