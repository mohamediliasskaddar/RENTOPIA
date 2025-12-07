package com.rental.payment.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BalanceResponse {
    private String walletAddress;
    private Double balanceEth;
    private Double balanceUsd;  // Optionnel: conversion USD
    private String message;
}