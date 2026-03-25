package com.reminder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@Profile("prod")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/reminders", true))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {}))
                .logout(logout -> logout
                        .addLogoutHandler((request, response, authentication) -> {
                            try {
                                if (authentication instanceof OAuth2AuthenticationToken) {
                                    OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
                                    OidcUser user = (OidcUser) token.getPrincipal();
                                    String idToken = user.getIdToken().getTokenValue();

                                    String logoutUrl = "http://localhost:8080/realms/Reminder/protocol/openid-connect/logout" +
                                            "?id_token_hint=" + idToken +
                                            "&post_logout_redirect_uri=" + URLEncoder.encode("http://localhost:8081/", StandardCharsets.UTF_8);

                                    response.sendRedirect(logoutUrl);
                                }
                            } catch (Exception e) {
                                // ignore
                            }
                        })
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
        );
        return http.build();
    }

}
