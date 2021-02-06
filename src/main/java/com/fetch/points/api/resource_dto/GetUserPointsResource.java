package com.fetch.points.api.resource_dto;

import com.fetch.points.domain.PayerBalance;
import lombok.Value;

import javax.validation.constraints.*;
import java.util.List;
import java.util.stream.Collectors;

public class GetUserPointsResource {

    @Value
    public static class Response {
        @NotNull
        @NotEmpty(message = "Cannot return an empty list of point balances")
        List<ResponseItem> balance;

        @Value
        private static class ResponseItem {
            @NotNull
            @Size(min=1, max=255, message="Name (of brand/payer) must be between 1 and 255 characters")
            String payer;
            @PositiveOrZero
            Long points;

            public ResponseItem(PayerBalance payerBalance) {
                this.payer = payerBalance.getPayerName();
                this.points = payerBalance.getPoints();
            }
        }

        public Response(List<PayerBalance> payerBalance) {
            this.balance = payerBalance
                    .stream()
                    .map(ResponseItem::new).collect(Collectors.toList());
        }
    }
}
