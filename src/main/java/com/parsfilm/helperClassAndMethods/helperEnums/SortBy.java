package com.parsfilm.helperClassAndMethods.helperEnums;

public enum SortBy {
    YEAR("year"),
    RATING("ratingKinopoisk");

    private final String fieldName;

    SortBy(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}