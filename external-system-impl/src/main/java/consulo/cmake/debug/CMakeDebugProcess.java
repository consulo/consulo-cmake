package consulo.cmake.debug;

import consulo.cmake.debug.breakpoint.CMakeScriptBreakpointType;
import consulo.component.ProcessCanceledException;
import consulo.execution.debug.XDebugSession;
import consulo.execution.debug.breakpoint.XLineBreakpointType;
import consulo.execution.debugger.dap.DAPDebugProcess;
import consulo.execution.debugger.dap.protocol.DAP;
import consulo.execution.debugger.dap.protocol.DAPFactory;
import consulo.execution.debugger.dap.protocol.LaunchRequestArguments;
import consulo.platform.Platform;
import jakarta.annotation.Nonnull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAP-based debug process for CMake script files (CMakeLists.txt, .cmake).
 * Requires CMake 3.27+ with built-in --debugger DAP server support.
 *
 * @author VISTALL
 */
public class CMakeDebugProcess extends DAPDebugProcess {
    private final int myPort;
    private final String mySourceDirectory;
    private final String myBuildDirectory;
    private final List<String> myExtraOptions;

    private volatile Process myCmakeProcess;

    public CMakeDebugProcess(@Nonnull XDebugSession session,
                             int port,
                             @Nonnull String sourceDirectory,
                             @Nonnull String buildDirectory,
                             @Nonnull List<String> extraOptions) {
        super(session);
        myPort = port;
        mySourceDirectory = sourceDirectory;
        myBuildDirectory = buildDirectory;
        myExtraOptions = extraOptions;
    }

    @Nonnull
    @Override
    protected XLineBreakpointType<?> getLineBreakpointType() {
        return CMakeScriptBreakpointType.getInstance();
    }

    @Override
    protected DAP createDAP(DAPFactory factory) {
        startCMake();
        return factory.createSocketDAP("localhost", myPort);
    }

    private void startCMake() {
        List<String> command = new ArrayList<>();
        command.add("cmake");
        command.add("--debugger");
        command.add("--debugger-port");
        command.add(String.valueOf(myPort));
        command.add("-S");
        command.add(mySourceDirectory);
        command.add("-B");
        command.add(myBuildDirectory);
        command.addAll(myExtraOptions);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        try {
            myCmakeProcess = pb.start();
            // Give cmake a moment to open the debugger socket
            Thread.sleep(300);
        }
        catch (IOException e) {
            throw new ProcessCanceledException(
                new RuntimeException("Failed to start cmake debugger: " + e.getMessage(), e));
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProcessCanceledException(e);
        }
    }

    @Override
    protected String getAdapterId() {
        return "cmake";
    }

    @Override
    protected LaunchRequestArguments createLaunchRequestArguments() {
        // cmake --debugger starts automatically; send a minimal launch to satisfy the DAP handshake
        LaunchRequestArguments launch = new LaunchRequestArguments();
        launch.env = Platform.current().os().environmentVariables();
        return launch;
    }

    @Override
    protected void stopImpl() {
        super.stopImpl();
        Process process = myCmakeProcess;
        if (process != null) {
            process.destroyForcibly();
            myCmakeProcess = null;
        }
    }
}
