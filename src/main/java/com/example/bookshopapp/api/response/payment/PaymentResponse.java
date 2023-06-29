package com.example.bookshopapp.api.response.payment;

import com.example.bookshopapp.api.dto.payment.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentResponse {
    String id;
    String status;
    Boolean paid;
    Amount amount;
    Confirmation confirmation;
    @JsonProperty("created_at")
    Date createdAt;
    String description;
    Metadata metadata;
    @JsonProperty("payment_method")
    PaymentMethod paymentMethod;
    Recipient recipient;
    Boolean refundable;
    Boolean test;
}

