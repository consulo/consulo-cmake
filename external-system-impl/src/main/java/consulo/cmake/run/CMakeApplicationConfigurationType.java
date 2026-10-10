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
import consulo.application.Application;
import consulo.cmake.icon.CMakeIconGroup;
import consulo.cmake.localize.CMakeLocalize;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.ConfigurationType;
import consulo.execution.configuration.ConfigurationTypeBase;
import consulo.execution.configuration.RunConfiguration;
import consulo.execution.configuration.RunConfigurationModule;
import consulo.project.Project;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
@ExtensionImpl
public class CMakeApplicationConfigurationType extends ConfigurationTypeBase {
    public static CMakeApplicationConfigurationType getInstance(Application application) {
        return application.getExtensionPoint(ConfigurationType.class).findExtensionOrFail(CMakeApplicationConfigurationType.class);
    }

    public CMakeApplicationConfigurationType() {
        super("CMakeApplication",
            CMakeLocalize.runConfigurationTypeName(),
            CMakeLocalize.runConfigurationTypeDescription(),
            CMakeIconGroup.cmake());
        addFactory(new ConfigurationFactory(this) {
            @Override
            public RunConfiguration createTemplateConfiguration(Project project) {
                return new CMakeApplicationConfiguration("", new RunConfigurationModule(project), this);
            }
        });
    }
}
