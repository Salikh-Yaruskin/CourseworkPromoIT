package com.Announcements.Announcements.MyException;

public class UserSelfException extends Exception {
    private String additionalInfo;

    public UserSelfException(String message, String additionalInfo) {
        super(message);
        this.additionalInfo = additionalInfo;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    @Override
    public String toString() {
        return super.toString() + " Подробная инфорация: " + additionalInfo;
    }
}
