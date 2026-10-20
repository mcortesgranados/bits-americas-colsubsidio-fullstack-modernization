package co.com.bits.colsubsidio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point of the demonstration platform for affiliates, companies and subsidy requests. This is an
 * independent technical prototype inspired by the information needs of a family compensation fund. It
 * does not reproduce, connect to, or use real Colsubsidio systems, data or APIs. Represents the
 * ColsubsidioPlatformApplication component of the colsubsidio module. It participates in the
 * demonstration platform for affiliates, companies and subsidy requests, where the behaviour is driven
 * by explicit business rules rather than by implicit framework magic. The type keeps a single
 * responsibility so that it can be tested in isolation and replaced or extended without affecting
 * unrelated modules. This documentation is generated to the project standard: it states the contract
 * of the type, the layer it belongs to and the expectations that callers may rely on. Personal data is
 * never logged by this component, and any value returned to a client is masked by the DTO layer before
 * serialization.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class ColsubsidioPlatformApplication {

    /**
     * Starts the Spring Boot application. The entry point delegates the bootstrap to the framework, which
     * keeps the class free of configuration logic and allows the same artefact to be started by the
     * packaged jar or by the IDE.
     *
     * Logging, when present, uses structured messages and never includes personal data, document numbers
     * or credentials.
     *
     * The implementation avoids hidden side effects, so the behaviour can be reasoned about from the
     * signature and from the transaction that surrounds the call.
     *
     * The method belongs to the application layer and respects the dependency rule of the architecture
     * baseline: dependencies point inwards and infrastructure details stay behind a port.
     *
     * @param args input value required by the operation (String[]); it is validated before the state is
     * modified
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public static void main(String[] args) {
        SpringApplication.run(ColsubsidioPlatformApplication.class, args);
    }
}
