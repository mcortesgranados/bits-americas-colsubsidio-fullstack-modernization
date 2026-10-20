package co.com.bits.colsubsidio.common.api;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Stable pagination envelope returned by every collection endpoint. The record exists so that the HTTP
 * contract does not expose Spring Data's {@link Page} directly: the serialized shape stays under the
 * control of this project and can evolve independently from the persistence framework. Clients receive
 * the content, the page metadata and two boolean shortcuts that allow a portal to enable or disable
 * the "next" and "previous" controls without additional arithmetic. Represents the PageResponse
 * component of the api module. It participates in the demonstration platform for affiliates, companies
 * and subsidy requests, where the behaviour is driven by explicit business rules rather than by
 * implicit framework magic. The type keeps a single responsibility so that it can be tested in
 * isolation and replaced or extended without affecting unrelated modules. This documentation is
 * generated to the project standard: it states the contract of the type, the layer it belongs to and
 * the expectations that callers may rely on. Personal data is never logged by this component, and any
 * value returned to a client is masked by the DTO layer before serialization.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {

    /**
     * Creates a new {@code PageResponse} from a Spring Data page.
     *
     * <p>The conversion is a pure function of its argument: it copies the content list and the
     * pagination metadata without retaining a reference to the {@link Page} instance, which keeps the
     * transport representation independent from the persistence context that produced it. The method is
     * static because it does not depend on any collaborator and must be usable from unit tests without a
     * Spring context.
     *
     * <p>Read paths never flush the persistence context, and the returned list is the one already
     * materialised by the repository query, so no additional database round trip is triggered by the
     * mapping. Errors raised while reading the page are translated into RFC 7807 problem details by the
     * central handler, preserving the correlation identifier of the request.
     *
     * @param page page of results supplied by the repository, including the pagination metadata required
     * by the client
     *
     * @return the transport representation of the page, never {@code null}
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }

    /**
     * Creates a new {@code PageResponse} after mapping every element of the source page.
     *
     * <p>The method centralises the conversion from entities to DTOs so that controllers do not repeat
     * the same {@code page.map(...)} expression, and so the pagination metadata is preserved exactly as
     * the repository reported it. The mapper is applied element by element; an exception thrown by the
     * mapper aborts the conversion and propagates to the caller, where the central handler turns it into
     * a problem detail with the correlation identifier of the request.
     *
     * <p>Because the conversion happens in memory over an already materialised page, the operation does
     * not issue additional queries and does not keep references to the entities once the response has
     * been serialized. This keeps the memory footprint bounded by the page size, which the API caps
     * through its pagination configuration.
     *
     * @param page   page of results supplied by the repository, including the pagination metadata
     *
     * @param mapper function applied to each element to obtain the representation expected by the client
     *
     * @return a page of mapped elements with the original pagination metadata, never {@code null}
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public static <T, R> PageResponse<R> from(Page<T> page, java.util.function.Function<T, R> mapper) {
        return from(page.map(mapper));
    }
}
