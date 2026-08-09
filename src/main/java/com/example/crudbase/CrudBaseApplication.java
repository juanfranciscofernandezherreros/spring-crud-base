package com.example.crudbase;

import com.example.crudbase.controller.ClienteController;
import com.example.crudbase.controller.ResultController;
import com.example.crudbase.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * {@link ResultController}, {@link ClienteController}, and {@link GlobalExceptionHandler}
 * keep the {@code @RestController}/{@code @RestControllerAdvice} annotations Spring MVC
 * requires for handler/advice detection, but are excluded from component scanning
 * here because {@link com.example.crudbase.config.BeanConfig} instantiates them
 * explicitly — without this exclusion, Spring would also auto-register a second,
 * scan-discovered instance of each and fail to start with an ambiguous mapping error.
 */
@SpringBootApplication
@ComponentScan(excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = { ResultController.class, ClienteController.class, GlobalExceptionHandler.class }))
public class CrudBaseApplication {
    public static void main(String[] args) {
        SpringApplication.run(CrudBaseApplication.class, args);
    }
}
