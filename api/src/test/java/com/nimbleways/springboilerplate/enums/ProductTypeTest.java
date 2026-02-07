package com.nimbleways.springboilerplate.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("ProductType Enum Tests")
class ProductTypeTest {

    @Test
    @DisplayName("Should have exactly 3 enum values")
    void shouldHaveThreeValues() {
        ProductType[] values = ProductType.values();
        assertEquals(3, values.length);
    }

    @Test
    @DisplayName("Should contain NORMAL value")
    void shouldContainNormal() {
        assertNotNull(ProductType.NORMAL);
        assertEquals("NORMAL", ProductType.NORMAL.name());
    }

    @Test
    @DisplayName("Should contain SEASONAL value")
    void shouldContainSeasonal() {
        assertNotNull(ProductType.SEASONAL);
        assertEquals("SEASONAL", ProductType.SEASONAL.name());
    }

    @Test
    @DisplayName("Should contain EXPIRABLE value")
    void shouldContainExpirable() {
        assertNotNull(ProductType.EXPIRABLE);
        assertEquals("EXPIRABLE", ProductType.EXPIRABLE.name());
    }

    @Test
    @DisplayName("Should convert uppercase string to NORMAL")
    void shouldConvertUppercaseToNormal() {
        ProductType result = ProductType.from("NORMAL");
        assertEquals(ProductType.NORMAL, result);
    }

    @Test
    @DisplayName("Should convert lowercase string to NORMAL")
    void shouldConvertLowercaseToNormal() {
        ProductType result = ProductType.from("normal");
        assertEquals(ProductType.NORMAL, result);
    }

    @Test
    @DisplayName("Should convert mixed case string to SEASONAL")
    void shouldConvertMixedCaseToSeasonal() {
        ProductType result = ProductType.from("SeAsOnAl");
        assertEquals(ProductType.SEASONAL, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"NORMAL", "normal", "Normal", "nOrMaL"})
    @DisplayName("Should handle different cases for NORMAL")
    void shouldHandleDifferentCasesForNormal(String input) {
        assertEquals(ProductType.NORMAL, ProductType.from(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SEASONAL", "seasonal", "Seasonal", "sEaSoNaL"})
    @DisplayName("Should handle different cases for SEASONAL")
    void shouldHandleDifferentCasesForSeasonal(String input) {
        assertEquals(ProductType.SEASONAL, ProductType.from(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"EXPIRABLE", "expirable", "Expirable", "ExPiRaBlE"})
    @DisplayName("Should handle different cases for EXPIRABLE")
    void shouldHandleDifferentCasesForExpirable(String input) {
        assertEquals(ProductType.EXPIRABLE, ProductType.from(input));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid value")
    void shouldThrowExceptionForInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> {
            ProductType.from("INVALID");
        });
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for empty string")
    void shouldThrowExceptionForEmptyString() {
        assertThrows(IllegalArgumentException.class, () -> {
            ProductType.from("");
        });
    }

    @Test
    @DisplayName("Should throw NullPointerException for null value")
    void shouldThrowExceptionForNullValue() {
        assertThrows(NullPointerException.class, () -> {
            ProductType.from(null);
        });
    }
}