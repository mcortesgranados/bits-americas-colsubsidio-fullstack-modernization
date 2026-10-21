package co.com.bits.colsubsidio.subsidy.application;

import java.io.InputStream;

/**
 * Stream of a stored document together with the metadata required to build the HTTP response.
 *
 * <p>The stream is returned instead of a byte array so a 5 MB file is never loaded twice in memory: the
 * controller copies it directly to the response and closes it when the write finishes.
 *
 * @param stream      content of the file; the caller is responsible for closing it
 * @param fileName    sanitised original name, used for {@code Content-Disposition}
 * @param contentType MIME type reported by the client and accepted at upload time
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-21 11:20 GMT-5 Bogotá D.C. Colombia
 */
public record DocumentContent(InputStream stream, String fileName, String contentType) {
}
