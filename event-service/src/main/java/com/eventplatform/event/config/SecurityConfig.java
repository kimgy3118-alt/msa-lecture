package com.eventplatform.event.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity

public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(request -> {
                var config = new org.springframework.web.cors.CorsConfiguration();
                config.addAllowedOriginPattern("*");
                config.addAllowedMethod("*");
                config.addAllowedHeader("*");
                return config;
            }))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        return http.build();
    }
}

// public class SecurityConfig {
//     @Bean
//     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//         http
//             .csrf(csrf -> csrf.disable())
            // .cors(cors -> cors.configurationSource(request -> {
            //     var config = new org.springframework.web.cors.CorsConfiguration();
            //     config.addAllowedOriginPattern("*");
            //     config.addAllowedMethod("*");
            //     config.addAllowedHeader("*");
            //     return config;
            // }))
//             .sessionManagement(session ->
//                 session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//             .authorizeHttpRequests(auth -> auth
//                 .requestMatchers(
//                     "/api-docs/**",
//                     "/swagger-ui/**",
//                     "/swagger-ui.html"
//                 ).permitAll()
//                 // 행사 목록/상세 조회는 인증 불필요
//                 .requestMatchers(HttpMethod.GET, "/api/events", "/api/events/**").permitAll()
//                 // 내부 서비스 호출 (Client Credentials)
//                 .requestMatchers("/api/events/internal/**").hasAuthority("SCOPE_service.read")
//                 // 행사 등록은 ORGANIZER만
//                 .requestMatchers(HttpMethod.POST, "/api/events").hasAuthority("ROLE_ORGANIZER")
//                 .anyRequest().authenticated()
//             )
//             .oauth2ResourceServer(oauth2 -> oauth2
//                 .jwt(jwt -> {})
//             );

//         return http.build();
//     }
// }