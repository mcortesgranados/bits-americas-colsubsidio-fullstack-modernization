package co.com.bits.colsubsidio.subsidy.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequest;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequestRepository;
import java.time.Year;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link RequestNumberGenerator}.
 *
 * <p>The tests pin the two properties the unique constraint relies on: the series starts at one for a new
 * year and advances by one over the highest stored radicado. The year is computed the same way the
 * generator does —Colombian time zone— so the test does not fail on the last hours of 31 December.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-21 10:40 GMT-5 Bogotá D.C. Colombia
 */
@ExtendWith(MockitoExtension.class)
class RequestNumberGeneratorTest {

    private static final String PREFIX =
            "SOL-" + Year.now(ZoneId.of("America/Bogota")).getValue() + "-";

    @Mock
    private SubsidyRequestRepository requests;

    @InjectMocks
    private RequestNumberGenerator generator;

    @Test
    @DisplayName("starts the yearly series at one when there are no requests")
    void startsSeries() {
        when(requests.findTopByRequestNumberStartingWithOrderByRequestNumberDesc(PREFIX))
                .thenReturn(Optional.empty());

        assertThat(generator.next()).isEqualTo(PREFIX + "0001");
    }

    @Test
    @DisplayName("advances the sequence over the highest radicado of the year")
    void advancesSequence() {
        SubsidyRequest previous = requestWith(PREFIX + "0009");
        when(requests.findTopByRequestNumberStartingWithOrderByRequestNumberDesc(PREFIX))
                .thenReturn(Optional.of(previous));

        assertThat(generator.next()).isEqualTo(PREFIX + "0010");
    }

    @Test
    @DisplayName("ignores a malformed radicado instead of failing the registration")
    void ignoresMalformedRadicado() {
        SubsidyRequest malformed = requestWith(PREFIX + "ABC");
        when(requests.findTopByRequestNumberStartingWithOrderByRequestNumberDesc(PREFIX))
                .thenReturn(Optional.of(malformed));

        assertThat(generator.next()).isEqualTo(PREFIX + "0001");
    }

    private SubsidyRequest requestWith(String requestNumber) {
        SubsidyRequest request = mock(SubsidyRequest.class);
        when(request.getRequestNumber()).thenReturn(requestNumber);
        return request;
    }
}
