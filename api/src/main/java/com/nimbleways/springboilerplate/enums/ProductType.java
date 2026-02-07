package com.nimbleways.springboilerplate.enums;

public enum ProductType {

    NORMAL,
    SEASONAL,
    EXPIRABLE;

    public static ProductType from(String value) {
        return ProductType.valueOf(value.toUpperCase());
    }
}
