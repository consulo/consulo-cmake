package consulo.cmake.debug;

import consulo.cmake.externalSystem.impl.CMakeConstants;
import consulo.cmake.setting.CMakeProjectSettings;
import consulo.cmake.setting.CMakeSettings;
import consulo.document.FileDocumentManager;
import consulo.execution.configuration.RunProfile;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.debug.*;
import consulo.execution.runner.DefaultProgramRunner;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.ui.RunContentDescriptor;
import consulo.externalSystem.service.execution.ExternalSystemRunConfiguration;
import consulo.process.ExecutionException;
import consulo.util.io.NetUtil;
import jakarta.annotation.Nonnull;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Program runner that debugs cmake scripts via cmake --debugger DAP server.
 * Intercepts ExternalSystemRunConfiguration for CMake with the Debug executor.
 * Requires CMake 3.27+.
 *
 * @author VISTALL
 */
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
        return CMakeConstants.SYSTEM_ID.getId().equals(config.getSettings().getExternalSystemIdString());
    }

    @Override
    protected RunContentDescriptor doExecute(@Nonnull RunProfileState state,
                                             @Nonnull ExecutionEnvironment env)
        throws ExecutionException {
        ExternalSystemRunConfiguration config = (ExternalSystemRunConfiguration) env.getRunProfile();
        String projectPath = config.getSettings().getExternalProjectPath();

        CMakeProjectSettings projectSettings = CMakeSettings.getInstance(env.getProject())
            .getLinkedProjectSettings(projectPath);

        String sourceDir = projectPath;
        String buildDir;
        if (projectSettings != null && projectSettings.getBuildDirectory() != null) {
            buildDir = projectSettings.getBuildDirectory();
        }
        else {
            buildDir = new File(sourceDir, "build").getAbsolutePath();
        }

        final int port;
        try {
            port = NetUtil.findAvailableSocketPort();
        }
        catch (IOException e) {
            throw new ExecutionException("Cannot find available port for cmake debugger: " + e.getMessage(), e);
        }

        FileDocumentManager.getInstance().saveAllDocuments();

        String finalSourceDir = sourceDir;
        String finalBuildDir = buildDir;

        XDebugSession session = XDebuggerManager.getInstance(env.getProject())
            .startSession(env, new XDebugProcessStarter() {
                @Nonnull
                @Override
                public XDebugProcess start(@Nonnull XDebugSession session) throws ExecutionException {
                    CMakeDebugProcess process = new CMakeDebugProcess(
                        session, port, finalSourceDir, finalBuildDir, List.of());
                    process.start();
                    return process;
                }
            });

        return session.getRunContentDescriptor();
    }
}
