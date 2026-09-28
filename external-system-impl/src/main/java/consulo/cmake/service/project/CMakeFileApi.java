package consulo.cmake.service.project;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import consulo.logging.Logger;
import jakarta.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 */
public final class CMakeFileApi {
    private static final Logger LOG = Logger.getInstance(CMakeFileApi.class);

    public static final String CODEMODEL_KIND = "codemodel";
    public static final String CMAKE_FILES_KIND = "cmakeFiles";

    private static final String API_DIR = ".cmake/api/v1";
    private static final String[] QUERIES = {"codemodel-v2", "cmakeFiles-v1"};

    private CMakeFileApi() {
    }

    public static void writeQueryFiles(File buildDir) throws IOException {
        Path queryDir = buildDir.toPath().resolve(API_DIR + "/query");
        Files.createDirectories(queryDir);
        for (String query : QUERIES) {
            Path queryFile = queryDir.resolve(query);
            if (!Files.exists(queryFile)) {
                Files.createFile(queryFile);
            }
        }
    }

    public static File getReplyDir(File buildDir) {
        return buildDir.toPath().resolve(API_DIR + "/reply").toFile();
    }

    @Nullable
    public static File findLatestIndex(File replyDir) {
        File[] indexFiles = replyDir.listFiles((d, name) -> name.startsWith("index-") && name.endsWith(".json"));
        if (indexFiles == null || indexFiles.length == 0) {
            return null;
        }

        File indexFile = indexFiles[0];
        for (File f : indexFiles) {
            if (f.lastModified() > indexFile.lastModified()) {
                indexFile = f;
            }
        }
        return indexFile;
    }

    @Nullable
    public static String findObjectJsonFile(JsonObject index, String kind) {
        JsonArray objects = index.getAsJsonArray("objects");
        if (objects == null) {
            return null;
        }
        for (JsonElement obj : objects) {
            JsonObject o = obj.getAsJsonObject();
            if (kind.equals(getString(o, "kind"))) {
                return getString(o, "jsonFile");
            }
        }
        return null;
    }

    @Nullable
    public static JsonObject readJson(Gson gson, File file) throws IOException {
        try (Reader reader = Files.newBufferedReader(file.toPath())) {
            return gson.fromJson(reader, JsonObject.class);
        }
    }

    @Nullable
    public static String getString(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        return (el != null && !el.isJsonNull()) ? el.getAsString() : null;
    }

    public static List<Path> readInputFiles(File sourceDir, File buildDir) {
        File replyDir = getReplyDir(buildDir);
        File indexFile = findLatestIndex(replyDir);
        if (indexFile == null) {
            return List.of();
        }

        try {
            Gson gson = new Gson();
            JsonObject index = readJson(gson, indexFile);
            String cmakeFilesJson = index == null ? null : findObjectJsonFile(index, CMAKE_FILES_KIND);
            if (cmakeFilesJson == null) {
                return List.of();
            }

            JsonObject cmakeFiles = readJson(gson, new File(replyDir, cmakeFilesJson));
            if (cmakeFiles == null) {
                return List.of();
            }

            Path source = sourceDir.toPath();
            JsonObject paths = cmakeFiles.getAsJsonObject("paths");
            String replySource = paths == null ? null : getString(paths, "source");
            if (replySource != null) {
                source = Path.of(replySource);
            }

            JsonArray inputs = cmakeFiles.getAsJsonArray("inputs");
            if (inputs == null) {
                return List.of();
            }

            List<Path> result = new ArrayList<>();
            for (JsonElement inputElement : inputs) {
                JsonObject input = inputElement.getAsJsonObject();
                if (isTrue(input, "isGenerated") || isTrue(input, "isCMake")) {
                    continue;
                }
                String path = getString(input, "path");
                if (path != null) {
                    result.add(source.resolve(path).normalize());
                }
            }
            return result;
        }
        catch (Exception e) {
            LOG.warn("Cannot read CMake file API reply in " + replyDir, e);
            return List.of();
        }
    }

    private static boolean isTrue(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        return el != null && el.isJsonPrimitive() && el.getAsBoolean();
    }
}
