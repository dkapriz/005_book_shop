package com.example.bookshopapp.api.dto.payment;

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
public class Confirmation {
    String type;
    @JsonProperty("return_url")
    String returnUrl;
    @JsonProperty("confirmation_url")
    String confirmationUrl;
}
