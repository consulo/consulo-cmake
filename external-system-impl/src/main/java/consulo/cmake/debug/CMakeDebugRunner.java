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
package consulo.cmake.debug;

import consulo.annotation.component.ExtensionImpl;
import consulo.cmake.externalSystem.impl.CMakeConstants;
import consulo.cmake.service.process.CMakeCommand;
import consulo.cmake.service.project.CMakeBuildInfo;
import consulo.cmake.service.project.CMakeFileApi;
import consulo.document.FileDocumentManager;
import consulo.execution.configuration.RunProfile;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.debug.DefaultDebugExecutor;
import consulo.execution.debug.XDebugProcess;
import consulo.execution.debug.XDebugProcessStarter;
import consulo.execution.debug.XDebugSession;
import consulo.execution.debug.XDebuggerManager;
import consulo.execution.process.ProcessTerminatedListener;
import consulo.execution.runner.DefaultProgramRunner;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.ui.RunContentDescriptor;
import consulo.externalSystem.service.execution.ExternalSystemRunConfiguration;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilderFactory;
import consulo.process.cmd.GeneralCommandLine;
import consulo.project.Project;
import jakarta.annotation.Nonnull;

import java.io.IOException;

/**
 * Program runner that debugs cmake scripts via cmake --debugger DAP server.
 * Intercepts ExternalSystemRunConfiguration for CMake with the Debug executor.
 * Requires CMake 3.27+.
 *
 * @author VISTALL
 */
@ExtensionImpl
public class CMakeDebugRunner extends DefaultProgramRunner {
    @Nonnull
    @Override
    public String getRunnerId() {
        return "CMakeDebug";
    }

    @Override
    public boolean canRun(@Nonnull String executorId, @Nonnull RunProfile profile) {
        if (!DefaultDebugExecutor.EXECUTOR_ID.equals(executorId)) {
            return false;
        }
        if (!(profile instanceof ExternalSystemRunConfiguration config)) {
            return false;
        }
        return CMakeConstants.SYSTEM_ID.getId().equals(config.getSettingsCopy().getExternalSystemIdString());
    }

    @Override
    protected RunContentDescriptor doExecute(@Nonnull RunProfileState state,
                                             @Nonnull ExecutionEnvironment env)
        throws ExecutionException {
        Project project = env.getProject();
        ExternalSystemRunConfiguration config = (ExternalSystemRunConfiguration) env.getRunProfile();
        CMakeBuildInfo buildInfo = CMakeBuildInfo.of(project, config.getSettingsCopy().getExternalProjectPath());

        try {
            CMakeFileApi.writeQueryFiles(buildInfo.buildDirectory());
        }
        catch (IOException e) {
            throw new ExecutionException(e.getMessage(), e);
        }

        FileDocumentManager.getInstance().saveAllDocuments();

        String pipeName = CMakeDebuggerPipe.createName();
        GeneralCommandLine commandLine = CMakeCommand.configure(buildInfo.sourceDirectory(), buildInfo.buildDirectory(), buildInfo.buildType())
            .withParameters("--debugger", "--debugger-pipe", pipeName);

        ProcessHandler cmakeProcess = project.getApplication().getInstance(ProcessHandlerBuilderFactory.class)
            .newBuilder(commandLine)
            .killable()
            .colored()
            .build();
        ProcessTerminatedListener.attach(cmakeProcess, project);

        XDebugSession session = XDebuggerManager.getInstance(project).startSession(env, new XDebugProcessStarter() {
            @Nonnull
            @Override
            public XDebugProcess start(@Nonnull XDebugSession session) {
                CMakeDebugProcess process = new CMakeDebugProcess(session, cmakeProcess, pipeName);
                process.start();
                return process;
            }
        });
        return session.getRunContentDescriptor();
    }
}
