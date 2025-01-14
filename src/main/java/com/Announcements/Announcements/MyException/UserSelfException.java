package com.Announcements.Announcements.MyException;

import lombok.Getter;

@Getter
public class UserSelfException extends Exception {
    private final String additionalInfo;

    public UserSelfException(String message, String additionalInfo) {
        super(message);
        this.additionalInfo = additionalInfo;
    }

    @Override
    public String toString() {
        return super.toString() + " Подробная инфорация: " + additionalInfo;
    }
}
