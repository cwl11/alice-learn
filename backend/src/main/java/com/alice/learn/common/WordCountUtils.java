package com.alice.learn.common;

public final class WordCountUtils {

    private WordCountUtils() {
    }

    public static int count(String content) {
        if (content == null || content.isBlank()) {
            return 0;
        }
        return content.trim().split("\\s+").length;
    }
}
