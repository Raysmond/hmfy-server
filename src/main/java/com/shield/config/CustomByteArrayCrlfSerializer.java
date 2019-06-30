package com.shield.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.ip.tcp.serializer.AbstractPooledBufferByteArraySerializer;
import org.springframework.integration.ip.tcp.serializer.SoftEndOfStreamException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Reads data in an InputStream to a byte[]; data must be terminated by \r\n
 * (not included in resulting byte[]).
 * Writes a byte[] to an OutputStream and adds \r\n.
 *
 * @author Gary Russell
 * @since 2.0
 */
@Slf4j
public class CustomByteArrayCrlfSerializer extends AbstractPooledBufferByteArraySerializer {

    /**
     * A single reusable instance.
     */
    public static final CustomByteArrayCrlfSerializer INSTANCE = new CustomByteArrayCrlfSerializer();

    private static final byte[] CRLF = "\r\n".getBytes();

    /**
     * Reads the data in the inputStream to a byte[]. Data must be terminated
     * by CRLF (\r\n). Throws a {@link SoftEndOfStreamException} if the stream
     * is closed immediately after the \r\n (i.e. no data is in the process of
     * being read).
     */
    @Override
    public byte[] doDeserialize(InputStream inputStream, byte[] buffer) throws IOException {
        int n = this.fillToCrLf(inputStream, buffer);
        return this.copyToSizedArray(buffer, n);
    }

    public int fillToCrLf(InputStream inputStream, byte[] buffer) throws IOException {
        int n = 0;
        int bite;
        if (logger.isDebugEnabled()) {
            logger.debug("Available to read: " + inputStream.available());
        }
        boolean endsWithJson = false;
        try {
            while (true) {
                bite = inputStream.read();
                if (bite < 0 && n == 0) {
                    throw new SoftEndOfStreamException("Stream closed between payloads");
                }
                checkClosure(bite);
                if (n > 0 && bite == '\n' && buffer[n - 1] == '\r') {
                    break;
                }
                if (n > 0 && bite == '}') {
                    buffer[n++] = (byte) bite;
                    endsWithJson = true;
                    break;
                }
                buffer[n++] = (byte) bite;
                if (n >= getMaxMessageSize()) {
                    //log.info("current msg: {}", new String(Arrays.copyOfRange(buffer, 0, n)));
                    //log.info("bytes: {}", Arrays.copyOfRange(buffer, 0, n));
                    throw new IOException("CRLF not found before max message length: " + getMaxMessageSize());
                }
            }
            if (!endsWithJson) {
                return n - 1; // trim \r
            } else {
                return n;
            }
        } catch (SoftEndOfStreamException e) {
            throw e;
        } catch (IOException e) {
            publishEvent(e, buffer, n);
            throw e;
        } catch (RuntimeException e) {
            publishEvent(e, buffer, n);
            throw e;
        }
    }

    /**
     * Writes the byte[] to the stream and appends \r\n.
     */
    @Override
    public void serialize(byte[] bytes, OutputStream outputStream) throws IOException {
        outputStream.write(bytes);
        outputStream.write(CRLF);
    }

}
