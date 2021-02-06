package com.fetch.points.domain;

import lombok.Value;

import javax.validation.constraints.NotBlank;

@Value
public class Payer {
    @NotBlank
    String payerName;
}
