package com.example.ReadMark.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponseDTO {
	private Long userId;
	private String name;
	private String email;
}


