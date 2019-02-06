package com.github.phone.utils;

public class PhoneNumberHolder {

    private final String prefix;
    private final String national;
    private final String phoneNumber;

    public PhoneNumberHolder(String prefix, String national) {
        this.prefix = prefix;
        this.national = national;
        this.phoneNumber = null;
    }

    public PhoneNumberHolder(int prefix, long national, String phoneNumber) {
        this.prefix = "+" + String.valueOf(prefix);
        this.national = String.valueOf(national);
        this.phoneNumber = phoneNumber;
    }

    public PhoneNumberHolder(String prefix, String national, String phoneNumber) {
        this.prefix = prefix;
        this.national = national;
        this.phoneNumber = phoneNumber;
    }

    public String getPrefix() {
        return prefix;
    }
    public String getNational() {
        return national;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
