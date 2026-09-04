package com.alice.learn.common;

import java.time.LocalDateTime;

public final class VocabReviewPolicy {

    private VocabReviewPolicy() {
    }

    public static int nextFamiliarity(int current, boolean remembered) {
        if (remembered) {
            return Math.min(5, current + 1);
        }
        return Math.max(0, current - 1);
    }

    public static LocalDateTime nextReviewAt(int familiarity, LocalDateTime now) {
        int days = switch (familiarity) {
            case 0 -> 0;
            case 1 -> 1;
            case 2 -> 3;
            case 3 -> 7;
            case 4 -> 14;
            default -> 30;
        };
        return now.plusDays(days);
    }
}
