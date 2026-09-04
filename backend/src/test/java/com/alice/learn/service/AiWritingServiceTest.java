package com.alice.learn.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AiWritingServiceTest {

    @Test
    void stripsJsonFence() {
        String raw = """
                ```json
                {"taScore": 6.5}
                ```
                """;
        assertEquals("{\"taScore\": 6.5}", AiWritingService.stripMarkdown(raw));
    }

    @Test
    void extractsJsonObjectFromPrefixText() {
        String raw = "Here is the result: {\"taScore\": 7.0}";
        assertEquals("{\"taScore\": 7.0}", AiWritingService.stripMarkdown(raw));
    }
}
