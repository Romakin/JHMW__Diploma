package org.home.syncBox.controller;

import org.home.syncBox.dto.AuthRequestDto;
import org.home.syncBox.dto.ErrorDto;
import org.home.syncBox.model.User;
import org.home.syncBox.security.jwt.JwtTokenProvider;
import org.home.syncBox.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/cloud/")
public class AuthenticationRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @Autowired
    public AuthenticationRestController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    @PostMapping("login")
    public ResponseEntity login(@RequestBody @Valid AuthRequestDto requestDto) {
        try {
            String username = requestDto.getLogin();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, requestDto.getPassword()));
            User user = userService.findByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException("User with username " + username + " not found");
            }
            Long lastEnter = new Date().getTime();
            String token = jwtTokenProvider.createToken(username, user.getRoles(), lastEnter);
            userService.setLastEnter(user.getId(), lastEnter);
            Map<String, String> response = new HashMap<>();
            response.put("username", username);
            response.put("auth-token", token);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }


    @PostMapping("logout")
    public ResponseEntity logout(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        User user = userService.findByUsername(jwtTokenProvider.getUsername(token));
        userService.setLastEnter(user.getId(), 1L);
        return ResponseEntity.ok("");
    }


    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDto> handleBadCredentialsException(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDto(400, e.getMessage()));
    }

    // ToDo reset password logic

}
