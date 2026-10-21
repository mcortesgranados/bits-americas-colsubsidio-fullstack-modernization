package co.com.bits.colsubsidio.auth.application;

import co.com.bits.colsubsidio.affiliate.domain.Affiliate;
import co.com.bits.colsubsidio.affiliate.domain.AffiliateRepository;
import co.com.bits.colsubsidio.auth.domain.AppUser;
import co.com.bits.colsubsidio.auth.domain.AppUserRepository;
import co.com.bits.colsubsidio.auth.domain.Role;
import co.com.bits.colsubsidio.company.domain.Company;
import co.com.bits.colsubsidio.company.domain.CompanyRepository;
import co.com.bits.colsubsidio.config.AppSecurityProperties;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the demonstration accounts declared in configuration the first time the application starts.
 *
 * <p>The accounts are not part of the SQL seed on purpose. A password must be stored as a BCrypt digest,
 * and a digest written by hand into a migration would be a value nobody can rotate and nobody can audit.
 * Generating it at startup keeps the plain password in the environment, the digest in the database and
 * the two separated by the encoder, which is the same flow a real provisioning job would follow.</p>
 *
 * <p>The initializer is idempotent: an account whose username already exists is left untouched, so
 * restarting the application does not reset a password that was changed during a demonstration. The
 * links to an affiliate and a company are resolved by business keys —document number and NIT— rather
 * than by database identifiers, because the identifiers depend on the order in which the seed ran.</p>
 *
 * <p>Nothing is logged except the username and whether the account was created, so the plain password
 * never reaches the log file. When a link cannot be resolved the account is still created without it,
 * which allows a back-office user to exist before the affiliate it will eventually manage.</p>
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
@Component
public class DemoUserInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoUserInitializer.class);

    private final AppSecurityProperties properties;
    private final AppUserRepository users;
    private final AffiliateRepository affiliates;
    private final CompanyRepository companies;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates the initializer with the collaborators required to resolve links and hash passwords.
     *
     * <p>The repositories are received as interfaces, so the class can be tested with doubles and without
     * a database. The encoder is the same bean used during authentication, which guarantees that the
     * digest written here is the one the login flow expects to read.</p>
     *
     * <p>The constructor performs no I/O and does not touch the configuration beyond storing the
     * reference; the actual provisioning happens in {@link #run(ApplicationArguments)} once the context is
     * ready and the schema has been migrated.</p>
     *
     * @param properties      typed security configuration holding the list of demonstration accounts
     * @param users           repository of accounts, used to check existence and to persist new rows
     * @param affiliates      repository used to resolve the affiliate linked to an account by document
     * @param companies       repository used to resolve the company linked to an account by NIT
     * @param passwordEncoder encoder used to turn the configured password into a BCrypt digest
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public DemoUserInitializer(
            AppSecurityProperties properties,
            AppUserRepository users,
            AffiliateRepository affiliates,
            CompanyRepository companies,
            PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.users = users;
        this.affiliates = affiliates;
        this.companies = companies;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Provisions every configured account that does not exist yet.
     *
     * <p>The method runs inside a transaction so a failure leaves no half created account behind. For
     * each configured user it resolves the optional links first, then checks whether the username is
     * already present, and only then builds the entity. The order matters: resolving links before the
     * existence check would do unnecessary queries on every restart.</p>
     *
     * <p>A role declared in configuration that does not exist in the enum fails the startup on purpose.
     * A typo in a role is a configuration error, and discovering it during the first login would be
     * harder to diagnose than refusing to start.</p>
     *
     * <p>Links are optional by design: a back-office or auditor account has neither an affiliate nor a
     * company, and the token simply omits those claims. When the document or the NIT does not match any
     * row, the account is created without the link and a warning names the missing business key.</p>
     *
     * @param args arguments supplied by Spring Boot at startup; they are not used by this initializer
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<AppSecurityProperties.DemoUser> configured = properties.demoUsers();
        if (configured == null || configured.isEmpty()) {
            log.info("No demonstration accounts configured; skipping provisioning");
            return;
        }

        for (AppSecurityProperties.DemoUser demo : configured) {
            if (users.existsByUsername(demo.username())) {
                log.debug("Demonstration account already present: {}", demo.username());
                continue;
            }

            Long affiliateId = demo.affiliateDocument() == null
                    ? null
                    : affiliates.findByDocumentNumber(demo.affiliateDocument())
                            .map(Affiliate::getId)
                            .orElse(null);
            Long companyId = demo.companyNit() == null
                    ? null
                    : companies.findByNit(demo.companyNit())
                            .map(Company::getId)
                            .orElse(null);

            if (demo.affiliateDocument() != null && affiliateId == null) {
                log.warn("Affiliate not found for demonstration account {}: document {}",
                        demo.username(), demo.affiliateDocument());
            }
            if (demo.companyNit() != null && companyId == null) {
                log.warn("Company not found for demonstration account {}: NIT {}",
                        demo.username(), demo.companyNit());
            }

            AppUser account = new AppUser(
                    demo.username(),
                    passwordEncoder.encode(demo.password()),
                    Role.valueOf(demo.role()),
                    affiliateId,
                    companyId);
            users.save(account);
            log.info("Demonstration account created: {} with role {}", demo.username(), demo.role());
        }
    }
}
