package deepdive.jsonstore.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import deepdive.jsonstore.domain.auth.auth.AdminJwtAuthenticationFilter;
import deepdive.jsonstore.domain.auth.auth.AdminLoginAuthenticationFilter;
import deepdive.jsonstore.domain.auth.auth.AdminJwtTokenProvider;
import deepdive.jsonstore.domain.auth.auth.MemberJwtAuthenticationFilter;
import deepdive.jsonstore.domain.auth.auth.MemberLoginAuthenticationFilter;
import deepdive.jsonstore.domain.auth.auth.MemberJwtTokenProvider;
import deepdive.jsonstore.domain.auth.service.AdminMemberDetailsService;
import deepdive.jsonstore.domain.auth.service.CustomMemberDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity(debug = true)
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomMemberDetailsService customMemberDetailsService;
    private final AdminMemberDetailsService adminMemberDetailsService;
    private final MemberJwtTokenProvider memberJwtTokenProvider;
    private final AdminJwtTokenProvider adminJwtTokenProvider;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider memberAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customMemberDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationProvider adminAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(adminMemberDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * 공용 AuthenticationManager: 회원 + 관리자 로그인에 사용
     */
    @Bean
    @Primary
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(List.of(
                memberAuthenticationProvider(),
                adminAuthenticationProvider()
        ));
    }

    /**
     * 관리자 전용 AuthenticationManager: 오로지 관리자 프로바이더만 사용
     */
    @Bean
    public AuthenticationManager adminAuthenticationManager() {
        return new ProviderManager(List.of(
                adminAuthenticationProvider()
        ));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationManager authenticationManager
    ) throws Exception {

        // 회원 로그인 필터
        MemberLoginAuthenticationFilter memberLoginFilter =
                new MemberLoginAuthenticationFilter(authenticationManager, memberJwtTokenProvider);
        memberLoginFilter.setFilterProcessesUrl("/api/v1/login");

        // 관리자 로그인 필터 (전용 매니저 사용)
        AdminLoginAuthenticationFilter adminLoginFilter =
                new AdminLoginAuthenticationFilter(adminAuthenticationManager(), adminJwtTokenProvider);
        adminLoginFilter.setFilterProcessesUrl("/api/v1/admin/login");

        // JWT 인증 필터
        MemberJwtAuthenticationFilter memberJwtFilter =
                new MemberJwtAuthenticationFilter(memberJwtTokenProvider, authenticationManager);
        AdminJwtAuthenticationFilter adminJwtFilter =
                new AdminJwtAuthenticationFilter(adminJwtTokenProvider, authenticationManager);

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 공용
                        .requestMatchers(
                                "/", "/index.html", "/css/**", "/js/**", "/firebase-messaging-sw.js",
                                "/api/v1/login", "/api/v1/admin/login",
                                "/api/v1/join", "/api/v1/admin/join",
                                "/api/v1/products/**",
                                "/swagger-ui/**", "/v3/api-docs/**"
                        ).permitAll()

                        // 관리자 전용
                        .requestMatchers("/api/v1/admin/**")
                        .hasAuthority("ADMIN")

                        // 회원 전용
                        .requestMatchers("/api/v1/member/**", "/api/v2/member/**")
                        .hasAuthority("MEMBER")
                        .requestMatchers(
                                "/api/v1/carts/**", "/api/v1/delivery/**",
                                "/api/v1/orders/**", "/api/v1/fcm-tokens/**",
                                "/api/v1/notifications/**",
                                "/api/v2/carts/**"
                        ).hasAuthority("MEMBER")

                        // 그 외
                        .anyRequest().authenticated()
                )
                // 로그인 필터
                .addFilterAt(memberLoginFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(adminLoginFilter, UsernamePasswordAuthenticationFilter.class)

                // JWT 인증 필터
                .addFilterAfter(memberJwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(adminJwtFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }
}
