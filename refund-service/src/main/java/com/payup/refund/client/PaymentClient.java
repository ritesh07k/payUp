package com.payup.refund.client;

import com.payup.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentClient {

    private final RestClient paymentServiceRestClient;

    public PaymentDetails fetchPayment(UUID paymentId, String bearerToken) {
        try {
            ApiResponse<PaymentDetails> response = paymentServiceRestClient.get()
                    .uri("/api/v1/payments/{id}", paymentId)
                    .header("Authorization", "Bearer " + bearerToken)
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<ApiResponse<PaymentDetails>>() {});

            if (response == null || response.getData() == null) {
                throw new PaymentLookupException("Payment not found in payment-service");
            }
            return response.getData();
        } catch (RestClientException e) {
            throw new PaymentLookupException("Unable to reach payment-service: " + e.getMessage());
        }
    }

    public record PaymentDetails(UUID id, UUID orderId, Long amountPaise, String currency, String status) {
    }

    public static class PaymentLookupException extends RuntimeException {
        public PaymentLookupException(String message) {
            super(message);
        }
    }
}
