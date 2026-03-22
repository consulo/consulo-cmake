package consulo.cmake.debug.breakpoint;

import consulo.annotation.component.ExtensionImpl;
import consulo.execution.debug.breakpoint.XLineBreakpointType;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;

/**
 * Line breakpoint type for CMake script files (.cmake and CMakeLists.txt).
 * Used with the cmake --debugger DAP interface (requires CMake 3.27+).
 *
 * @author VISTALL
 */
@ExtensionImpl
public class CMakeScriptBreakpointType extends XLineBreakpointType<CMakeScriptBreakpointProperties> {
    public static CMakeScriptBreakpointType getInstance() {
        return EXTENSION_POINT_NAME.findExtensionOrFail(CMakeScriptBreakpointType.class);
    }

    @Inject
    protected CMakeScriptBreakpointType() {
        super("CMakeScriptBreakpoint", "CMake Script Breakpoint");
    }

    @Nullable
    @Override
    public CMakeScriptBreakpointProperties createBreakpointProperties(@Nonnull VirtualFile file, int line) {
        return new CMakeScriptBreakpointProperties();
    }
}
