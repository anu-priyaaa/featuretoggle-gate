package com.training.featuretoggle;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain chain(HttpSecurity http) throws Exception {

        RequestMatcher h2Console =
                PathPatternRequestMatcher.withDefaults().matcher("/h2-console/**");

        http
                .authorizeHttpRequests(a -> a
                        .requestMatchers(h2Console).permitAll()
                        .requestMatchers("/login", "/register", "/css/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .csrf(c -> c.ignoringRequestMatchers(h2Console))     // fixes the 403 on Test Connection
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))   // lets the console render its frames
                .formLogin(f -> f
                        .loginPage("/login")
                        .successHandler((req, res, auth) -> {
                            boolean isAdmin = auth.getAuthorities().stream()
                                    .anyMatch(g -> g.getAuthority().equals("ROLE_ADMIN"));
                            res.sendRedirect(isAdmin ? "/admin/features" : "/dashboard");
                        }))
                .logout(l -> l.logoutSuccessUrl("/login?logout"));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}