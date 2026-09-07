package com.alice.learn.security;

import com.alice.learn.common.CurrentUserHolder;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** 解析失败原因挂在 request 上，由 SecurityConfig 的 authenticationEntryPoint 读取后返回给前端。 */
    public static final String ATTR_JWT_ERROR = "alice.jwtError";
    public static final String ERROR_EXPIRED = "TOKEN_EXPIRED";
    public static final String ERROR_INVALID = "TOKEN_INVALID";

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                try {
                    Long userId = jwtUtil.parseUserId(token);
                    String username = jwtUtil.parseUsername(token);
                    var authentication = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER")));
                    authentication.setDetails(userId);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    CurrentUserHolder.setUserId(userId);
                } catch (ExpiredJwtException ex) {
                    SecurityContextHolder.clearContext();
                    request.setAttribute(ATTR_JWT_ERROR, ERROR_EXPIRED);
                } catch (JwtException | IllegalArgumentException ex) {
                    SecurityContextHolder.clearContext();
                    request.setAttribute(ATTR_JWT_ERROR, ERROR_INVALID);
                }
            }
            // 公开接口（登录/注册）即使带了过期 token 也照常放行，只有受保护接口才会走到 entryPoint 返回 401
            filterChain.doFilter(request, response);
        } finally {
            CurrentUserHolder.clear();
        }
    }
}
