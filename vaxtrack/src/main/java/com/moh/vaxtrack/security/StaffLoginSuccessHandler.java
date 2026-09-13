package com.moh.vaxtrack.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Sends each staff role to its OWN dashboard after login, instead of
// everyone landing on /admin/dashboard (which only Super Admin can see).
@Component
public class StaffLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {

        boolean isSuperAdmin = hasRole(authentication, "ROLE_SUPER_ADMIN");
        boolean isSubAdmin = hasRole(authentication, "ROLE_SUB_ADMIN");

        if (isSuperAdmin) {
            response.sendRedirect("/admin/dashboard");
        } else if (isSubAdmin) {
            response.sendRedirect("/subadmin/dashboard");
        } else {
            // Inventory Manager and Medical Staff dashboards don't exist yet.
            // TODO: add their redirects here once those deliveries are built.
            response.sendRedirect("/login/staff?dashboardComingSoon");
        }
    }

    // Checks if the logged-in user has a specific role
    private boolean hasRole(Authentication authentication, String roleName) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority.getAuthority().equals(roleName)) {
                return true;
            }
        }
        return false;
    }
}
