package com.fetch.points.domain;

import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
public class User {
    @NotNull
    long userId;
}
