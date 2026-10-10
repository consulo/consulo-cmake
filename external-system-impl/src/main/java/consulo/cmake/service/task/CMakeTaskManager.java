package consulo.cmake.service.task;

import consulo.cmake.externalSystem.impl.CMakeConstants;
import consulo.cmake.localize.CMakeLocalize;
import consulo.cmake.service.process.CMakeProcessRunner;
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
        String buildDir = settings != null ? settings.getBuildDirectory() : null;
        if (buildDir == null || buildDir.isBlank()) {
            File sourceDir = new File(projectPath);
            if (sourceDir.isFile()) sourceDir = sourceDir.getParentFile();
            buildDir = sourceDir.getAbsolutePath() + File.separator + "build";
        }
        String buildType = settings != null ? settings.getBuildType() : CMakeProjectSettings.DEFAULT_BUILD_TYPE;

        for (String taskName : taskNames) {
            runCMakeBuild(id, taskName, buildDir, buildType, listener);
        }
    }

    private void runCMakeBuild(ExternalSystemTaskId id, String taskName, String buildDir, String buildType,
                               ExternalSystemTaskNotificationListener listener) throws ExternalSystemException {
        GeneralCommandLine commandLine = new GeneralCommandLine(CMakeConstants.CMAKE_EXECUTABLE)
            .withParameters("--build", buildDir, "--config", buildType);

        if ("clean".equals(taskName)) {
            commandLine.addParameters("--target", "clean");
        }
        else if (!"build".equals(taskName)) {
            commandLine.addParameters("--target", taskName);
        }

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
