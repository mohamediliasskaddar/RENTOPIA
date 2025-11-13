package com.rentaldapp.userservice.model.dto;

import com.rentaldapp.userservice.model.entity.UserLanguage.ProficiencyLevel;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddLanguageDTO {

    @NotNull(message = "L'ID de la langue est obligatoire")
    private Integer languageId;

    private ProficiencyLevel proficiencyLevel = ProficiencyLevel.INTERMEDIATE;
}