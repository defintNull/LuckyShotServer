package org.luckyshotserver.Models.Enums;

public enum MessageEnum {
    OK("OK"),
    READY("READY"),
    ERROR("ERROR"),
    INPUT("INPUT"),
    ADD_ACTION("ADD_ACTION"),
    SHOW("SHOW"),
    SHOW_ERROR("SHOW_ERROR"),
    REFRESH("REFRESH"),
    SHOW_BULLETS("SHOW_BULLETS"),
    SHOW_GAME_STATE("SHOW_GAME_STATE"),
    END("END"),
    ADD_ERROR("ADD_ERROR");

    private final String message;

    MessageEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
