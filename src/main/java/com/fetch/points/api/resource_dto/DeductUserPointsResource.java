package com.fetch.points.api.resource_dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import javax.validation.constraints.*;
import java.util.List;
import java.util.stream.Collectors;

public class DeductUserPointsResource {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        @NotNull
        Long points;
    }

    @Value
    public static class Response {
        @NotNull
        List<ResponseItem> deductions;

        @Value
        private static class ResponseItem {
            @Size(min=1, max=255, message="Name (of brand/payer) must be between 1 and 255 characters")
            @NotNull
            String payer;

            @Negative(message = "Point deductions can only be negative")
            @NotNull
            Long points;
            String timestamp = "now";

            public ResponseItem(UserPointsTransactionDTO pointsTransaction) {
                this.payer = pointsTransaction.getPayerName();
                this.points = pointsTransaction.getPoints();
            }
        }

        public Response(List<UserPointsTransactionDTO> userPointsTransactions) {
            this.deductions = userPointsTransactions
                    .stream()
                    .map(ResponseItem::new).collect(Collectors.toList());
        }
    }
}
