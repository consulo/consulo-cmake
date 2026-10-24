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
import consulo.cmake.localize.CMakeLocalize;
import consulo.cmake.module.CMakeTarget;
import consulo.execution.configuration.CommandLineState;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.LegacyRunConfigurationConverter;
import consulo.execution.configuration.ModuleBasedConfiguration;
import consulo.execution.configuration.RunConfigurationModule;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.configuration.RuntimeConfigurationError;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.externalSystem.util.ExternalSystemApiUtil;
import consulo.module.Module;
import consulo.nativeDev.debugger.driver.NativeDebugTarget;
import consulo.nativeDev.debugger.run.NativeProgramRunConfiguration;
import consulo.nativeDev.profiler.NativeProfilableRunProfile;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilderFactory;
import jakarta.annotation.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
public class CMakeApplicationConfiguration extends ModuleBasedConfiguration<RunConfigurationModule> implements NativeProgramRunConfiguration, NativeProfilableRunProfile {
    public CMakeApplicationConfiguration(String name, RunConfigurationModule configurationModule, ConfigurationFactory factory) {
        super(name, configurationModule, factory);
    }

    @Override
    protected Class<CMakeApplicationConfigurationOptions> getOptionsClass() {
        return CMakeApplicationConfigurationOptions.class;
    }

    @Override
    public CMakeApplicationConfigurationOptions getOptions() {
        return (CMakeApplicationConfigurationOptions) super.getOptions();
    }

    @Override
    protected void convertLegacyState(LegacyRunConfigurationConverter converter) {
        super.convertLegacyState(converter);

        CMakeApplicationConfigurationOptions options = new CMakeApplicationConfigurationOptions();
        options.setTarget(converter.getElement().getAttributeValue("target"));
        converter.setOptions(options);

        NativeProgramRunConfiguration.convertLegacyProgramState(converter);
    }

    @Override
    public List<RunConfigurationFragment<?>> createFragments() {
        return List.of(new CMakeTargetFragment());
    }

    public static List<CMakeTarget> findExecutableTargets(Module module) {
        List<CMakeTarget> targets = new ArrayList<>();
        for (CMakeTarget target : ReadAction.compute(() -> CMakeTarget.list(module))) {
            if (target.isExecutable()) {
                targets.add(target);
            }
        }
        return targets;
    }

    @Nullable
    public String getTargetName() {
        return getOptions().getTarget();
    }

    public void setTargetName(@Nullable String targetName) {
        getOptions().setTarget(targetName);
    }

    @Override
    public Collection<Module> getValidModules() {
        List<Module> modules = new ArrayList<>();
        for (Module module : getAllModules()) {
            if (!findExecutableTargets(module).isEmpty()) {
                modules.add(module);
            }
        }
        return modules;
    }

    @Nullable
    public CMakeTarget getTarget() {
        Module module = getConfigurationModule().getModule();
        String targetName = getTargetName();
        if (module == null || targetName == null) {
            return null;
        }
        CMakeTarget target = ReadAction.compute(() -> CMakeTarget.find(module, targetName));
        return target != null && target.isExecutable() ? target : null;
    }

    @Nullable
    public String getLinkedProjectPath() {
        Module module = getConfigurationModule().getModule();
        return module == null ? null : ExternalSystemApiUtil.getExternalProjectPath(module);
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationError {
        String targetName = getTargetName();
        if (getConfigurationModule().getModule() == null || targetName == null) {
            throw new RuntimeConfigurationError(CMakeLocalize.runConfigurationTargetNotSpecified());
        }
        if (getTarget() == null) {
            throw new RuntimeConfigurationError(CMakeLocalize.runConfigurationTargetNotExecutable(targetName));
        }
    }

    @Override
    public RunProfileState getState(Executor executor, ExecutionEnvironment environment) throws ExecutionException {
        Path executable = getExecutable();
        return new CommandLineState(environment) {
            @Override
            protected ProcessHandler startProcess() throws ExecutionException {
                return getProject().getApplication().getInstance(ProcessHandlerBuilderFactory.class)
                    .newBuilder(createCommandLine(executable.toString(), executable.getParent()))
                    .killable()
                    .colored()
                    .build();
            }
        };
    }

    @Override
    public NativeDebugTarget createDebugTarget(ExecutionEnvironment environment) throws ExecutionException {
        Path executable = getExecutable();
        return createDebugTarget(executable, executable.getParent());
    }

    private Path getExecutable() throws ExecutionException {
        String targetName = getTargetName();
        if (getConfigurationModule().getModule() == null || targetName == null) {
            throw new ExecutionException(CMakeLocalize.runConfigurationTargetNotSpecified().get());
        }
        CMakeTarget target = getTarget();
        if (target == null) {
            throw new ExecutionException(CMakeLocalize.runConfigurationTargetNotExecutable(targetName).get());
        }
        Path artifact = target.artifact();
        if (artifact == null) {
            throw new ExecutionException(CMakeLocalize.runConfigurationArtifactUnknown(target.name()).get());
        }
        return artifact;
    }
}
