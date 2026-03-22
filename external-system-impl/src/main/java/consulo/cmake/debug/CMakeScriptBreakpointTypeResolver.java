package consulo.cmake.debug;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.cmake.debug.breakpoint.CMakeScriptBreakpointType;
import consulo.execution.debug.breakpoint.XLineBreakpointType;
import consulo.execution.debug.breakpoint.XLineBreakpointTypeResolver;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import consulo.language.file.FileTypeManager;
import consulo.virtualFileSystem.fileType.FileType;
import jakarta.annotation.Nullable;

/**
 * Resolves the CMake script breakpoint type for .cmake and CMakeLists.txt files.
 *
 * @author VISTALL
 */
@ExtensionImpl
public class CMakeScriptBreakpointTypeResolver implements XLineBreakpointTypeResolver {
    @RequiredReadAction
    @Nullable
    @Override
    public XLineBreakpointType<?> resolveBreakpointType(Project project, VirtualFile file, int line) {
        String name = file.getName();
        String ext = file.getExtension();
        if ("CMakeLists.txt".equals(name) || "cmake".equalsIgnoreCase(ext)) {
            return CMakeScriptBreakpointType.getInstance();
        }
        return null;
    }

    @Override
    public FileType getFileType() {
        // Resolved at runtime after CMakeFileType is registered by language-impl
        return FileTypeManager.getInstance().getFileTypeByExtension("cmake");
    }
}
