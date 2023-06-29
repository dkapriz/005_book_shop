package com.example.bookshopapp.api.request.payment;

import com.example.bookshopapp.api.dto.payment.Amount;
import com.example.bookshopapp.api.dto.payment.Confirmation;
import com.example.bookshopapp.api.dto.payment.PaymentMethodData;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentRequest {
    Amount amount;
    @JsonProperty("payment_method_data")
    PaymentMethodData paymentMethodData;
    Confirmation confirmation;
    String description;
    @JsonProperty("payment_token")
    String paymentToken;
    @JsonProperty("payment_method_id")
    String paymentMethodId;
    Boolean capture;
}