package com.rentaldapp.userservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LanguageDTO {
    private Integer id;
    private String code;
    private String name;
    private String nativeName;
}