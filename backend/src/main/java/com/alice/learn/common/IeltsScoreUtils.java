package com.alice.learn.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class IeltsScoreUtils {

    private IeltsScoreUtils() {
    }

    public static BigDecimal roundToHalfBand(double score) {
        if (score < 0) {
            score = 0;
        }
        if (score > 9) {
            score = 9;
        }
        double rounded = Math.round(score * 2.0) / 2.0;
        return BigDecimal.valueOf(rounded).setScale(1, RoundingMode.HALF_UP);
    }

    public static BigDecimal overall(BigDecimal ta, BigDecimal cc, BigDecimal lr, BigDecimal gra) {
        double avg = ta.add(cc).add(lr).add(gra)
                .divide(BigDecimal.valueOf(4), 4, RoundingMode.HALF_UP)
                .doubleValue();
        return roundToHalfBand(avg);
    }
}
