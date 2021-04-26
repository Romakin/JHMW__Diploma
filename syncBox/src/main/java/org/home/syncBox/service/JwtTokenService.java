package org.home.syncBox.service;

import org.home.syncBox.dto.AuthRequestDto;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface JwtTokenService {

    Map<String, String> login(AuthRequestDto requestDto);

    void logout(HttpServletRequest request);

}
