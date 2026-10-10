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
package consulo.cmake.compiler;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.ReadAction;
import consulo.build.ui.progress.BuildProgress;
import consulo.build.ui.progress.BuildProgressDescriptor;
import consulo.cmake.localize.CMakeLocalize;
import consulo.cmake.module.CMakeTarget;
import consulo.cmake.run.CMakeApplicationConfiguration;
import consulo.cmake.service.process.CMakeCommand;
import consulo.cmake.service.process.CMakeProcessRunner;
import consulo.cmake.service.project.CMakeBuildInfo;
import consulo.cmake.service.project.CMakeFileApi;
import consulo.compiler.CompileContextEx;
import consulo.compiler.CompileDriver;
import consulo.compiler.CompilerRunner;
import consulo.compiler.scope.CompileScope;
import consulo.dataContext.DataContext;
import consulo.execution.configuration.RunConfiguration;
import consulo.externalSystem.util.ExternalSystemApiUtil;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.process.ExecutionException;
import consulo.process.cmd.GeneralCommandLine;
import consulo.project.Project;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
@ExtensionImpl
public class CMakeCompilerRunner implements CompilerRunner {
    private final Project myProject;

    @Inject
    public CMakeCompilerRunner(Project project) {
        myProject = project;
    }

    @Override
    public LocalizeValue getName() {
        return CMakeLocalize.compilerName();
    }

    @RequiredReadAction
    @Override
    public Result checkAvailable(DataContext dataContext) {
        RunConfiguration runConfiguration = dataContext.getData(RunConfiguration.KEY);
        if (runConfiguration != null && !(runConfiguration instanceof CMakeApplicationConfiguration)) {
            return NO;
        }
        for (Module module : ModuleManager.getInstance(myProject).getModules()) {
            if (CMakeTarget.isCMakeModule(module)) {
                return new YesResult(PlatformIconGroup.actionsCompile());
            }
        }
        return NO;
    }

    @Override
    public boolean build(CompileDriver compileDriver,
                         CompileContextEx context,
                         BuildProgress<BuildProgressDescriptor> buildProgress,
                         boolean isRebuild,
                         boolean forceCompile,
                         boolean onlyCheckStatus) {
        if (onlyCheckStatus) {
            return false;
        }

        CompileScope compileScope = context.getCompileScope();
        if (compileScope.getUserData(RunConfiguration.KEY) instanceof CMakeApplicationConfiguration configuration) {
            return buildForLaunch(configuration, context, buildProgress, isRebuild);
        }

        boolean success = true;
        Set<String> builtProjects = new HashSet<>();
        for (Module module : compileScope.getAffectedModules()) {
            String projectPath = ReadAction.compute(() -> CMakeTarget.isCMakeModule(module) ? ExternalSystemApiUtil.getExternalProjectPath(module) : null);
            if (projectPath == null || !builtProjects.add(projectPath)) {
                continue;
            }
            success &= build(CMakeBuildInfo.of(myProject, projectPath), null, context, buildProgress, isRebuild);
        }
        return success;
    }

    private boolean buildForLaunch(CMakeApplicationConfiguration configuration,
                                   CompileContextEx context,
                                   BuildProgress<BuildProgressDescriptor> buildProgress,
                                   boolean isRebuild) {
        String projectPath = ReadAction.compute(configuration::getLinkedProjectPath);
        CMakeTarget target = configuration.getTarget();
        if (projectPath == null || target == null) {
            context.newError(CMakeLocalize.runConfigurationTargetNotSpecified()).add();
            return false;
        }
        return build(CMakeBuildInfo.of(myProject, projectPath), target.name(), context, buildProgress, isRebuild);
    }

    private boolean build(CMakeBuildInfo buildInfo,
                          @Nullable String target,
                          CompileContextEx context,
                          BuildProgress<BuildProgressDescriptor> buildProgress,
                          boolean isRebuild) {
        if (!buildInfo.isConfigured()) {
            try {
                CMakeFileApi.writeQueryFiles(buildInfo.buildDirectory());
            }
            catch (IOException e) {
                context.newError(CMakeLocalize.errorFailedWriteQuery(String.valueOf(e.getMessage()))).add();
                return false;
            }
            GeneralCommandLine configure = CMakeCommand.configure(buildInfo.sourceDirectory(), buildInfo.buildDirectory(), buildInfo.buildType());
            if (!run(configure, buildInfo.sourceDirectory(), context, buildProgress)) {
                return false;
            }
        }

        GeneralCommandLine build = CMakeCommand.build(buildInfo.buildDirectory(), buildInfo.buildType(), target, isRebuild);
        return run(build, buildInfo.buildDirectory(), context, buildProgress);
    }

    private static boolean run(GeneralCommandLine commandLine,
                               File baseDirectory,
                               CompileContextEx context,
                               BuildProgress<BuildProgressDescriptor> buildProgress) {
        String presentation = commandLine.getCommandLineString();
        context.getProgressIndicator().setText(LocalizeValue.of(presentation));
        buildProgress.output("> " + presentation + "\n", true);

        CMakeBuildDiagnosticParser parser = new CMakeBuildDiagnosticParser(context, baseDirectory);
        int exitCode;
        try {
            exitCode = new CMakeProcessRunner().run(commandLine, (text, stdout) -> {
                buildProgress.output(text, stdout);
                parser.accept(text, stdout);
            }, () -> context.getProgressIndicator().isCanceled());
        }
        catch (ExecutionException e) {
            context.newError(CMakeLocalize.compilerCannotRun(presentation, String.valueOf(e.getMessage()))).add();
            return false;
        }
        parser.flush();

        if (exitCode == CMakeProcessRunner.CANCELLED_EXIT_CODE) {
            return false;
        }
        if (exitCode != 0) {
            if (parser.getErrorCount() == 0) {
                context.newError(CMakeLocalize.compilerExitCode(presentation, exitCode)).add();
            }
            return false;
        }
        return true;
    }
}
