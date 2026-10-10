package consulo.cmake.service.task;

import consulo.cmake.localize.CMakeLocalize;
import consulo.cmake.service.process.CMakeCommand;
import consulo.cmake.service.process.CMakeProcessRunner;
import consulo.cmake.service.project.CMakeProjectPaths;
import consulo.cmake.setting.CMakeExecutionSettings;
import consulo.cmake.setting.CMakeProjectSettings;
import consulo.externalSystem.model.task.ExternalSystemTaskId;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import consulo.externalSystem.rt.model.ExternalSystemException;
import consulo.externalSystem.task.ExternalSystemTaskManager;
import consulo.process.ExecutionException;
import consulo.process.cmd.GeneralCommandLine;

import jakarta.annotation.Nullable;
import java.io.File;
import java.util.List;

/**
 * @author VISTALL
 */
public class CMakeTaskManager implements ExternalSystemTaskManager<CMakeExecutionSettings> {
    private final CMakeProcessRunner myRunner = new CMakeProcessRunner();

    @Override
    public void executeTasks(ExternalSystemTaskId id,
                             List<String> taskNames,
                             String projectPath,
                             @Nullable CMakeExecutionSettings settings,
                             List<String> vmOptions,
                             List<String> scriptParameters,
                             @Nullable String debuggerSetup,
                             ExternalSystemTaskNotificationListener listener) throws ExternalSystemException {
        File buildDir = CMakeProjectPaths.getBuildDir(CMakeProjectPaths.getSourceDir(projectPath),
            settings != null ? settings.getBuildDirectory() : null);
        String buildType = settings != null ? settings.getBuildType() : CMakeProjectSettings.DEFAULT_BUILD_TYPE;

        for (String taskName : taskNames) {
            runCMakeBuild(id, CMakeCommand.build(buildDir, buildType, CMakeCommand.targetOfTask(taskName), false), listener);
        }
    }

    private void runCMakeBuild(ExternalSystemTaskId id, GeneralCommandLine commandLine,
                               ExternalSystemTaskNotificationListener listener) throws ExternalSystemException {
        listener.onTaskOutput(id, "Running: " + commandLine.getCommandLineString() + "\n", true);

        int exitCode;
        try {
            exitCode = myRunner.run(commandLine, id, listener);
        }
        catch (ExecutionException e) {
            throw new ExternalSystemException("Failed to run cmake --build: " + e.getMessage());
        }
        if (exitCode == CMakeProcessRunner.CANCELLED_EXIT_CODE) {
            throw new ExternalSystemException(CMakeLocalize.errorCmakeCancelled().get());
        }
        if (exitCode != 0) {
            throw new ExternalSystemException("cmake --build exited with code " + exitCode);
        }
    }

    @Override
    public boolean cancelTask(ExternalSystemTaskId id, ExternalSystemTaskNotificationListener listener) throws ExternalSystemException {
        myRunner.cancel();
        return true;
    }
}
