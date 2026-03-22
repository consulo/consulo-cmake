package consulo.cmake.debug.breakpoint;

import consulo.execution.debug.breakpoint.XBreakpointProperties;

/**
 * Breakpoint properties for CMake script line breakpoints.
 *
 * @author VISTALL
 */
public class CMakeScriptBreakpointProperties extends XBreakpointProperties<CMakeScriptBreakpointProperties> {
    @Override
    public CMakeScriptBreakpointProperties getState() {
        return this;
    }

    @Override
    public void loadState(CMakeScriptBreakpointProperties state) {
    }
}
