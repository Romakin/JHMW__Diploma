package org.home.syncBox.configuration;

import lombok.extern.slf4j.Slf4j;
import org.home.syncBox.security.jwt.JwtConfigurer;
import org.home.syncBox.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.cors.CorsConfiguration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@Slf4j
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtTokenProvider jwtTokenProvider;

    private static final String ADMIN_ENDPOINTS = "/api/v1/admin/**",
            LOGIN_ENDPOINT = "/cloud/login",
            USER_ENDPOINT = "/cloud/**",
            DB_CONSOLE_ENDPOINT = "/h2-console/**";

    @Value("${server.cors.originFromHeader.label}")
    private String corsOriginFromHeaderLabel;
    @Value("#{'${server.cors.allowedOrigins}'.split(';')}")
    private List<String> corsAllowedOrigins;
    @Value("#{'${server.cors.allowedMethods}'.split(';')}")
    private List<String> corsAllowedMethods;

    @Autowired
    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .cors().configurationSource(this::getCorsConfiguration)
                .and()
                .headers().frameOptions().disable()
                .and()
                .authorizeRequests()
                .antMatchers(LOGIN_ENDPOINT, DB_CONSOLE_ENDPOINT).permitAll()
                .antMatchers(ADMIN_ENDPOINTS).hasRole("ADMIN")
                .antMatchers(USER_ENDPOINT).hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
                .and()
                .apply(new JwtConfigurer(jwtTokenProvider))
                .and()
                .exceptionHandling().authenticationEntryPoint(SecurityConfig::commence)
        ;
    }

    private CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
        CorsConfiguration cc = new CorsConfiguration().applyPermitDefaultValues();
        cc.setAllowCredentials(true);
        for (String corsAllowedMethod : corsAllowedMethods) {
            try {
                cc.addAllowedMethod(HttpMethod.valueOf(corsAllowedMethod));
            } catch (IllegalArgumentException e) {
            }
        }
        List<String> list = new ArrayList<>();
        for (String s : corsAllowedOrigins) {
            String originH = request.getHeader("Origin");
            if (originH == null) continue;
            String origin = s.replace(corsOriginFromHeaderLabel, request.getHeader("Origin"));
            list.add(origin);
        }
        cc.setAllowedOrigins(list);
        return cc;
    }

    private static void commence(HttpServletRequest req, HttpServletResponse resp, AuthenticationException e) throws IOException {
        resp.setStatus(HttpStatus.UNAUTHORIZED.value());
        resp.setContentType("application/json");
        resp.getWriter().write("{\"message\":\"Unauthorized request\",\"id\":\"401\"}");
    }
}
