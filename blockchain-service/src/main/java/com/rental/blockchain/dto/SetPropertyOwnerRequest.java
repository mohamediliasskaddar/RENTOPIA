package com.rental.blockchain.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class SetPropertyOwnerRequest {
    @NotNull(message = "PropertyId est requis")
    private Long propertyId;

    @NotNull(message = "OwnerAddress est requis")
    private String ownerAddress;
}