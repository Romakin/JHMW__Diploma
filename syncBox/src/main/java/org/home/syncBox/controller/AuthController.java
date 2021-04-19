package org.home.syncBox.controller;

import lombok.AllArgsConstructor;
import org.home.syncBox.dto.AuthRequest;
import org.home.syncBox.dto.Login;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@AllArgsConstructor
@RestController
@RequestMapping(value={ "/login", "/logout"})
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @PostMapping("login")
    public Login login(@RequestBody @Valid AuthRequest authRequest) {

    }
}
