package org.home.syncBox.controller;

import org.home.syncBox.dto.AuthRequestDto;
import org.home.syncBox.dto.ErrorDto;
import org.home.syncBox.service.impl.JwtTokenServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping(value = "/cloud/")
public class AuthenticationRestController {

    private final JwtTokenServiceImpl jwtTokenService;

    public AuthenticationRestController(JwtTokenServiceImpl jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("login")
    public ResponseEntity login(@RequestBody @Valid AuthRequestDto requestDto) {
        return ResponseEntity.ok(jwtTokenService.login(requestDto));
    }

    @PostMapping("logout")
    public void logout(HttpServletRequest request) {
        jwtTokenService.logout(request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDto> handleBadCredentialsException(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDto(400, e.getMessage()));
    }

    // ToDo reset password logic

}
