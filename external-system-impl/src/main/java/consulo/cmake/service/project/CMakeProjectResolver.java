package consulo.cmake.service.project;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import consulo.cmake.externalSystem.impl.CMakeConstants;
import consulo.cmake.externalSystem.model.CMakeCompileData;
import consulo.cmake.localize.CMakeLocalize;
import consulo.cmake.service.process.CMakeCommand;
import consulo.cmake.service.process.CMakeProcessRunner;
import consulo.cmake.setting.CMakeExecutionSettings;
import consulo.cmake.setting.CMakeProjectSettings;
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
import consulo.process.ExecutionException;
import consulo.process.cmd.GeneralCommandLine;

import consulo.util.io.FileUtil;
import jakarta.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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

    private final CMakeProcessRunner myRunner = new CMakeProcessRunner();

    @Nullable
    @Override
    public DataNode<ProjectData> resolveProjectInfo(ExternalSystemTaskId id,
                                                    String projectPath,
                                                    boolean isPreviewMode,
                                                    @Nullable CMakeExecutionSettings settings,
                                                    ExternalSystemTaskNotificationListener listener)
        throws ExternalSystemException, IllegalArgumentException, IllegalStateException {
        File sourceDir = CMakeProjectPaths.getSourceDir(projectPath);
        File buildDir = CMakeProjectPaths.getBuildDir(sourceDir, settings != null ? settings.getBuildDirectory() : null);

        File cmakeListsFile = new File(sourceDir, CMakeConstants.CMAKE_LISTS_TXT);
        if (!cmakeListsFile.exists()) {
            throw new IllegalArgumentException(CMakeLocalize.errorNoCmakeLists(sourceDir.getAbsolutePath()).get());
        }

        notify(listener, id, CMakeLocalize.statusWritingQuery().get());
        writeQueryFile(buildDir);

        if (!isPreviewMode) {
            notify(listener, id, CMakeLocalize.statusConfiguring().get());
            runCMake(sourceDir, buildDir, getBuildType(settings), listener, id);
        }

        notify(listener, id, CMakeLocalize.statusReadingStructure().get());
        File replyDir = CMakeFileApi.getReplyDir(buildDir);
        if (!replyDir.exists()) {
            return fallbackResolve(sourceDir, cmakeListsFile, projectPath);
        }

        return parseFileApiReply(sourceDir, buildDir, replyDir, projectPath, getBuildType(settings));
    }

    private static String getBuildType(@Nullable CMakeExecutionSettings settings) {
        return settings != null ? settings.getBuildType() : CMakeProjectSettings.DEFAULT_BUILD_TYPE;
    }

    // -------------------------------------------------------------------------
    // File API query
    // -------------------------------------------------------------------------

    private static void writeQueryFile(File buildDir) throws ExternalSystemException {
        try {
            CMakeFileApi.writeQueryFiles(buildDir);
        }
        catch (IOException e) {
            throw new ExternalSystemException(CMakeLocalize.errorFailedWriteQuery(e.getMessage()).get());
        }
    }

    // -------------------------------------------------------------------------
    // cmake invocation
    // -------------------------------------------------------------------------

    private void runCMake(File sourceDir,
                          File buildDir,
                          String buildType,
                          ExternalSystemTaskNotificationListener listener,
                          ExternalSystemTaskId id) throws ExternalSystemException {
        try {
            Files.createDirectories(buildDir.toPath());
        }
        catch (IOException e) {
            throw new ExternalSystemException(CMakeLocalize.errorCannotCreateBuildDir(e.getMessage()).get());
        }

        GeneralCommandLine commandLine = CMakeCommand.configure(sourceDir, buildDir, buildType);

        int exitCode;
        try {
            exitCode = myRunner.run(commandLine, id, listener);
        }
        catch (ExecutionException e) {
            throw new ExternalSystemException(CMakeLocalize.errorFailedRunCmake(e.getMessage()).get());
        }
        if (exitCode == CMakeProcessRunner.CANCELLED_EXIT_CODE) {
            throw new ExternalSystemException(CMakeLocalize.errorCmakeCancelled().get());
        }
        if (exitCode != 0) {
            throw new ExternalSystemException(CMakeLocalize.errorCmakeExitCode(exitCode).get());
        }
    }

    // -------------------------------------------------------------------------
    // CMake File API reply parsing
    // -------------------------------------------------------------------------

    private DataNode<ProjectData> parseFileApiReply(File sourceDir, File buildDir, File replyDir, String linkedProjectPath, String buildType)
        throws ExternalSystemException {
        Gson gson = new Gson();

        File indexFile = CMakeFileApi.findLatestIndex(replyDir);
        if (indexFile == null) {
            return fallbackResolve(sourceDir, new File(sourceDir, CMakeConstants.CMAKE_LISTS_TXT), linkedProjectPath);
        }

        JsonObject index = readJson(gson, indexFile);

        String codemodelFile = CMakeFileApi.findObjectJsonFile(index, CMakeFileApi.CODEMODEL_KIND);
        if (codemodelFile == null) {
            return fallbackResolve(sourceDir, new File(sourceDir, CMakeConstants.CMAKE_LISTS_TXT), linkedProjectPath);
        }

        JsonObject codemodel = readJson(gson, new File(replyDir, codemodelFile));

        String projectName = null;
        JsonArray configurations = codemodel.getAsJsonArray("configurations");
        JsonObject configuration = selectConfiguration(configurations, buildType);
        if (configuration != null) {
            JsonArray projects = configuration.getAsJsonArray("projects");
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

        // Always-available build/clean tasks
        projectNode.createChild(ProjectKeys.TASK,
            new TaskData(CMakeConstants.SYSTEM_ID, "build", linkedProjectPath, CMakeLocalize.taskBuildAll().get()));
        projectNode.createChild(ProjectKeys.TASK,
            new TaskData(CMakeConstants.SYSTEM_ID, "clean", linkedProjectPath, CMakeLocalize.taskClean().get()));

        DataNode<ModuleData> rootNode = createRootModule(projectNode, projectName, sourcePath, linkedProjectPath);
        Set<String> sourceRoots = new LinkedHashSet<>();
        if (configuration != null) {
            JsonArray targets = configuration.getAsJsonArray("targets");
            if (targets != null) {
                for (JsonElement targetEl : targets) {
                    String targetJsonFile = getStr(targetEl.getAsJsonObject(), "jsonFile");
                    if (targetJsonFile == null) continue;
                    parseTarget(gson, new File(replyDir, targetJsonFile), sourcePath, buildDir.getAbsolutePath(),
                        projectNode, rootNode, linkedProjectPath, sourceRoots);
                }
            }
        }

        ContentRootData contentRoot = new ContentRootData(CMakeConstants.SYSTEM_ID, sourcePath);
        for (String dir : sourceRoots) {
            contentRoot.storePath(ExternalSystemSourceType.SOURCE, dir);
        }
        if (sourceRoots.isEmpty()) {
            contentRoot.storePath(ExternalSystemSourceType.SOURCE, sourcePath);
        }
        rootNode.createChild(ProjectKeys.CONTENT_ROOT, contentRoot);

        return projectNode;
    }

    private static DataNode<ModuleData> createRootModule(DataNode<ProjectData> projectNode, String projectName,
                                                         String sourcePath, String linkedProjectPath) {
        ModuleData rootData = new ModuleData(projectName, CMakeConstants.SYSTEM_ID, projectName, sourcePath, linkedProjectPath);
        return projectNode.createChild(ProjectKeys.MODULE, rootData);
    }

    private void parseTarget(Gson gson, File targetFile, String sourcePath, String buildPath,
                             DataNode<ProjectData> projectNode, DataNode<ModuleData> rootNode, String linkedProjectPath,
                             Set<String> sourceRoots)
        throws ExternalSystemException {
        if (!targetFile.exists()) return;

        JsonObject target = readJson(gson, targetFile);
        String name = getStr(target, "name");
        String type = getStr(target, "type");
        if (name == null || type == null || SKIP_TARGET_TYPES.contains(type)) {
            return;
        }

        rootNode.createChild(CMakeTargetData.KEY, new CMakeTargetData(name, type, findArtifact(target, buildPath), buildPath));

        JsonArray sources = target.getAsJsonArray("sources");
        if (sources != null) {
            for (JsonElement srcEl : sources) {
                String path = getStr(srcEl.getAsJsonObject(), "path");
                if (path != null) {
                    File srcFile = resolveRelative(path, sourcePath, buildPath);
                    if (srcFile != null && srcFile.getParentFile() != null) {
                        addSourceRoot(sourceRoots, sourcePath, srcFile.getParentFile());
                    }
                }
            }
        }

        JsonArray compileGroups = target.getAsJsonArray("compileGroups");
        if (compileGroups != null) {
            for (JsonElement cgEl : compileGroups) {
                JsonObject compileGroup = cgEl.getAsJsonObject();
                List<String> includePaths = new ArrayList<>();
                List<String> systemIncludePaths = new ArrayList<>();
                JsonArray includes = compileGroup.getAsJsonArray("includes");
                if (includes != null) {
                    for (JsonElement incEl : includes) {
                        JsonObject include = incEl.getAsJsonObject();
                        String incPath = getStr(include, "path");
                        if (incPath != null) {
                            addSourceRoot(sourceRoots, sourcePath, new File(incPath));
                            JsonElement isSystem = include.get("isSystem");
                            if (isSystem != null && isSystem.getAsBoolean()) {
                                systemIncludePaths.add(incPath);
                            }
                            else {
                                includePaths.add(incPath);
                            }
                        }
                    }
                }

                String language = getStr(compileGroup, "language");
                if (language != null) {
                    JsonObject languageStandard = compileGroup.getAsJsonObject("languageStandard");
                    String standard = languageStandard == null ? null : getStr(languageStandard, "standard");
                    rootNode.createChild(CMakeCompileData.KEY,
                        new CMakeCompileData(name, language, includePaths, systemIncludePaths, readDefines(compileGroup), standard));
                }
            }
        }

        projectNode.createChild(ProjectKeys.TASK,
            new TaskData(CMakeConstants.SYSTEM_ID, name, linkedProjectPath, CMakeLocalize.taskBuildTarget(name).get()));
    }

    private static Map<String, String> readDefines(JsonObject compileGroup) {
        Map<String, String> defines = new LinkedHashMap<>();
        JsonArray array = compileGroup.getAsJsonArray("defines");
        if (array == null) {
            return defines;
        }
        for (JsonElement element : array) {
            String define = getStr(element.getAsJsonObject(), "define");
            if (define == null || define.isEmpty()) {
                continue;
            }
            int eq = define.indexOf('=');
            if (eq < 0) {
                defines.put(define, "");
            }
            else {
                defines.put(define.substring(0, eq), define.substring(eq + 1));
            }
        }
        return defines;
    }

    private static void addSourceRoot(Set<String> sourceRoots, String sourcePath, File directory) {
        String path = directory.getAbsolutePath();
        if (FileUtil.isAncestor(sourcePath, path, false)) {
            sourceRoots.add(path);
        }
    }

    @Nullable
    private static JsonObject selectConfiguration(@Nullable JsonArray configurations, String buildType) {
        if (configurations == null || configurations.isEmpty()) {
            return null;
        }
        for (JsonElement element : configurations) {
            JsonObject configuration = element.getAsJsonObject();
            if (buildType.equalsIgnoreCase(getStr(configuration, "name"))) {
                return configuration;
            }
        }
        return configurations.get(0).getAsJsonObject();
    }

    @Nullable
    private static String findArtifact(JsonObject target, String buildPath) {
        JsonArray artifacts = target.getAsJsonArray("artifacts");
        if (artifacts == null || artifacts.isEmpty()) {
            return null;
        }
        String path = getStr(artifacts.get(0).getAsJsonObject(), "path");
        if (path == null) {
            return null;
        }
        File file = new File(path);
        return (file.isAbsolute() ? file : new File(buildPath, path)).getAbsolutePath();
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

        DataNode<ModuleData> rootNode = createRootModule(projectNode, projectName, sourcePath, linkedProjectPath);
        rootNode.createChild(ProjectKeys.CONTENT_ROOT, new ContentRootData(CMakeConstants.SYSTEM_ID, sourcePath));

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
        try {
            JsonObject json = CMakeFileApi.readJson(gson, file);
            if (json == null) {
                throw new IOException("empty");
            }
            return json;
        }
        catch (IOException e) {
            throw new ExternalSystemException(CMakeLocalize.errorFailedReadFile(file.getName(), e.getMessage()).get());
        }
    }

    @Nullable
    private static String getStr(JsonObject obj, String key) {
        return CMakeFileApi.getString(obj, key);
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
        myRunner.cancel();
        return true;
    }
}
