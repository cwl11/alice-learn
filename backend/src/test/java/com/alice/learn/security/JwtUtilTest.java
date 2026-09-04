package com.alice.learn.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUtilTest {

    @Test
    void generatesAndParsesToken() {
        JwtUtil jwtUtil = new JwtUtil("alice-learn-dev-jwt-secret-key-please-change-32b", 72);
        String token = jwtUtil.generate(12L, "alice");
        assertEquals(12L, jwtUtil.parseUserId(token));
        assertEquals("alice", jwtUtil.parseUsername(token));
    }

    @Test
    void rejectsTamperedToken() {
        JwtUtil jwtUtil = new JwtUtil("alice-learn-dev-jwt-secret-key-please-change-32b", 72);
        String token = jwtUtil.generate(1L, "alice");
        String tampered = token.substring(0, token.length() - 4) + "abcd";
        assertThrows(Exception.class, () -> jwtUtil.parseUserId(tampered));
    }
}
