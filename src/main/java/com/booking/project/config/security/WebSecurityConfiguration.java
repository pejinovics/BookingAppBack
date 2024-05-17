package com.booking.project.config.security;

import com.booking.project.config.security.jwt.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)  // <-- Obavezno za @PreAuthorize
public class WebSecurityConfiguration {

//    @Autowired
//    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeRequests() // csrf->disabled, pošto nam JWT odrađuje zaštitu od CSRF napada          statički html i login mogu svi da pozovu
                .requestMatchers("/*").permitAll().requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/register**").permitAll()
                .requestMatchers("api/register/confirm**").permitAll()
                .requestMatchers("/api/accommodations/cards/filter**").permitAll()
                .requestMatchers("/api/accommodations/minMaxPrice").permitAll()
                .requestMatchers("/api/accommodations/popular").permitAll()
                .requestMatchers("/api/accommodations/{id}").permitAll()
                .requestMatchers("/api/commentsAboutAcc/acc/{id}").permitAll()
                .requestMatchers("/socket/**").permitAll()
//                .requestMatchers("/api/accommodation**").authenticated() // sav pristup API-ju mora da bude autentikovan
                .anyRequest().authenticated()
                .and()
                .cors()
                .and().oauth2ResourceServer(auth ->
                        auth.jwt(token -> token.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter())));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();// PasswordEncoderFactories.createDelegatingPasswordEncoder();
        //System.out.println(encoder.encode("admin"));
        return encoder;
//        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}