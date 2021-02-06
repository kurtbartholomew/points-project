package com.fetch.points.api.resource_dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fetch.points.config.UtcZoneDateDeserializer;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Data
public class UserPointsTransactionDTO {

    Long userId;

    @NotNull
    @Size(min=1, max=255, message="Name (of brand/payer) must be between 1 and 255 characters")
    String payerName;

    @NotNull
    Long points;

    @JsonDeserialize(using=UtcZoneDateDeserializer.class)
    @PastOrPresent
    ZonedDateTime transactionDate;

    public UserPointsTransactionDTO(long userId, String name, Long points, ZonedDateTime transactionDate) {
        this.userId = userId;
        this.payerName = name;
        this.points = points;
        this.transactionDate = transactionDate;
    }

    public UserPointsTransactionDTO(String name, Long points) {
        this.payerName = name;
        this.points = points;
        this.transactionDate = ZonedDateTime.now(ZoneOffset.UTC);
    }
}
