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

import consulo.configurable.ConfigurationException;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.project.Project;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
public class CMakeApplicationConfigurationEditor extends SettingsEditor<CMakeApplicationConfiguration> {
    private final Project myProject;
    @Nullable
    private CMakeApplicationConfigurationLayout myLayout;

    public CMakeApplicationConfigurationEditor(Project project) {
        myProject = project;
    }

    @RequiredUIAccess
    @Override
    protected Component createUIComponent() {
        CMakeApplicationConfigurationLayout layout = new CMakeApplicationConfigurationLayout(myProject);
        layout.build();
        myLayout = layout;
        return layout.getComponent();
    }

    @RequiredUIAccess
    @Override
    protected void resetEditorFrom(CMakeApplicationConfiguration configuration) {
        CMakeApplicationConfigurationLayout layout = myLayout;
        if (layout != null) {
            layout.reset(configuration);
        }
    }

    @RequiredUIAccess
    @Override
    protected void applyEditorTo(CMakeApplicationConfiguration configuration) throws ConfigurationException {
        CMakeApplicationConfigurationLayout layout = myLayout;
        if (layout != null) {
            layout.apply(configuration);
        }
    }
}
