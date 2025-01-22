package org.luckyshotserver.Models.Enums;

public enum MessageEnum {
    OK("OK"),
    ERROR("ERROR");

    private final String message;

    MessageEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
