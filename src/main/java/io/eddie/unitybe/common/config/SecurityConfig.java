package io.eddie.unitybe.common.config;

import io.eddie.unitybe.common.config.entrypoint.JwtAccessDeniedHandler;
import io.eddie.unitybe.common.config.entrypoint.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http){
        return http
                .cors(cors -> cors.disable())
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/users/register", "/api/v1/auth/login").anonymous()
                        .requestMatchers("/api/v1/auth/logout").authenticated()
                        .requestMatchers("/api/v1/users/me").authenticated()
                        .requestMatchers("/api/v1/auth/refresh").permitAll()
                        .requestMatchers("/api/v1/items/**").permitAll()
//                        .requestMatchers("/api/v1/items/**").hasAuthority("USER")
                        .anyRequest().permitAll()
                )
                .exceptionHandling(exp ->exp
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                //jwt는 세션 사용x
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
