package com.swef.cookcode.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSignUpRequest {
  @NotBlank
  private String email;

  @NotBlank
  private String nickname;

  @NotBlank
  private String password;
}
