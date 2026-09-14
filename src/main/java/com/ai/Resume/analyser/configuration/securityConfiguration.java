package com.ai.Resume.analyser.configuration;


import com.ai.Resume.analyser.jwt.jwtFilter;
import com.ai.Resume.analyser.service.failureHandler;
import com.ai.Resume.analyser.service.successHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

/**
 * This is the ORIGINAL app's security config (JWT cookie auth + OAuth2 login +
 * stateless sessions + CORS). A previous pass replaced this file with a bare
 * SecurityConfig.java that had no JWT filter wired in at all, which broke
 * authentication for the whole app - restored here, extended (not replaced)
 * with two new matchers for TalentRevive.
 */
@Configuration
@EnableWebSecurity
public class securityConfiguration {

    @Autowired
    private entryPointService userDetails;

    @Autowired
    private jwtFilter jwtfilter;

    @Autowired
    private successHandler successHandler;

    @Autowired
    private failureHandler failureHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(requests-> requests
                        .requestMatchers("/api/auth/**","/api/core/**","/","/login","/forgotpassword","/static/**","/index.html","/manifest.json","/assets/**")
                        .permitAll()
                        .requestMatchers("/api/public/**")
                        .permitAll()
                        .requestMatchers("/api/hr/**")
                        .hasRole("hr")
                        .anyRequest().authenticated())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtfilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth ->oauth
                        .loginPage("/login")
                        .successHandler(successHandler)
                        .failureHandler(failureHandler))
                .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider =  new DaoAuthenticationProvider(userDetails);
        authenticationProvider.setPasswordEncoder(new BCryptPasswordEncoder(12));
        return authenticationProvider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:5173");
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**",configuration);
        return configurationSource;
    }

}
