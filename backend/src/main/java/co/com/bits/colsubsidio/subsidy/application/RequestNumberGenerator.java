package co.com.bits.colsubsidio.subsidy.application;

import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequest;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequestRepository;
import java.time.Year;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Builds the readable radicado of a new subsidy request: {@code SOL-<year>-<sequence>}.
 *
 * <p>The sequence restarts every year and is derived from the highest radicado already stored for the
 * current series. Reading the maximum instead of counting rows means a deleted request never produces a
 * duplicate number, and the zero-padded format keeps the lexicographic order of the column equal to the
 * numeric order of the sequence.
 *
 * <p>Two environment facts are worth stating: the year is taken in the Colombian time zone, so a request
 * registered late on 31 December does not jump to the next series because the server runs in UTC; and
 * {@link #next()} is serialised inside the JVM. That is enough for a single-instance demonstration but not
 * for a cluster: with several instances the unique constraint on {@code request_number} is the real
 * guarantee, and the next step would be a database sequence or a table row locked with
 * {@code SELECT ... FOR UPDATE}.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-21 10:40 GMT-5 Bogotá D.C. Colombia
 */
@Component
public class RequestNumberGenerator {

    private static final Logger log = LoggerFactory.getLogger(RequestNumberGenerator.class);
    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private static final String PREFIX = "SOL-";
    private static final int SEQUENCE_DIGITS = 4;

    private final SubsidyRequestRepository requests;

    /**
     * Receives the repository used to read the current head of the series.
     *
     * @param requests repository over the subsidy requests; never {@code null}
     */
    public RequestNumberGenerator(SubsidyRequestRepository requests) {
        this.requests = requests;
    }

    /**
     * Returns the next free radicado of the current year.
     *
     * <p>The method is synchronised because the read-then-increment sequence is not atomic by itself; the
     * lock is held only for the duration of one indexed query, so contention is negligible in the expected
     * volume. A malformed predecessor —a value that does not follow the format— is logged and ignored, so
     * one bad row imported by hand cannot stop the registration flow.
     *
     * @return a radicado such as {@code SOL-2026-0007}, never {@code null}
     */
    public synchronized String next() {
        String prefix = PREFIX + Year.now(BOGOTA).getValue() + "-";
        int nextSequence = requests.findTopByRequestNumberStartingWithOrderByRequestNumberDesc(prefix)
                .map(SubsidyRequest::getRequestNumber)
                .map(number -> sequenceOf(number, prefix))
                .orElse(0) + 1;
        return prefix + String.format("%0" + SEQUENCE_DIGITS + "d", nextSequence);
    }

    /**
     * Extracts the numeric sequence from a radicado, returning zero when it cannot be interpreted.
     *
     * @param requestNumber stored radicado, already known to start with the prefix
     * @param prefix        prefix of the series
     * @return the sequence, or zero when the suffix is not a number
     */
    private int sequenceOf(String requestNumber, String prefix) {
        String suffix = requestNumber.substring(prefix.length());
        try {
            return Integer.parseInt(suffix);
        } catch (NumberFormatException ex) {
            log.warn("Ignoring malformed radicado in the sequence lookup: {}", requestNumber);
            return 0;
        }
    }
}
