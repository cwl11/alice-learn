package com.alice.learn.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReadingServiceTest {

    @Test
    void comparesAnswersCaseInsensitively() {
        assertTrue(ReadingService.isCorrect("FALSE", "false"));
        assertTrue(ReadingService.isCorrect(" medical ", "medical"));
        assertFalse(ReadingService.isCorrect("TRUE", "FALSE"));
    }
}
