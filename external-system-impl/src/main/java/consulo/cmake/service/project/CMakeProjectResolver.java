package consulo.cmake.service.project;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import consulo.cmake.externalSystem.impl.CMakeConstants;
import consulo.cmake.localize.CMakeLocalize;
import consulo.cmake.setting.CMakeExecutionSettings;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.model.project.ContentRootData;
import consulo.externalSystem.model.project.ModuleData;
import consulo.externalSystem.model.task.TaskData;
import consulo.externalSystem.model.task.ExternalSystemTaskId;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationEvent;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import consulo.externalSystem.rt.model.ExternalSystemException;
import consulo.externalSystem.rt.model.ExternalSystemSourceType;
import consulo.externalSystem.service.project.ExternalSystemProjectResolver;
import consulo.externalSystem.service.project.ProjectData;

import jakarta.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves CMake project structure using the CMake File API (cmake-file-api(7)).
 *
 * @author VISTALL
 */
public class CMakeProjectResolver implements ExternalSystemProjectResolver<CMakeExecutionSettings> {
    private static final Pattern PROJECT_NAME_PATTERN = Pattern.compile("project\\s*\\(\\s*(\\S+)", Pattern.CASE_INSENSITIVE);
    private static final Set<String> SKIP_TARGET_TYPES = Set.of("UTILITY", "INTERFACE_LIBRARY");

    private volatile boolean myCancelled;

    @Nullable
    @Override
    public DataNode<ProjectData> resolveProjectInfo(ExternalSystemTaskId id,
                                                    String projectPath,
                                                    boolean isPreviewMode,
                                                    @Nullable CMakeExecutionSettings settings,
                                                    ExternalSystemTaskNotificationListener listener)
        throws ExternalSystemException, IllegalArgumentException, IllegalStateException {
        myCancelled = false;

        File sourceDir = new File(projectPath);
        if (sourceDir.isFile()) {
            sourceDir = sourceDir.getParentFile();
        }

        String buildDirPath = settings != null ? settings.getBuildDirectory() : null;
        if (buildDirPath == null || buildDirPath.isBlank()) {
            buildDirPath = sourceDir.getAbsolutePath() + File.separator + "build";
        }
        File buildDir = new File(buildDirPath);

        File cmakeListsFile = new File(sourceDir, CMakeConstants.CMAKE_LISTS_TXT);
        if (!cmakeListsFile.exists()) {
            throw new IllegalArgumentException(CMakeLocalize.errorNoCmakeLists(sourceDir.getAbsolutePath()).get());
        }

        notify(listener, id, CMakeLocalize.statusWritingQuery().get());
        writeQueryFile(buildDir);

        if (!isPreviewMode) {
            notify(listener, id, CMakeLocalize.statusConfiguring().get());
            runCMake(sourceDir, buildDir, listener, id);
        }

        notify(listener, id, CMakeLocalize.statusReadingStructure().get());
        File replyDir = buildDir.toPath().resolve(".cmake/api/v1/reply").toFile();
        if (!replyDir.exists()) {
            return fallbackResolve(sourceDir, cmakeListsFile, projectPath);
        }

        return parseFileApiReply(sourceDir, buildDir, replyDir, projectPath);
    }

    // -------------------------------------------------------------------------
    // File API query
    // -------------------------------------------------------------------------

    private static void writeQueryFile(File buildDir) throws ExternalSystemException {
        Path queryDir = buildDir.toPath().resolve(".cmake/api/v1/query");
        try {
            Files.createDirectories(queryDir);
            Path queryFile = queryDir.resolve("codemodel-v2");
            if (!Files.exists(queryFile)) {
                Files.createFile(queryFile);
            }
        }
        catch (IOException e) {
            throw new ExternalSystemException(CMakeLocalize.errorFailedWriteQuery(e.getMessage()).get());
        }
    }

    // -------------------------------------------------------------------------
    // cmake invocation
    // -------------------------------------------------------------------------

    private void runCMake(File sourceDir, File buildDir, ExternalSystemTaskNotificationListener listener, ExternalSystemTaskId id)
        throws ExternalSystemException {
        try {
            Files.createDirectories(buildDir.toPath());
        }
        catch (IOException e) {
            throw new ExternalSystemException(CMakeLocalize.errorCannotCreateBuildDir(e.getMessage()).get());
        }

        ProcessBuilder pb = new ProcessBuilder("cmake", "-S", sourceDir.getAbsolutePath(), "-B", buildDir.getAbsolutePath());
        pb.redirectErrorStream(true);
        try {
            Process process = pb.start();
            try (var reader = process.inputReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (myCancelled) {
                        process.destroyForcibly();
                        throw new ExternalSystemException(CMakeLocalize.errorCmakeCancelled().get());
                    }
                    listener.onTaskOutput(id, line + "\n", true);
                }
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new ExternalSystemException(CMakeLocalize.errorCmakeExitCode(exitCode).get());
            }
        }
        catch (IOException | InterruptedException e) {
            throw new ExternalSystemException(CMakeLocalize.errorFailedRunCmake(e.getMessage()).get());
        }
    }

    // -------------------------------------------------------------------------
    // CMake File API reply parsing
    // -------------------------------------------------------------------------

    private DataNode<ProjectData> parseFileApiReply(File sourceDir, File buildDir, File replyDir, String linkedProjectPath)
        throws ExternalSystemException {
        Gson gson = new Gson();

        File[] indexFiles = replyDir.listFiles((d, name) -> name.startsWith("index-") && name.endsWith(".json"));
        if (indexFiles == null || indexFiles.length == 0) {
            return fallbackResolve(sourceDir, new File(sourceDir, CMakeConstants.CMAKE_LISTS_TXT), linkedProjectPath);
        }

        File indexFile = indexFiles[0];
        for (File f : indexFiles) {
            if (f.lastModified() > indexFile.lastModified()) {
                indexFile = f;
            }
        }

        JsonObject index = readJson(gson, indexFile);

        String codemodelFile = null;
        JsonArray objects = index.getAsJsonArray("objects");
        if (objects != null) {
            for (JsonElement obj : objects) {
                JsonObject o = obj.getAsJsonObject();
                if ("codemodel".equals(getStr(o, "kind"))) {
                    codemodelFile = getStr(o, "jsonFile");
                    break;
                }
            }
        }
        if (codemodelFile == null) {
            return fallbackResolve(sourceDir, new File(sourceDir, CMakeConstants.CMAKE_LISTS_TXT), linkedProjectPath);
        }

        JsonObject codemodel = readJson(gson, new File(replyDir, codemodelFile));

        String projectName = null;
        JsonArray configurations = codemodel.getAsJsonArray("configurations");
        if (configurations != null && !configurations.isEmpty()) {
            JsonObject firstConfig = configurations.get(0).getAsJsonObject();
            JsonArray projects = firstConfig.getAsJsonArray("projects");
            if (projects != null && !projects.isEmpty()) {
                projectName = getStr(projects.get(0).getAsJsonObject(), "name");
            }
        }
        if (projectName == null || projectName.isBlank()) {
            projectName = sourceDir.getName();
        }

        String sourcePath = sourceDir.getAbsolutePath();

        ProjectData projectData = new ProjectData(CMakeConstants.SYSTEM_ID, projectName, sourcePath, linkedProjectPath);
        DataNode<ProjectData> projectNode = new DataNode<>(ProjectKeys.PROJECT, projectData, null);

        // Root module for the project directory
        createRootModule(projectNode, projectName, sourcePath, linkedProjectPath);

        // Always-available build/clean tasks
        projectNode.createChild(ProjectKeys.TASK,
            new TaskData(CMakeConstants.SYSTEM_ID, "build", linkedProjectPath, CMakeLocalize.taskBuildAll().get()));
        projectNode.createChild(ProjectKeys.TASK,
            new TaskData(CMakeConstants.SYSTEM_ID, "clean", linkedProjectPath, CMakeLocalize.taskClean().get()));

        // Parse targets
        if (configurations != null && !configurations.isEmpty()) {
            JsonObject config = configurations.get(0).getAsJsonObject();
            JsonArray targets = config.getAsJsonArray("targets");
            if (targets != null) {
                for (JsonElement targetEl : targets) {
                    String targetJsonFile = getStr(targetEl.getAsJsonObject(), "jsonFile");
                    if (targetJsonFile == null) continue;
                    parseTarget(gson, new File(replyDir, targetJsonFile), sourcePath, buildDir.getAbsolutePath(),
                        projectNode, linkedProjectPath);
                }
            }
        }

        return projectNode;
    }

    private static void createRootModule(DataNode<ProjectData> projectNode, String projectName,
                                         String sourcePath, String linkedProjectPath) {
        ModuleData rootData = new ModuleData(projectName, CMakeConstants.SYSTEM_ID, projectName, sourcePath, linkedProjectPath);
        DataNode<ModuleData> rootNode = projectNode.createChild(ProjectKeys.MODULE, rootData);
        // Content root with no source types — marks the project dir as content boundary
        rootNode.createChild(ProjectKeys.CONTENT_ROOT, new ContentRootData(CMakeConstants.SYSTEM_ID, sourcePath));
    }

    private void parseTarget(Gson gson, File targetFile, String sourcePath, String buildPath,
                             DataNode<ProjectData> projectNode, String linkedProjectPath)
        throws ExternalSystemException {
        if (!targetFile.exists()) return;

        JsonObject target = readJson(gson, targetFile);
        String name = getStr(target, "name");
        String type = getStr(target, "type");
        if (name == null || type == null || SKIP_TARGET_TYPES.contains(type)) {
            return;
        }

        // Use target name as unique module ID
        ModuleData moduleData = new ModuleData(name, CMakeConstants.SYSTEM_ID, name, sourcePath, linkedProjectPath);
        DataNode<ModuleData> moduleNode = projectNode.createChild(ProjectKeys.MODULE, moduleData);

        // Source dirs from sources list
        Set<String> sourceDirs = new HashSet<>();
        JsonArray sources = target.getAsJsonArray("sources");
        if (sources != null) {
            for (JsonElement srcEl : sources) {
                String path = getStr(srcEl.getAsJsonObject(), "path");
                if (path != null) {
                    File srcFile = resolveRelative(path, sourcePath, buildPath);
                    if (srcFile != null && srcFile.getParentFile() != null) {
                        sourceDirs.add(srcFile.getParentFile().getAbsolutePath());
                    }
                }
            }
        }

        // Include dirs from compile groups
        List<String> includeDirs = new ArrayList<>();
        JsonArray compileGroups = target.getAsJsonArray("compileGroups");
        if (compileGroups != null) {
            for (JsonElement cgEl : compileGroups) {
                JsonArray includes = cgEl.getAsJsonObject().getAsJsonArray("includes");
                if (includes != null) {
                    for (JsonElement incEl : includes) {
                        String incPath = getStr(incEl.getAsJsonObject(), "path");
                        if (incPath != null) {
                            includeDirs.add(new File(incPath).getAbsolutePath());
                        }
                    }
                }
            }
        }

        // Single content root rooted at project source dir; add source/include dirs as source roots
        ContentRootData contentRoot = new ContentRootData(CMakeConstants.SYSTEM_ID, sourcePath);
        boolean anyAdded = false;
        for (String dir : sourceDirs) {
            contentRoot.storePath(ExternalSystemSourceType.SOURCE, dir);
            anyAdded = true;
        }
        for (String dir : includeDirs) {
            contentRoot.storePath(ExternalSystemSourceType.SOURCE, dir);
            anyAdded = true;
        }
        if (!anyAdded) {
            contentRoot.storePath(ExternalSystemSourceType.SOURCE, sourcePath);
        }
        moduleNode.createChild(ProjectKeys.CONTENT_ROOT, contentRoot);

        // Per-target build task
        projectNode.createChild(ProjectKeys.TASK,
            new TaskData(CMakeConstants.SYSTEM_ID, name, linkedProjectPath, CMakeLocalize.taskBuildTarget(name).get()));
    }

    // -------------------------------------------------------------------------
    // Fallback
    // -------------------------------------------------------------------------

    private static DataNode<ProjectData> fallbackResolve(File sourceDir, File cmakeListsFile, String linkedProjectPath) {
        String projectName = extractProjectName(cmakeListsFile);
        if (projectName == null) projectName = sourceDir.getName();
        String sourcePath = sourceDir.getAbsolutePath();

        ProjectData projectData = new ProjectData(CMakeConstants.SYSTEM_ID, projectName, sourcePath, linkedProjectPath);
        DataNode<ProjectData> projectNode = new DataNode<>(ProjectKeys.PROJECT, projectData, null);

        createRootModule(projectNode, projectName, sourcePath, linkedProjectPath);

        projectNode.createChild(ProjectKeys.TASK,
            new TaskData(CMakeConstants.SYSTEM_ID, "build", linkedProjectPath, CMakeLocalize.taskBuildAll().get()));
        projectNode.createChild(ProjectKeys.TASK,
            new TaskData(CMakeConstants.SYSTEM_ID, "clean", linkedProjectPath, CMakeLocalize.taskClean().get()));

        return projectNode;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    @Nullable
    private static String extractProjectName(File cmakeListsFile) {
        try {
            String content = Files.readString(cmakeListsFile.toPath());
            Matcher matcher = PROJECT_NAME_PATTERN.matcher(content);
            if (matcher.find()) return matcher.group(1);
        }
        catch (IOException ignored) {
        }
        return null;
    }

    private static JsonObject readJson(Gson gson, File file) throws ExternalSystemException {
        try (Reader reader = Files.newBufferedReader(file.toPath())) {
            return gson.fromJson(reader, JsonObject.class);
        }
        catch (IOException e) {
            throw new ExternalSystemException(CMakeLocalize.errorFailedReadFile(file.getName(), e.getMessage()).get());
        }
    }

    @Nullable
    private static String getStr(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        return (el != null && !el.isJsonNull()) ? el.getAsString() : null;
    }

    @Nullable
    private static File resolveRelative(String path, String sourceDir, String buildDir) {
        File f = new File(path);
        if (f.isAbsolute()) return f;
        File fromSource = new File(sourceDir, path);
        if (fromSource.exists()) return fromSource;
        File fromBuild = new File(buildDir, path);
        if (fromBuild.exists()) return fromBuild;
        return fromSource;
    }

    private static void notify(ExternalSystemTaskNotificationListener listener, ExternalSystemTaskId id, String message) {
        listener.onStatusChange(new ExternalSystemTaskNotificationEvent(id, message));
    }

    @Override
    public boolean cancelTask(ExternalSystemTaskId taskId, ExternalSystemTaskNotificationListener listener) {
        myCancelled = true;
        return true;
    }
}
