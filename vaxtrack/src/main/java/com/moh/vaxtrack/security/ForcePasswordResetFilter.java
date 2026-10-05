package com.moh.vaxtrack.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ForcePasswordResetFilter extends OncePerRequestFilter {

    private static final String RESET_PATH = "/staff-profile/force-reset-password";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        boolean isExempt = path.equals(RESET_PATH)
                || path.equals("/login/staff")
                || path.equals("/logout")
                || path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/");

        if (!isExempt) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof CustomUserDetails principal) {
                Boolean mustReset = principal.getUser().getMustResetPassword();
                if (Boolean.TRUE.equals(mustReset)) {
                    response.sendRedirect(RESET_PATH);
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
