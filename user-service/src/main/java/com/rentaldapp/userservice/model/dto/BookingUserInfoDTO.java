package com.rentaldapp.userservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingUserInfoDTO {
    private Integer userId;
    private String fullName;
    private String email;
    private String phone;
    private String walletAddress;
    private Boolean isHost;
    private Boolean isGuest;
}