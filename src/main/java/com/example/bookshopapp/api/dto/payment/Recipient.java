package com.example.bookshopapp.api.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Recipient {
    @JsonProperty("account_id")
    String accountId;
    @JsonProperty("gateway_id")
    String gatewayId;
}
