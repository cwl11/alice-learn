package com.alice.learn.common;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VocabReviewPolicyTest {

    @Test
    void increasesFamiliarityWhenRemembered() {
        assertEquals(3, VocabReviewPolicy.nextFamiliarity(2, true));
        assertEquals(5, VocabReviewPolicy.nextFamiliarity(5, true));
    }

    @Test
    void decreasesFamiliarityWhenForgotten() {
        assertEquals(1, VocabReviewPolicy.nextFamiliarity(2, false));
        assertEquals(0, VocabReviewPolicy.nextFamiliarity(0, false));
    }

    @Test
    void schedulesReviewByFamiliarity() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 4, 12, 0);
        assertEquals(now, VocabReviewPolicy.nextReviewAt(0, now));
        assertEquals(now.plusDays(7), VocabReviewPolicy.nextReviewAt(3, now));
        assertEquals(now.plusDays(30), VocabReviewPolicy.nextReviewAt(5, now));
    }
}
