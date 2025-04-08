package deepdive.jsonstore.common.config;

import deepdive.jsonstore.domain.auth.auth.*;
import deepdive.jsonstore.domain.auth.service.AdminMemberDetailsService;
import deepdive.jsonstore.domain.auth.service.CustomMemberDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomMemberDetailsService customMemberDetailsService;
    private final AdminMemberDetailsService adminMemberDetailsService;
    private final MemberJwtTokenProvider memberJwtTokenProvider;
    private final AdminJwtTokenProvider adminJwtTokenProvider;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // 회원 로그인 필터
    @Bean
    public MemberLoginAuthenticationFilter memberLoginAuthenticationFilter(AuthenticationManager authenticationManager) {
        return new MemberLoginAuthenticationFilter(authenticationManager, memberJwtTokenProvider);
    }

    // 관리자 로그인 필터
    @Bean
    public AdminLoginAuthenticationFilter adminLoginAuthenticationFilter(AuthenticationManager authenticationManager) {
        return new AdminLoginAuthenticationFilter(authenticationManager, adminJwtTokenProvider);
    }

    // 회원 JWT 인증 필터
    @Bean
    public MemberJwtAuthenticationFilter memberJwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        return new MemberJwtAuthenticationFilter(memberJwtTokenProvider, authenticationManager);
    }

    // 관리자 JWT 인증 필터
    @Bean
    public AdminJwtAuthenticationFilter adminJwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        return new AdminJwtAuthenticationFilter(adminJwtTokenProvider, authenticationManager);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   MemberLoginAuthenticationFilter memberLoginAuthenticationFilter,
                                                   AdminLoginAuthenticationFilter adminLoginAuthenticationFilter,
                                                   MemberJwtAuthenticationFilter memberJwtAuthenticationFilter,
                                                   AdminJwtAuthenticationFilter adminJwtAuthenticationFilter
    ) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/login", "/api/v1/admin/login").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(memberLoginAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(adminLoginAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(memberJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(adminJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider memberAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customMemberDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public DaoAuthenticationProvider adminAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(adminMemberDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
