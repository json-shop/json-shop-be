package deepdive.jsonstore.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    @Bean
    public AuthenticationManager authenticationManager() {
        List<AuthenticationProvider> providers = List.of(
                memberAuthenticationProvider(),
                adminAuthenticationProvider()
        );
        return new ProviderManager(providers);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationManager authenticationManager) throws Exception {

        // 로그인 필터
        MemberLoginAuthenticationFilter memberLoginFilter =
                new MemberLoginAuthenticationFilter(authenticationManager, memberJwtTokenProvider);
        memberLoginFilter.setFilterProcessesUrl("/api/v1/login");

        AdminLoginAuthenticationFilter adminLoginFilter =
                new AdminLoginAuthenticationFilter(authenticationManager, adminJwtTokenProvider);
        adminLoginFilter.setFilterProcessesUrl("/api/v1/admin/login");

        // JWT 인증 필터
        MemberJwtAuthenticationFilter memberJwtFilter =
                new MemberJwtAuthenticationFilter(memberJwtTokenProvider, authenticationManager);

        AdminJwtAuthenticationFilter adminJwtFilter =
                new AdminJwtAuthenticationFilter(adminJwtTokenProvider, authenticationManager);

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // FCM 테스트용 프론트엔드 접근경로
                        .requestMatchers("/", "/index.html", "/css/**", "/js/**").permitAll()
                                       
                        // 공용 접근 경로
                        .requestMatchers("/api/v1/login", "/api/v1/admin/login", "/api/v1/join","/api/v1/admin/join").permitAll()

                        // 관리자 전용 경로
                        .requestMatchers("/api/v1/admin/**").hasAuthority("ADMIN")

                        // 멤버 전용 경로
                        .requestMatchers("/api/v1/member/**").hasAuthority("MEMBER")
                        .requestMatchers("/api/v1/products/**", "/api/v1/cart/**", "/api/v1/delivery/**", "/api/v1/orders/**","/api/v1/fcm-tokens/**","api/v1/notifications/**").hasAuthority("MEMBER")

                        .anyRequest().authenticated()
                )
                // 로그인 필터는 UsernamePasswordAuthenticationFilter 위치에 정확히 지정
                .addFilterAt(memberLoginFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(adminLoginFilter, UsernamePasswordAuthenticationFilter.class)

                // JWT 인증 필터는 로그인 필터 뒤에 실행
                .addFilterAfter(memberJwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(adminJwtFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }
}