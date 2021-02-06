package com.fetch.points.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
public class PayerBalance {

    @NotBlank
    @NotNull
    String payerName;

    @NotNull
    @PositiveOrZero
    Long points;

    @NotNull
    ZonedDateTime createdDate;

    public PayerBalance(PayerBalance balance, Long pointsUpdate) {
        this.payerName = balance.getPayerName();
        this.points = pointsUpdate;
        this.createdDate = balance.getCreatedDate();
    }
}
