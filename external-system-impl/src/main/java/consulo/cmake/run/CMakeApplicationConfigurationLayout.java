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
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.nativeDev.debugger.run.NativeProgramParametersLayout;
import consulo.project.Project;
import consulo.ui.ComboBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
public class CMakeApplicationConfigurationLayout extends NativeProgramParametersLayout<CMakeApplicationConfiguration> {
    private final Project myProject;
    @Nullable
    private ComboBox<CMakeTargetReference> myTargetBox;

    @RequiredUIAccess
    public CMakeApplicationConfigurationLayout(Project project) {
        super(project);
        myProject = project;
    }

    @RequiredUIAccess
    @Override
    protected void addBefore(FormBuilder builder) {
        List<CMakeTargetReference> targets = new ArrayList<>();
        Module[] modules = ModuleManager.getInstance(myProject).getModules();
        for (Module module : modules) {
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
        myTargetBox = targetBox;

        builder.addLabeled(CMakeLocalize.runConfigurationTargetLabel(), targetBox);
    }

    @RequiredUIAccess
    @Override
    public void reset(CMakeApplicationConfiguration configuration) {
        super.reset(configuration);
        ComboBox<CMakeTargetReference> targetBox = myTargetBox;
        if (targetBox == null) {
            return;
        }
        Module module = configuration.getConfigurationModule().getModule();
        String targetName = configuration.getTargetName();
        targetBox.setValueByCondition(
            reference -> reference.module() == module && Objects.equals(reference.target().name(), targetName)
        );
    }

    @RequiredUIAccess
    @Override
    public void apply(CMakeApplicationConfiguration configuration) {
        super.apply(configuration);
        ComboBox<CMakeTargetReference> targetBox = myTargetBox;
        CMakeTargetReference reference = targetBox == null ? null : targetBox.getValue();
        configuration.setModuleName(reference == null ? null : reference.module().getName());
        configuration.setTargetName(reference == null ? null : reference.target().name());
    }
}
