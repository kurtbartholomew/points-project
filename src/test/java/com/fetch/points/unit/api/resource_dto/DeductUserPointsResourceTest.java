package com.fetch.points.unit.api.resource_dto;

import com.fetch.points.api.resource_dto.DeductUserPointsResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JsonTest
public class DeductUserPointsResourceTest {

    @Autowired
    private JacksonTester<DeductUserPointsResource.Request> requestJSON;

    private static final Long VALID_POINTS = -1000L;
    private static final String JSON_TO_DESERIALIZE = "{\"points\":\""+ VALID_POINTS +"\"}";

    private DeductUserPointsResource.Request request;

    private Validator validator;

    private static Stream<DeductUserPointsResource.Request> generateValidRequests() {
        return Stream.of(
                new DeductUserPointsResource.Request(-1000L),
                new DeductUserPointsResource.Request(1000L),
                new DeductUserPointsResource.Request(Long.MIN_VALUE)
        );
    }

    private static Stream<DeductUserPointsResource.Request> generateInvalidRequests() {
        return Stream.of(
            new DeductUserPointsResource.Request(null)
        );
    }

    @BeforeEach
    public void setup() {
        request = new DeductUserPointsResource.Request(VALID_POINTS);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void shouldSerializeCorrectly() throws IOException {
        assertThat(this.requestJSON.write(request))
                .extractingJsonPathNumberValue("@.points")
                .isEqualTo(VALID_POINTS.intValue());
    }

    @Test
    public void shouldDeserializeCorrectly() throws IOException {
        assertThat(this.requestJSON.parseObject(JSON_TO_DESERIALIZE).getPoints()).isEqualTo(VALID_POINTS);
    }

    @ParameterizedTest
    @MethodSource("generateValidRequests")
    public void shouldValidateWithValidValues(DeductUserPointsResource.Request req) {
        Set<ConstraintViolation<DeductUserPointsResource.Request>> violations = validator.validate(req);
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("generateInvalidRequests")
    public void shouldFailValidateWithInvalidValues(DeductUserPointsResource.Request req) {
        Set<ConstraintViolation<DeductUserPointsResource.Request>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
    }
}
