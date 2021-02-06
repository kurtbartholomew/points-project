package com.fetch.points.unit.api.resource_dto;

import com.fetch.points.api.resource_dto.ModifyUserPointsResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JsonTest
public class ModifyUserPointsResourceTest {

    private Validator validator;

    private static Stream<ModifyUserPointsResource.Request> generateValidRequests() {
        return Stream.of(
                new ModifyUserPointsResource.Request("DANNON", 1000L, ZonedDateTime.now(ZoneOffset.UTC))
        );
    }

    private static Stream<ModifyUserPointsResource.Request> generateInvalidRequests() {
        return Stream.of(
                new ModifyUserPointsResource.Request(null, null, null)
        );
    }

    @BeforeEach
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @ParameterizedTest
    @MethodSource("generateValidRequests")
    public void shouldValidateWithValidValues(ModifyUserPointsResource.Request req) {
        Set<ConstraintViolation<ModifyUserPointsResource.Request>> violations = validator.validate(req);
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("generateInvalidRequests")
    public void shouldFailValidateWithInvalidValues(ModifyUserPointsResource.Request req) {
        Set<ConstraintViolation<ModifyUserPointsResource.Request>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
    }
}
