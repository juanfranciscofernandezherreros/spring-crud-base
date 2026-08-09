package com.example.crudbase.config;

import com.example.crudbase.controller.ClienteController;
import com.example.crudbase.controller.ResultController;
import com.example.crudbase.exception.GlobalExceptionHandler;
import com.example.crudbase.logging.RequestLoggingFilter;
import com.example.crudbase.mapper.ClienteMapper;
import com.example.crudbase.mapper.ClienteMapperImpl;
import com.example.crudbase.mapper.ResultMapper;
import com.example.crudbase.mapper.ResultMapperImpl;
import com.example.crudbase.repository.ClienteRepository;
import com.example.crudbase.repository.ResultRepository;
import com.example.crudbase.service.ClienteService;
import com.example.crudbase.service.ResultService;
import com.example.crudbase.service.impl.ClienteServiceImpl;
import com.example.crudbase.service.impl.ResultServiceImpl;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Central, explicit wiring for every application bean that would otherwise rely on
 * stereotype annotations ({@code @Service}, {@code @Component}) and classpath scanning.
 * {@code @RestController} and {@code @RestControllerAdvice} still appear on their
 * respective classes because Spring MVC requires them there for handler/advice
 * detection, but those classes are excluded from component scanning (see
 * {@link com.example.crudbase.CrudBaseApplication}) so this is the only place that
 * instantiates them.
 *
 * <p>{@link ResultRepository} and {@link ClienteRepository} are deliberately not
 * redefined here: Spring Data JPA repository interfaces are backed by a proxy
 * generated via reflection ({@code JpaRepositoryFactoryBean}), not a plain class you
 * can {@code new} up, so manually {@code @Bean}-defining them would fight the
 * framework for no benefit. They stay auto-configured by
 * {@code spring-boot-starter-data-jpa}.
 */
@Configuration
public class BeanConfig {

    @Bean
    public ResultMapper resultMapper() {
        return new ResultMapperImpl();
    }

    @Bean
    public ClienteMapper clienteMapper() {
        return new ClienteMapperImpl();
    }

    /**
     * {@code @Lazy}: this bean depends on {@link MeterRegistry}, which is only present
     * when Actuator/Micrometer autoconfiguration runs. Slice tests such as
     * {@code @DataJpaTest} still process this class (plain {@code @Configuration}
     * classes aren't excluded by test-slice filters the way {@code @Service} would be)
     * but don't load Actuator — without laziness, context refresh would eagerly call
     * this method and fail with "no qualifying bean of type MeterRegistry" even though
     * nothing in that slice ever asks for a {@link ResultService}. This alone only defers
     * creation until first *demand*, though — see the {@code @Lazy} on {@link #resultController}'s
     * parameter for why that demand doesn't happen eagerly either.
     */
    @Bean
    @Lazy
    public ResultService resultService(ResultRepository resultRepository, ResultMapper resultMapper, MeterRegistry meterRegistry) {
        return new ResultServiceImpl(resultRepository, resultMapper, meterRegistry);
    }

    /**
     * {@code @Lazy} on the parameter (not just on {@link #resultService}'s own definition)
     * matters here: a lazy bean *definition* only defers creation until something actually
     * demands it, and an eagerly-created bean's constructor/factory-method parameter is
     * exactly such a demand unless that specific injection point is also marked
     * {@code @Lazy} — otherwise {@code resultController} would immediately force
     * {@code resultService} to resolve, defeating the point.
     */
    @Bean
    public ResultController resultController(@Lazy ResultService resultService) {
        return new ResultController(resultService);
    }

    /**
     * {@code @Lazy}: same reasoning as {@link #resultService}, mirrored for the
     * Cliente stack — this bean also depends on {@link MeterRegistry}, which is only
     * present once Actuator/Micrometer autoconfiguration runs.
     */
    @Bean
    @Lazy
    public ClienteService clienteService(ClienteRepository clienteRepository, ClienteMapper clienteMapper, MeterRegistry meterRegistry) {
        return new ClienteServiceImpl(clienteRepository, clienteMapper, meterRegistry);
    }

    /**
     * {@code @Lazy} on the parameter mirrors {@link #resultController}'s reasoning:
     * without it, eagerly creating {@code clienteController} would force
     * {@code clienteService} to resolve immediately, defeating its own laziness.
     */
    @Bean
    public ClienteController clienteController(@Lazy ClienteService clienteService) {
        return new ClienteController(clienteService);
    }

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    public RequestLoggingFilter requestLoggingFilter() {
        return new RequestLoggingFilter();
    }
}
