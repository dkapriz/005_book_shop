package com.example.bookshopapp.model.enums;

public enum BookStatus {
    UNLINK("UNLINK", "Не привязана"), PAID("PAID", "Куплена"), KEPT("KEPT", "Отложена"),
    CART("CART", "В корзине"), ARCHIVED("ARCHIVED", "В архиве");

    private final String status;
    private final String name;

    BookStatus(String status, String name) {
        this.status = status;
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }
}
