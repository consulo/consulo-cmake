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

import consulo.cmake.icon.CMakeIconGroup;
import consulo.cmake.localize.CMakeLocalize;
import consulo.cmake.module.CMakeTarget;
import consulo.disposer.Disposable;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.configuration.fragment.RunConfigurationFragmentEditor;
import consulo.execution.configuration.fragment.RunConfigurationFragmentLayout;
import consulo.execution.configuration.fragment.StandardRunConfigurationFragmentOrder;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.project.Project;
import consulo.ui.ComboBox;
import consulo.ui.annotation.RequiredUIAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The executable target to run; the module of the target is chosen by the module fragment.
 */
public class CMakeTargetFragment implements RunConfigurationFragment<CMakeApplicationConfiguration> {
    @Override
    public String getId() {
        return "cmakeTarget";
    }

    @Override
    public LocalizeValue getName() {
        return CMakeLocalize.runConfigurationFragmentTarget();
    }

    @Override
    public int getOrder() {
        return StandardRunConfigurationFragmentOrder.FIRST;
    }

    @Override
    public boolean isRemovable() {
        return false;
    }

    @RequiredUIAccess
    @Override
    public RunConfigurationFragmentEditor<CMakeApplicationConfiguration> createEditor(Project project, Disposable parent) {
        List<CMakeTargetReference> targets = new ArrayList<>();
        for (Module module : ModuleManager.getInstance(project).getModules()) {
            for (CMakeTarget target : CMakeApplicationConfiguration.findExecutableTargets(module)) {
                targets.add(new CMakeTargetReference(module, target));
            }
        }
        boolean showModule = targets.stream().map(CMakeTargetReference::module).distinct().count() > 1;
        ComboBox<CMakeTargetReference> targetBox = ComboBox.create(targets);
        targetBox.setRenderer((presentation, item) -> {
            CMakeTargetReference reference = item.getValue();
            if (reference != null) {
                presentation.withIcon(CMakeIconGroup.cmake());
                presentation.append(reference.target().name());
                if (showModule) {
                    presentation.append(" [" + reference.module().getName() + "]");
                }
            }
        });

        return new RunConfigurationFragmentEditor<>() {
            @RequiredUIAccess
            @Override
            public void addRows(RunConfigurationFragmentLayout layout) {
                layout.addLabeled(CMakeLocalize.runConfigurationTargetLabel(), targetBox);
            }

            @RequiredUIAccess
            @Override
            public void reset(CMakeApplicationConfiguration target) {
                Module module = target.getConfigurationModule().getModule();
                String targetName = target.getTargetName();
                targetBox.setValueByCondition(
                    reference -> reference.module() == module && Objects.equals(reference.target().name(), targetName)
                );
            }

            @RequiredUIAccess
            @Override
            public void apply(CMakeApplicationConfiguration target) {
                CMakeTargetReference reference = targetBox.getValue();
                target.setTargetName(reference == null ? null : reference.target().name());
            }
        };
    }
}
