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
import consulo.execution.configuration.ModuleBasedConfiguration;
import consulo.execution.configuration.RunConfiguration;
import consulo.execution.configuration.RunConfigurationModule;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.configuration.RuntimeConfigurationError;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.externalSystem.util.ExternalSystemApiUtil;
import consulo.module.Module;
import consulo.nativeDev.debugger.driver.NativeDebugTarget;
import consulo.nativeDev.debugger.run.NativeProgramRunConfiguration;
import consulo.nativeDev.debugger.run.NativeProgramSettings;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilderFactory;
import consulo.util.xml.serializer.InvalidDataException;
import consulo.util.xml.serializer.WriteExternalException;
import jakarta.annotation.Nullable;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
public class CMakeApplicationConfiguration extends ModuleBasedConfiguration<RunConfigurationModule> implements NativeProgramRunConfiguration {
    private static final String ATTR_TARGET = "target";

    private NativeProgramSettings myProgramSettings = new NativeProgramSettings();
    @Nullable
    private String myTargetName;

    public CMakeApplicationConfiguration(String name, RunConfigurationModule configurationModule, ConfigurationFactory factory) {
        super(name, configurationModule, factory);
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
        return myTargetName;
    }

    public void setTargetName(@Nullable String targetName) {
        myTargetName = targetName;
    }

    @Override
    public NativeProgramSettings getProgramSettings() {
        return myProgramSettings;
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
        String targetName = myTargetName;
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
    public boolean isCompileBeforeLaunchAddedByDefault() {
        return false;
    }

    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new CMakeApplicationConfigurationEditor(getProject());
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationError {
        String targetName = myTargetName;
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
                    .newBuilder(myProgramSettings.createCommandLine(executable.toString(), executable.getParent()))
                    .killable()
                    .colored()
                    .build();
            }
        };
    }

    @Override
    public NativeDebugTarget createDebugTarget(ExecutionEnvironment environment) throws ExecutionException {
        Path executable = getExecutable();
        return myProgramSettings.createDebugTarget(executable, executable.getParent());
    }

    private Path getExecutable() throws ExecutionException {
        String targetName = myTargetName;
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

    @Override
    public ModuleBasedConfiguration clone() {
        CMakeApplicationConfiguration clone = (CMakeApplicationConfiguration) super.clone();
        clone.myProgramSettings = myProgramSettings.copy();
        return clone;
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);
        readModule(element);
        myTargetName = element.getAttributeValue(ATTR_TARGET);
        myProgramSettings.readExternal(element);
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);
        writeModule(element);
        if (myTargetName != null) {
            element.setAttribute(ATTR_TARGET, myTargetName);
        }
        myProgramSettings.writeExternal(element);
    }
}
