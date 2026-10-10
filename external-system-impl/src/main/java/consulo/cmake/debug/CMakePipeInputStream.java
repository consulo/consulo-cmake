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

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutionException;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
final class CMakePipeInputStream extends InputStream {
    private final Channel myChannel;

    CMakePipeInputStream(Channel channel) {
        myChannel = channel;
    }

    @Override
    public int read() throws IOException {
        byte[] buffer = new byte[1];
        int read = read(buffer, 0, 1);
        return read <= 0 ? -1 : buffer[0] & 0xFF;
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        if (length == 0) {
            return 0;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, offset, length);
        if (myChannel instanceof SocketChannel socketChannel) {
            return socketChannel.read(byteBuffer);
        }
        try {
            int read = ((AsynchronousFileChannel) myChannel).read(byteBuffer, 0).get();
            return read == 0 ? -1 : read;
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException();
        }
        catch (ExecutionException e) {
            throw new IOException(e.getCause());
        }
    }

    @Override
    public void close() throws IOException {
        myChannel.close();
    }
}
