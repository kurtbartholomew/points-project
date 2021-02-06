package com.fetch.points.unit.api.resource_dto;

import com.fetch.points.api.resource_dto.GetUserPointsResource;
import com.fetch.points.domain.PayerBalance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JsonTest
public class GetUserPointsResourceTest {

    private Validator validator;

    private static Stream<GetUserPointsResource.Response> generateValidResponses() {
        return Stream.of(
            new GetUserPointsResource.Response(List.of(
                    new PayerBalance(null, null, null)
            ))
        );
    }

    private static Stream<GetUserPointsResource.Response> generateInvalidResponses() {
        return Stream.of(
                new GetUserPointsResource.Response(Collections.emptyList())
        );
    }

    @BeforeEach
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @ParameterizedTest
    @MethodSource("generateValidResponses")
    public void shouldValidateWithValidValues(GetUserPointsResource.Response req) {
        Set<ConstraintViolation<GetUserPointsResource.Response>> violations = validator.validate(req);
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("generateInvalidResponses")
    public void shouldFailValidateWithInvalidValues(GetUserPointsResource.Response req) {
        Set<ConstraintViolation<GetUserPointsResource.Response>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
    }
}
