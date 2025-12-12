package com.malak.chatapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
	@Schema(example = "user")
	@NotBlank(message = "Username must be not blank or null")
	private String username;
	
	@Schema(example = "123456789")
	@NotBlank(message = "Password Must be not blank or null")
	@Size(min = 8, message = "Password must be at least 8 characters")
	private String password;
}
