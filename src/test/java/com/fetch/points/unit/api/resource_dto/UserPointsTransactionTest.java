package com.fetch.points.unit.api.resource_dto;

import com.fetch.points.api.resource_dto.UserPointsTransactionDTO;
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
public class UserPointsTransactionTest {

    private Validator validator;

    private static Stream<UserPointsTransactionDTO> generateValidTransaction() {
        return Stream.of(
                new UserPointsTransactionDTO(3L, "DANNON", 1000L, ZonedDateTime.now(ZoneOffset.UTC)),
                new UserPointsTransactionDTO("DANNON", 1000L)
        );
    }

    private static Stream<UserPointsTransactionDTO> generateInvalidTransaction() {
        return Stream.of(
                new UserPointsTransactionDTO(3L, null, null, ZonedDateTime.now(ZoneOffset.UTC).plusDays(1)),
                new UserPointsTransactionDTO(null, null)
        );
    }

    @BeforeEach
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @ParameterizedTest
    @MethodSource("generateValidTransaction")
    public void shouldValidateWithValidValues(UserPointsTransactionDTO req) {
        Set<ConstraintViolation<UserPointsTransactionDTO>> violations = validator.validate(req);
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("generateInvalidTransaction")
    public void shouldFailValidateWithInvalidValues(UserPointsTransactionDTO req) {
        Set<ConstraintViolation<UserPointsTransactionDTO>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
    }
}
