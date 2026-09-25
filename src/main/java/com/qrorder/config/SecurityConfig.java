package com.qrorder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Simple in-memory staff logins for the demo. Three fixed accounts, one per
 * role - a real deployment would back this with a proper user table and
 * per-restaurant accounts, but for a single-restaurant college project this
 * keeps things simple while still genuinely gating each dashboard.
 *
 * Default credentials (change freely - see README):
 *   kitchen / kitchen123   -> ROLE_KITCHEN
 *   waiter  / waiter123    -> ROLE_WAITER
 *   manager / manager123   -> ROLE_MANAGER (also allowed into kitchen + waiter views)
 *
 * The customer-facing pages and /api/public/** stay completely open, since a
 * real diner is never expected to log in.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder encoder) {
        UserDetails kitchen = User.withUsername("kitchen")
                .password(encoder.encode("kitchen123")).roles("KITCHEN").build();
        UserDetails waiter = User.withUsername("waiter")
                .password(encoder.encode("waiter123")).roles("WAITER").build();
        UserDetails manager = User.withUsername("manager")
                .password(encoder.encode("manager123")).roles("MANAGER").build();
        return new InMemoryUserDetailsManager(kitchen, waiter, manager);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // customer-facing: always open, no login
                .requestMatchers("/", "/index.html", "/customer.html", "/api/public/**").permitAll()
                // dev convenience console
                .requestMatchers("/h2-console/**").permitAll()
                // static assets / login page itself
                .requestMatchers("/login", "/login.html", "/css/**", "/js/**").permitAll()
                // role-gated dashboards - manager can also view kitchen/waiter screens
                .requestMatchers("/kitchen.html", "/api/kitchen/**").hasAnyRole("KITCHEN", "MANAGER")
                .requestMatchers("/waiter.html", "/api/waiter/**").hasAnyRole("WAITER", "MANAGER")
                .requestMatchers("/manager.html", "/api/manager/**").hasRole("MANAGER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                .permitAll())
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                .logoutSuccessUrl("/login.html?logout")
                .permitAll())
            // our fetch() calls from static HTML don't carry CSRF tokens, so
            // the JSON API is exempted - acceptable for this demo, a real
            // build would add token handling instead of disabling it broadly
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/h2-console/**", "/login"))
            .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
        return http.build();
    }
}
