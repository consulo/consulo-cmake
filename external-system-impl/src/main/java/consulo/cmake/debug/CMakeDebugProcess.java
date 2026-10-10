/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package consulo.cmake.debug;

import consulo.cmake.debug.breakpoint.CMakeScriptBreakpointType;
import consulo.cmake.localize.CMakeLocalize;
import consulo.component.ProcessCanceledException;
import consulo.execution.debug.XDebugSession;
import consulo.execution.debug.breakpoint.XLineBreakpointType;
import consulo.execution.debugger.dap.DAPDebugProcess;
import consulo.execution.debugger.dap.protocol.DAP;
import consulo.execution.debugger.dap.protocol.DAPFactory;
import consulo.execution.debugger.dap.protocol.DisconnectArguments;
import consulo.execution.debugger.dap.protocol.event.ExitedEvent;
import consulo.execution.ui.ExecutionConsole;
import consulo.execution.ui.console.ConsoleView;
import consulo.logging.Logger;
import consulo.process.ProcessHandler;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * DAP-based debug process for CMake script files (CMakeLists.txt, .cmake).
 * Requires CMake 3.27+ with built-in --debugger DAP server support.
 *
 * @author VISTALL
 */
public class CMakeDebugProcess extends DAPDebugProcess {
    private static final Logger LOG = Logger.getInstance(CMakeDebugProcess.class);

    private static final long CONNECT_TIMEOUT_MS = 30_000;

    private final ProcessHandler myCMakeProcess;
    private final String myPipeName;
    private final AtomicBoolean myDisconnected = new AtomicBoolean();

    @Nullable
    private volatile CMakeDebuggerConnection myConnection;

    public CMakeDebugProcess(@Nonnull XDebugSession session, @Nonnull ProcessHandler cmakeProcess, @Nonnull String pipeName) {
        super(session);
        myCMakeProcess = cmakeProcess;
        myPipeName = pipeName;
    }

    @Nonnull
    @Override
    protected XLineBreakpointType<?> getLineBreakpointType() {
        return CMakeScriptBreakpointType.getInstance();
    }

    @Override
    protected DAP createDAP(DAPFactory factory) {
        try {
            CMakeDebuggerConnection connection = CMakeDebuggerPipe.connect(myPipeName, myCMakeProcess, CONNECT_TIMEOUT_MS);
            myConnection = connection;
            return factory.createStreamDAP(connection.input(), connection.output());
        }
        catch (CMakeDebuggerExitedException e) {
            throw failStart(CMakeLocalize.debugErrorExited(e.getExitCode()).get(), e);
        }
        catch (IOException e) {
            throw failStart(CMakeLocalize.debugErrorPipeTimeout(myPipeName).get(), e);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProcessCanceledException(e);
        }
    }

    private ProcessCanceledException failStart(String message, Exception cause) {
        LOG.warn(message, cause);
        getSession().reportError(message);
        getSession().stop();
        return new ProcessCanceledException(cause);
    }

    @Override
    protected CompletableFuture<?> startDebuggee(DAP dap) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    protected String getAdapterId() {
        return "cmake";
    }

    @Nullable
    @Override
    protected ProcessHandler doGetProcessHandler() {
        return myCMakeProcess;
    }

    @Override
    public ExecutionConsole createConsole() {
        ExecutionConsole console = super.createConsole();
        if (console instanceof ConsoleView consoleView) {
            consoleView.attachToProcess(myCMakeProcess);
        }
        return console;
    }

    @Override
    protected void onExited(ExitedEvent event) {
    }

    @Override
    protected void onTerminated() {
        if (!myDisconnected.compareAndSet(false, true)) {
            return;
        }
        DAP dap = getDAP();
        DisconnectArguments arguments = new DisconnectArguments();
        arguments.terminateDebuggee = false;
        dap.disconnect(arguments).whenComplete((o, error) -> dap.close());
    }

    @Override
    protected void stopImpl() {
        if (myDisconnected.compareAndSet(false, true)) {
            super.stopImpl();
        }
        CMakeDebuggerConnection connection = myConnection;
        myConnection = null;
        if (connection != null) {
            try {
                connection.channel().close();
            }
            catch (IOException ignored) {
            }
        }
        if (!myCMakeProcess.isProcessTerminated()) {
            myCMakeProcess.destroyProcess();
        }
        CMakeDebuggerPipe.delete(myPipeName);
    }
}
