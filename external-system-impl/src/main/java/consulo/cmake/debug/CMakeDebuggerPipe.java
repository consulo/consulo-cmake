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

import consulo.platform.Platform;
import consulo.process.ProcessHandler;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
public final class CMakeDebuggerPipe {
    private static final long POLL_INTERVAL_MS = 50;

    private CMakeDebuggerPipe() {
    }

    public static String createName() {
        String id = UUID.randomUUID().toString().substring(0, 8);
        Platform platform = Platform.current();
        if (platform.os().isWindows()) {
            return "\\\\.\\pipe\\consulo-cmake-" + id;
        }
        String runtimeDir = platform.os().getEnvironmentVariable("XDG_RUNTIME_DIR");
        Path directory = runtimeDir != null && !runtimeDir.isBlank() && Files.isDirectory(Path.of(runtimeDir)) ? Path.of(runtimeDir) : Path.of("/tmp");
        return directory.resolve("consulo-cmake-" + id + ".sock").toString();
    }

    public static CMakeDebuggerConnection connect(String name, ProcessHandler cmakeProcess, long timeoutMs) throws IOException, InterruptedException {
        long deadline = System.nanoTime() + timeoutMs * 1_000_000L;
        IOException lastError = null;
        while (System.nanoTime() < deadline) {
            if (cmakeProcess.isProcessTerminated()) {
                Integer exitCode = cmakeProcess.getExitCode();
                throw new CMakeDebuggerExitedException(exitCode == null ? -1 : exitCode);
            }
            try {
                CMakeDebuggerConnection connection = tryConnect(name);
                if (connection != null) {
                    return connection;
                }
            }
            catch (IOException e) {
                lastError = e;
            }
            Thread.sleep(POLL_INTERVAL_MS);
        }
        throw lastError != null ? lastError : new IOException("Timed out waiting for " + name);
    }

    public static void delete(String name) {
        if (Platform.current().os().isWindows()) {
            return;
        }
        try {
            Files.deleteIfExists(Path.of(name));
        }
        catch (IOException ignored) {
        }
    }

    private static CMakeDebuggerConnection tryConnect(String name) throws IOException {
        if (Platform.current().os().isWindows()) {
            AsynchronousFileChannel channel = AsynchronousFileChannel.open(Path.of(name), StandardOpenOption.READ, StandardOpenOption.WRITE);
            return new CMakeDebuggerConnection(new CMakePipeInputStream(channel), new CMakePipeOutputStream(channel), channel);
        }
        Path socket = Path.of(name);
        if (!Files.exists(socket)) {
            return null;
        }
        SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX);
        try {
            channel.connect(UnixDomainSocketAddress.of(socket));
        }
        catch (IOException e) {
            channel.close();
            throw e;
        }
        return new CMakeDebuggerConnection(new CMakePipeInputStream(channel), new CMakePipeOutputStream(channel), channel);
    }
}
