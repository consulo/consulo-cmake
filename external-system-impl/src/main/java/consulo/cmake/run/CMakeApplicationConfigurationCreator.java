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

import consulo.application.ReadAction;
import consulo.cmake.module.CMakeTarget;
import consulo.execution.RunManager;
import consulo.execution.RunnerAndConfigurationSettings;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.RunConfiguration;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
public final class CMakeApplicationConfigurationCreator {
    private CMakeApplicationConfigurationCreator() {
    }

    public static void createConfigurations(Project project) {
        List<CMakeTargetReference> targets = new ArrayList<>();
        for (Module module : ReadAction.compute(() -> ModuleManager.getInstance(project).getModules())) {
            for (CMakeTarget target : CMakeApplicationConfiguration.findExecutableTargets(module)) {
                targets.add(new CMakeTargetReference(module, target));
            }
        }
        if (targets.isEmpty()) {
            return;
        }
        project.getUIAccess().give(() -> createConfigurations(project, targets));
    }

    @RequiredUIAccess
    private static void createConfigurations(Project project, List<CMakeTargetReference> targets) {
        if (project.isDisposed()) {
            return;
        }

        CMakeApplicationConfigurationType type = CMakeApplicationConfigurationType.getInstance(project.getApplication());
        RunManager runManager = RunManager.getInstance(project);
        Set<String> existing = new HashSet<>();
        for (RunConfiguration configuration : runManager.getConfigurationsList(type)) {
            if (configuration instanceof CMakeApplicationConfiguration cmake) {
                existing.add(key(cmake.getConfigurationModule().getModuleName(), cmake.getTargetName()));
            }
        }

        ConfigurationFactory factory = type.getConfigurationFactories()[0];
        RunnerAndConfigurationSettings first = null;
        for (CMakeTargetReference reference : targets) {
            Module module = reference.module();
            if (module.isDisposed() || !existing.add(key(module.getName(), reference.target().name()))) {
                continue;
            }
            RunnerAndConfigurationSettings settings = runManager.createRunConfiguration(reference.target().name(), factory);
            CMakeApplicationConfiguration configuration = (CMakeApplicationConfiguration) settings.getConfiguration();
            configuration.setModule(module);
            configuration.setTargetName(reference.target().name());
            runManager.addConfiguration(settings, false);
            if (first == null) {
                first = settings;
            }
        }

        if (first != null && runManager.getSelectedConfiguration() == null) {
            runManager.setSelectedConfiguration(first);
        }
    }

    private static String key(String moduleName, @Nullable String targetName) {
        return moduleName + "/" + targetName;
    }
}
