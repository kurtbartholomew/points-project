package com.fetch.points.unit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fetch.points.config.UtcZoneDateDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;


public class UtcZoneDateDeserializerTest {

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class TestDate {
        @JsonDeserialize(using= UtcZoneDateDeserializer.class)
        ZonedDateTime dateTime;
    }

    private ZonedDateTime createUTCDateTime() {
        return ZonedDateTime.now(ZoneOffset.UTC);
    }

    @Test
    public void shouldDeserializeCorrectly() throws IOException {
        ZonedDateTime dateTime = createUTCDateTime();
        String json = String.format("{\"dateTime\":\"%s\"}", dateTime);
        TestDate deserializedTestDate = new ObjectMapper().readValue(json, TestDate.class);
        assertEquals(dateTime, deserializedTestDate.getDateTime());
    }
}