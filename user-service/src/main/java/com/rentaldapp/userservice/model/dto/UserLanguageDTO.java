package com.rentaldapp.userservice.model.dto;

import com.rentaldapp.userservice.model.entity.UserLanguage.ProficiencyLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLanguageDTO {
    private Integer languageId;
    private String languageCode;
    private String languageName;
    private String languageNativeName;
    private ProficiencyLevel proficiencyLevel;
}