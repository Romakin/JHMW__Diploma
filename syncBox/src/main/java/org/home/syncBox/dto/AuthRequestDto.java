package org.home.syncBox.dto;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Data
@Validated
public class AuthRequestDto {

    @NotBlank
    String login;

    @NotBlank
    String password;
}
