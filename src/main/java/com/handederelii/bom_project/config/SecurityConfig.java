package com.handederelii.bom_project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // AuthService buradan alacak
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF kapat (test için)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()   // register/login açık
                        .requestMatchers("/bom/query").authenticated() // özellikle koru (opsiyonel)
                );
        return http.build();
    }
}
