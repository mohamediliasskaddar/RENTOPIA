package com.realestate.listing.dto;

import java.time.LocalDate;

public record PropertySearchResultDTO(
        Integer propertyId,
        String title,
        String city,
        String country,
        Double averagePricePerNight,
        Double totalPrice,
        Double discountPercentage,
        String discountType,
        LocalDate checkIn,
        LocalDate checkOut,
        Integer nights
) {}