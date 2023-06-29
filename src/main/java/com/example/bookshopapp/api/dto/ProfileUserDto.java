package com.example.bookshopapp.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProfileUserDto {
    private String name;
    private String mail;
    private String phone;
    private boolean confirmEmail;
    private boolean confirmPhone;
}
