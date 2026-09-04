package com.alice.learn.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WordCountUtilsTest {

    @Test
    void countsEnglishWords() {
        assertEquals(4, WordCountUtils.count("This is a test"));
    }

    @Test
    void treatsBlankAsZero() {
        assertEquals(0, WordCountUtils.count("   "));
        assertEquals(0, WordCountUtils.count(null));
    }
}
