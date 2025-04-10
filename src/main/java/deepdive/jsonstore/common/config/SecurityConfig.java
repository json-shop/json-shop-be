package deepdive.jsonstore.common.config;

import deepdive.jsonstore.domain.auth.auth.*;
import deepdive.jsonstore.domain.auth.service.AdminMemberDetailsService;
import deepdive.jsonstore.domain.auth.service.CustomMemberDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@EnableWebSecurity
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
    public AuthenticationManager authenticationManager() {
        List<AuthenticationProvider> providers = List.of(
                memberAuthenticationProvider(),
                adminAuthenticationProvider()
        );
        return new ProviderManager(providers);
    }

    @Bean
    public AuthenticationProvider adminAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(adminMemberDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationProvider memberAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customMemberDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationManager authenticationManager) throws Exception {

        // 인증 프로바이더 등록
        http.authenticationProvider(memberAuthenticationProvider());
        http.authenticationProvider(adminAuthenticationProvider());

        //  Member 로그인 필터 설정
        MemberLoginAuthenticationFilter memberLoginAuthenticationFilter =
                new MemberLoginAuthenticationFilter(authenticationManager, memberJwtTokenProvider);
        memberLoginAuthenticationFilter.setFilterProcessesUrl("/api/v1/login");

        //  Admin 로그인 필터 설정
        AdminLoginAuthenticationFilter adminLoginAuthenticationFilter =
                new AdminLoginAuthenticationFilter(authenticationManager, adminJwtTokenProvider);
        adminLoginAuthenticationFilter.setFilterProcessesUrl("/api/v1/admin/login");

        //  JWT 인증 필터들
        MemberJwtAuthenticationFilter memberJwtAuthenticationFilter =
                new MemberJwtAuthenticationFilter(memberJwtTokenProvider, authenticationManager);
        AdminJwtAuthenticationFilter adminJwtAuthenticationFilter =
                new AdminJwtAuthenticationFilter(adminJwtTokenProvider, authenticationManager);

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/login", "/api/v1/admin/login", "/api/v1/join").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(memberLoginAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(adminLoginAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(memberJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(adminJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
