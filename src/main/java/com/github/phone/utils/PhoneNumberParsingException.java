package com.github.phone.utils;

public class PhoneNumberParsingException extends RuntimeException {

    private static final long serialVersionUID = 1L;


    public PhoneNumberParsingException(Throwable cause) {
        super(cause.getCause());
    }

    public PhoneNumberParsingException(String message) {
        super(message);
    }

    public PhoneNumberParsingException(String message, Throwable cause) {
        super(message, cause instanceof PhoneNumberParsingException ? cause.getCause() : cause);
    }

}
