package com.payUp.build.payment.entity;

public enum PaymentStatus {
    CREATED,
    PENDING,
    PROCESSING,
    AUTHORIZED,
    CAPTURED,
    FAILED,
    REFUNDED
}