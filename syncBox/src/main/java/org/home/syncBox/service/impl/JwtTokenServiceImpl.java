package org.home.syncBox.service.impl;

import org.home.syncBox.dto.AuthRequestDto;
import org.home.syncBox.model.User;
import org.home.syncBox.security.jwt.JwtTokenProvider;
import org.home.syncBox.service.JwtTokenService;
import org.home.syncBox.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtTokenServiceImpl implements JwtTokenService {

    private static final long DEFAULT_BREAK_AUTH_USER_LAST_ENTER_TIME = 1L;

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @Autowired
    public JwtTokenServiceImpl(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    @Override
    public Map<String, String> login(AuthRequestDto requestDto) {
        try {
            String username = requestDto.getLogin();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, requestDto.getPassword()));
            User user = userService.getByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException("User with username " + username + " not found");
            }
            Long lastEnter = new Date().getTime();
            String token = jwtTokenProvider.createToken(username, user.getRoles(), lastEnter);
            userService.setLastEnter(user.getId(), lastEnter);
            Map<String, String> response = new HashMap<>();
            response.put("username", username);
            response.put("auth-token", token);
            return response;
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    @Override
    public void logout(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        User user = userService.getByUsername(jwtTokenProvider.getUsername(token));
        if (user != null)
            userService.setLastEnter(user.getId(), DEFAULT_BREAK_AUTH_USER_LAST_ENTER_TIME);
        else
            throw new BadCredentialsException("Invalid username or password");
    }
}
