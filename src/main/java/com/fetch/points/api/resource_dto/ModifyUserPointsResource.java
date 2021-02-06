package com.fetch.points.api.resource_dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fetch.points.config.UtcZoneDateDeserializer;
import lombok.Value;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class ModifyUserPointsResource {

    @Value
    public static class Request {
        @NotNull
        @Size(min=1, max=255, message="Name (of brand/payer) must be between 1 and 255 characters")
        String payer;

        @NotNull
        Long points;

        @JsonDeserialize(using= UtcZoneDateDeserializer.class)
        @PastOrPresent
        ZonedDateTime timestamp;

        public UserPointsTransactionDTO toUserPointsTransaction(long userId) {
            ZonedDateTime timestampDate = (timestamp == null) ?
                    ZonedDateTime.now(ZoneOffset.UTC) : timestamp;
            return new UserPointsTransactionDTO(userId, this.payer, this.points, timestampDate);
        }
    }
}
