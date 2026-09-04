package com.alice.learn.common;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IeltsScoreUtilsTest {

    @Test
    void roundsToNearestHalfBand() {
        assertEquals(new BigDecimal("6.5"), IeltsScoreUtils.roundToHalfBand(6.4));
        assertEquals(new BigDecimal("7.0"), IeltsScoreUtils.roundToHalfBand(6.75));
        assertEquals(new BigDecimal("7.0"), IeltsScoreUtils.roundToHalfBand(6.9));
    }

    @Test
    void overallUsesAverageThenHalfBand() {
        BigDecimal overall = IeltsScoreUtils.overall(
                new BigDecimal("6.5"),
                new BigDecimal("6.5"),
                new BigDecimal("6.0"),
                new BigDecimal("7.0"));
        assertEquals(new BigDecimal("6.5"), overall);
    }
}
