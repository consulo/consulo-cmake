package consulo.cmake.service.task;

import consulo.cmake.setting.CMakeExecutionSettings;
import consulo.externalSystem.model.task.ExternalSystemTaskId;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationEvent;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import consulo.externalSystem.rt.model.ExternalSystemException;
import consulo.externalSystem.task.ExternalSystemTaskManager;

import jakarta.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 */
public class CMakeTaskManager implements ExternalSystemTaskManager<CMakeExecutionSettings> {
    private volatile Process myProcess;

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

        for (String taskName : taskNames) {
            runCMakeBuild(id, taskName, buildDir, listener);
        }
    }

    private void runCMakeBuild(ExternalSystemTaskId id, String taskName, String buildDir,
                               ExternalSystemTaskNotificationListener listener) throws ExternalSystemException {
        List<String> command = new ArrayList<>();
        command.add("cmake");
        command.add("--build");
        command.add(buildDir);

        if ("clean".equals(taskName)) {
            command.add("--target");
            command.add("clean");
        }
        else if (!"build".equals(taskName)) {
            command.add("--target");
            command.add(taskName);
        }

        listener.onTaskOutput(id, "Running: " + String.join(" ", command) + "\n", true);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        try {
            myProcess = pb.start();
            try (var reader = myProcess.inputReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    listener.onTaskOutput(id, line + "\n", true);
                }
            }
            int exitCode = myProcess.waitFor();
            if (exitCode != 0) {
                throw new ExternalSystemException("cmake --build exited with code " + exitCode);
            }
        }
        catch (IOException | InterruptedException e) {
            throw new ExternalSystemException("Failed to run cmake --build: " + e.getMessage());
        }
        finally {
            myProcess = null;
        }
    }

    @Override
    public boolean cancelTask(ExternalSystemTaskId id, ExternalSystemTaskNotificationListener listener) throws ExternalSystemException {
        Process process = myProcess;
        if (process != null) {
            process.destroyForcibly();
            return true;
        }
        return false;
    }

    private static void notify(ExternalSystemTaskNotificationListener listener, ExternalSystemTaskId id, String message) {
        listener.onStatusChange(new ExternalSystemTaskNotificationEvent(id, message));
    }
}
