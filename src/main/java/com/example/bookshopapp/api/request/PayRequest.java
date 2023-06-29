package com.example.bookshopapp.api.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.example.bookshopapp.config.BookShopConfig.TIME_CACHING_PAYMENT_REQUEST_SEC;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PayRequest {
    private String hash;
    private String sum;
    private Long time;

    @Override
    public int hashCode() {
        return hash.hashCode() + sum.hashCode() + this.getCurrentTime();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PayRequest) {
            return this.hash.equals(((PayRequest) obj).hash) &&
                    this.sum.equals(((PayRequest) obj).sum) &&
                    this.getCurrentTime() == ((PayRequest) obj).getCurrentTime();
        }
        return false;
    }

    public int getCurrentTime() {
        return (int) (time / (TIME_CACHING_PAYMENT_REQUEST_SEC * 1000));
    }
}
