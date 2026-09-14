package com.moh.vaxtrack.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class StaffLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {

        if (hasRole(authentication, "ROLE_SUPER_ADMIN")) {
            response.sendRedirect("/admin/dashboard");
        } else if (hasRole(authentication, "ROLE_SUB_ADMIN")) {
            response.sendRedirect("/subadmin/dashboard");
        } else if (hasRole(authentication, "ROLE_INVENTORY_MANAGER")) {
            response.sendRedirect("/inventory/dashboard");
        } else {
            // TODO: add its redirect here once that delivery is built.
            response.sendRedirect("/login/staff?dashboardComingSoon");
        }
    }

    private boolean hasRole(Authentication authentication, String roleName) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority.getAuthority().equals(roleName)) {
                return true;
            }
        }
        return false;
    }
}
