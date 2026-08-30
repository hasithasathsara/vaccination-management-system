package com.moh.vaxtrack.config;

import com.moh.vaxtrack.security.CustomUserDetailsService;
import com.moh.vaxtrack.security.PatientUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PatientUserDetailsService patientUserDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                           PatientUserDetailsService patientUserDetailsService) {
        this.userDetailsService = userDetailsService;
        this.patientUserDetailsService = patientUserDetailsService;
    }

    // Shared password hashing, used by both staff and patient login
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Staff login credential check (user table)
    @Bean
    public DaoAuthenticationProvider staffAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // Patient login credential check (patient table)
    @Bean
    public DaoAuthenticationProvider patientAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(patientUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // Patient security rules — checked FIRST (narrow match), completely separate from staff
    @Bean
    @Order(1)
    public SecurityFilterChain patientSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/login/patient", "/register/patient", "/patient/**", "/patient-logout")
                .authenticationProvider(patientAuthenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login/patient", "/register/patient").permitAll()
                        .requestMatchers("/patient/**").hasRole("PATIENT")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login/patient")
                        .loginProcessingUrl("/login/patient")
                        .defaultSuccessUrl("/patient/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/patient-logout")
                        .logoutSuccessUrl("/login/patient")
                        .permitAll()
                );

        return http.build();
    }

    // Staff / admin security rules — checked SECOND, catches everything else
    @Bean
    @Order(2)
    public SecurityFilterChain staffSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(staffAuthenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login/staff", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("SUPER_ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login/staff")
                        .loginProcessingUrl("/login/staff")
                        .defaultSuccessUrl("/admin/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login/staff")
                        .permitAll()
                );

        return http.build();
    }
}
