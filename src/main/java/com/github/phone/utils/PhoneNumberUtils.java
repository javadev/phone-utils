package com.github.phone.utils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PhoneNumberUtils {

    private static Logger log = LoggerFactory.getLogger(PhoneNumberUtils.class);

    private static final String EMPTY_COUNTRY_CODE = "null";
    private static final String UNKNOWN_REGION = "ZZ";
    private static final String JUST_NUMBERS = "[^\\w\\s\\.]";

    static com.google.i18n.phonenumbers.PhoneNumberUtil phoneUtil;

    static {
        phoneUtil = com.google.i18n.phonenumbers.PhoneNumberUtil.getInstance();
    }

    public static Phonenumber.PhoneNumber parsePhoneByGoogle(String phone, String country) {

        try {
            return phoneUtil.parse(phone, country);

        } catch (NumberParseException e) {
            log.warn(e.getMessage() + " country: " + country + " phone: " + phone);
        }
        return null;
    }

    public static boolean hasCountryCode(int code, String phoneNumber) {
        return isPossibleFullPhoneNumber(phoneNumber) && getCountryCodeFromFullPhoneNumber(phoneNumber) == code;
    }

    /**
     * Given a possible full phone number, starting with +, country code part of number
     * will be returned.
     *
     * @param fullPhoneNumber Full phone number, starting with +
     * @return The country code
     * @throws PhoneNumberParsingException if phone number not valid
     */
    public static int getCountryCodeFromFullPhoneNumber(String fullPhoneNumber) {
        try {
            PhoneNumber phoneNumber = phoneUtil.parse(fullPhoneNumber, UNKNOWN_REGION);
            return phoneNumber.getCountryCode();
        } catch (NumberParseException e) {
            throw new PhoneNumberParsingException(e);
        }
    }

    /**
     * Italian numbers are strange, this method checks if number is italian,
     * and thereby leading 0 in national part valid.
     * @param fullPhoneNumber Full phone number, starting with +
     * @return True if italian number, false otherwise
     */
    public static boolean isItalianOrUnknownNumber(String fullPhoneNumber) {
        PhoneNumber phoneNumber;
        try {
            phoneNumber = phoneUtil.parse(fullPhoneNumber, UNKNOWN_REGION);
        } catch (NumberParseException e) {
            return true;
        }
        return phoneNumber.isItalianLeadingZero();
    }

    /**
     * Given a possible full phone number, starting with +, country code part of number
     * will be returned with + added as prefix.
     * @param fullPhoneNumber Full phone number, starting with +
     * @return The country code with prefix +
     * @throws PhoneNumberParsingException if phone number not valid
     */
    public static String getCountryCodeWithPlusSignFromFullPhoneNumber(String fullPhoneNumber) {
        try {
            PhoneNumber phoneNumber = phoneUtil.parse(fullPhoneNumber, UNKNOWN_REGION);
            return "+" + String.valueOf(phoneNumber.getCountryCode());
        } catch (NumberParseException e) {
            throw new PhoneNumberParsingException(e);
        }
    }

    /**
     * Will get national part of phone number
     * @param fullPhoneNumber Full phone number, starting with +
     * @return the national number
     * @throws PhoneNumberParsingException if phone number not valid
     */
    public static String getPhoneNumberWithoutCountryCodeFromFullPhoneNumber(String fullPhoneNumber) {
        try {
            PhoneNumber phoneNumber = phoneUtil.parse(fullPhoneNumber, UNKNOWN_REGION);
            StringBuilder nationalNumber = new StringBuilder();
            if (phoneNumber.isItalianLeadingZero()) {
                nationalNumber.append("0");
            }
            nationalNumber.append(Long.toString(phoneNumber.getNationalNumber()));
            return nationalNumber.toString();
        } catch (NumberParseException e) {
            throw new PhoneNumberParsingException(e);
        }
    }

    /**
     * Returns a Google PhoneNumber object built by parsing provided phone number
     * @param fullPhoneNumber Full phone number, starting with +
     * @return Google PhoneNumber object
     * @throws PhoneNumberParsingException if phone number not valid
     */
    public static PhoneNumber getPhoneNumberObjFromFullPhoneNumber(String fullPhoneNumber) {

        try {
            return  phoneUtil.parse(fullPhoneNumber, UNKNOWN_REGION);
        } catch (NumberParseException e) {
            throw new PhoneNumberParsingException(e);
        }
    }

    /**
     * Comparing if two national numbers are the same.
     * @param phone1 first number to check
     * @param phone2 second number to check
     * @return True if the same, false otherwise
     * @throws PhoneNumberParsingException phone numbers not valid
     */
    public static boolean areNationalNumbersSame(String phone1, String phone2) {

        if(phone1 == null || phone1.isEmpty() || phone2 == null || phone2.isEmpty()) {
            return false;
        }

        try{

            Long number1 = getNationalNumber(phone1);
            Long number2 = getNationalNumber(phone2);

            return !(number1 == null || number2 == null) && number1.equals(number2);

        } catch (PhoneNumberParsingException e) {
            return false;
        }
    }

    /*
     * Input can be with or without country code, and with or without country prefix at all.
     * We will do our best in the method. If all other fails we will simply return the raw phone
     * number given as input, and with all non numeric characters removed.
     */
    public static Long getNationalNumber(String phoneNumber) {
        if(phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return null;
        }
        try{

            PhoneNumber phoneObj = getPhoneNumberObjFromFullPhoneNumberAddPlusPrefixIfNotExist(phoneNumber);

            return phoneObj.getNationalNumber();

        }catch(PhoneNumberParsingException e) {
            return removeAllNonNumeric(phoneNumber);
        }
    }

    /*
     * Be aware that this method is kind of hack.
     *
     * https://groups.google.com/forum/#!topic/libphonenumber-discuss/IqP4cC8udn0
     */
    public static PhoneNumber getPhoneNumberObjFromFullPhoneNumberAddPlusPrefixIfNotExist(String fullPhoneNumber) {

        try {

            return getPhoneNumberObjFromFullPhoneNumber(fullPhoneNumber);

        } catch (PhoneNumberParsingException e) {

            // if doesnt exist, lets add plus prefix and
            if(fullPhoneNumber == null || fullPhoneNumber.isEmpty()) {
                throw new PhoneNumberParsingException("Phone number is null or empty: "+fullPhoneNumber);
            }
            // a full phone number requires plus prefix, add if it doesn't exist
            if(!fullPhoneNumber.startsWith("+") && fullPhoneNumber.startsWith("1")) {
                fullPhoneNumber = "+"+fullPhoneNumber;
            }

            if(isValidFullPhoneNumberHelper(fullPhoneNumber)) {
                return getPhoneNumberObjFromFullPhoneNumber(fullPhoneNumber);
            }

            throw e;
        }
    }

    /*
     * Checks if number is valid. Adds default country code provided if phone number is not complete.
     */
    public static boolean isValidPhoneNumber(String defaultCountryCode, String phoneNumber) {
        return isValidFullPhoneNumberHelper(
                generateFullPhoneNumber(defaultCountryCode, phoneNumber)
        );
    }

    public static boolean isValidNorwegianPhoneNumber(String phoneNumber) {
        return isValidPhoneNumber("+47", phoneNumber);
    }

    public static List<String> validatePhoneNumbers(List<String> numbers) {
        if(numbers == null) {
            return new ArrayList<>();
        }
        return numbers.stream()
                .filter(n -> n != null)
                .filter(n -> !n.isEmpty())
                .filter(n -> PhoneNumberUtils.isValidPhoneNumber("+47", n))
                .distinct()
                .map(n -> PhoneNumberUtils.generateFullPhoneNumber("+47", n))
                .collect(Collectors.toList());
    }

    public static String generateFullPhoneNumber(String defaultCountryCode, String phoneNumber) {

        if(phoneNumber == null) {
            return null;
        }

        // else, remove all eventual invalid characters
        phoneNumber = removeNonInteger(phoneNumber);

        // first check if already valid number
        if(isValidFullPhoneNumberHelper(phoneNumber)) {
            return phoneNumber;
        }

        try {

            String region = phoneUtil.getRegionCodeForCountryCode(Integer.parseInt(defaultCountryCode));
            PhoneNumber phoneNumberObj = phoneUtil.parse(phoneNumber, region);
            long phonePrefix = phoneNumberObj.getCountryCode();
            long nationalNumber = phoneNumberObj.getNationalNumber();

            return "+"+phonePrefix + nationalNumber;

        } catch (NumberParseException | NumberFormatException e) {
            log.error(e.getMessage(), e);
        }

        // we give up
        return phoneNumber;
    }

    public static String generateFullNorwegianPhoneNumber(String phoneNumber) {
        if(!isValidNorwegianPhoneNumber(phoneNumber)) {
            throw new PhoneNumberParsingException("Not valid norwegian number: "+phoneNumber);
        }
        return generateFullPhoneNumber("+47", phoneNumber);
    }

    public static PhoneNumber parseNumber(String fullPhoneNumber, String defaultCountryCode, String phoneNumber)
        throws PhoneNumberParsingException {

        try {
            // first check if already valid number
            if(isValidFullPhoneNumberHelper(fullPhoneNumber)) {
                return phoneUtil.parse(fullPhoneNumber, UNKNOWN_REGION);
            }
            return parseNumber(defaultCountryCode, phoneNumber);

        } catch (NumberParseException e) {
            throw new PhoneNumberParsingException(e);
        }
    }

    public static PhoneNumber parseNumber(String countryCode, String phoneNumber) throws PhoneNumberParsingException {

        if(phoneNumber == null) {
            throw new PhoneNumberParsingException("Input phone number is null");
        }

        // else, remove all eventual invalid characters
        phoneNumber = removeNonInteger(phoneNumber);

        try {

            // first check if already valid number
            if(isValidFullPhoneNumberHelper(phoneNumber)) {
                return phoneUtil.parse(phoneNumber, UNKNOWN_REGION);
            }

            String region = phoneUtil.getRegionCodeForCountryCode(Integer.parseInt(countryCode));
            PhoneNumber obj = phoneUtil.parse(phoneNumber, region);
            if (!isValidPhoneNumber(countryCode, phoneNumber)) {
                throw new PhoneNumberParsingException(String.format("Prefix: %s, national: %s are not valid number",
                    countryCode, phoneNumber));
            }
            return obj;

        } catch (NumberParseException | NumberFormatException e) {
            throw new PhoneNumberParsingException(e);
        }
    }

    public static String formatPhoneNumber(PhoneNumber obj) {
        if(obj == null) {
            throw new PhoneNumberParsingException("Obj is null");
        }
        return "+"+obj.getCountryCode() + obj.getNationalNumber();
    }

    public static PhoneNumberHolder formatPhoneNumberHolder(PhoneNumber obj) {
        if(obj == null) {
            throw new PhoneNumberParsingException("Obj is null");
        }
        return new PhoneNumberHolder(obj.getCountryCode(), obj.getNationalNumber(), formatPhoneNumber(obj));
    }

    public static boolean isValidFullPhoneNumberHelper(String fullPhoneNumber) {
        try {
            PhoneNumber phoneNumber = phoneUtil.parse(fullPhoneNumber, UNKNOWN_REGION);
            return phoneUtil.isValidNumber(phoneNumber);
        } catch (NumberParseException e) {
            return false;
        }
    }

    public static boolean isPossibleFullPhoneNumber(String fullPhoneNumber) {
        if (null == fullPhoneNumber) {
            return false;
        }

        String regexp = "((?:[a-z][a-z]+))";
        Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(fullPhoneNumber);

        if (matcher.find()) {
            return false;
        }

        try {
            PhoneNumber phoneNumber = phoneUtil.parse(fullPhoneNumber, UNKNOWN_REGION);
            return phoneUtil.isPossibleNumber(phoneNumber);
        } catch (NumberParseException e) {
            return false;
        }
    }

    public static String normalizePhoneNumber(String phoneNumber) {

        PhoneNumber pNumber;
        try {
            pNumber = phoneUtil.parse(phoneNumber, UNKNOWN_REGION);
        } catch (NumberParseException e) {
            log.debug("bad  number:" + phoneNumber);
            throw new PhoneNumberParsingException("phone number invalid: " + phoneNumber);
        }
        phoneNumber = phoneUtil.format(pNumber, PhoneNumberFormat.E164);

        if (!phoneUtil.isPossibleNumber(pNumber)) {
            throw new PhoneNumberParsingException("phone number invalid: " + phoneNumber);
        }

        return phoneNumber;
    }

    public static String replaceInternationalCallingPrefixWithPlus(String phoneNumber) {
        String changedPhoneNumber = phoneNumber;
        if (changedPhoneNumber != null && changedPhoneNumber.startsWith("00")) {
            changedPhoneNumber = "+" + phoneNumber.substring(2);
        }
        return changedPhoneNumber;
    }

    public static boolean hasCountryCode(String phoneNumber) {
        String changedPhoneNumber = replaceInternationalCallingPrefixWithPlus(phoneNumber);
        try {
            getCountryCodeFromFullPhoneNumber(changedPhoneNumber);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static String appendCountryCodeIfMissingAndNormalize(String phoneNumber, String countryCode) {

        if(phoneNumber != null && phoneNumber.startsWith("00")) {
            phoneNumber = "+"+phoneNumber.substring(2);
        }

        String countryCodeNum;
        String region = null;
        if(countryCode != null && !countryCode.isEmpty()) {
            countryCodeNum = countryCode.replaceAll(JUST_NUMBERS, "");
            region = phoneUtil.getRegionCodeForCountryCode(Integer.parseInt(countryCodeNum));
        }

        PhoneNumber pNumber;
        try {
            pNumber = phoneUtil.parse(phoneNumber, region);
        } catch (NumberParseException e) {
            log.debug("bad  region: " + region + ",  or number:" + phoneNumber);
            throw new PhoneNumberParsingException("phone number invalid: " + phoneNumber);
        }

        if (!phoneUtil.isPossibleNumber(pNumber)) {
            throw new PhoneNumberParsingException("phone number invalid: " + phoneNumber);
        }

        phoneNumber = phoneUtil.format(pNumber, PhoneNumberFormat.E164);
        return phoneNumber;
    }

    public static String getPhoneWithoutCountryCode(String phoneNumber, String countryCode) {
        String countryCodeNum = countryCode.replaceAll(JUST_NUMBERS, "");
        String region = phoneUtil.getRegionCodeForCountryCode(Integer.parseInt(countryCodeNum));
        PhoneNumber pNumber = null;
        try {
            pNumber = phoneUtil.parse(phoneNumber, region);
        } catch (NumberParseException e) {
            throw new PhoneNumberParsingException("phone number error: " + phoneNumber);
        }
        // countryCode = countryCode.replaceAll("\\+", "\\\\+"); - what is it?
        return pNumber.isItalianLeadingZero() ? "0" + pNumber.getNationalNumber() : "" + pNumber.getNationalNumber();
    }


    public static String removeNationalLeadingZero(String phoneNumber) {
        //
        if(phoneNumber == null || phoneNumber.isEmpty()) {
            return phoneNumber;
        }

        try{
            // if normalization succeeds, its a perfect valid number with country code, lets just normalize it
            return normalizePhoneNumber(phoneNumber);

        }catch (PhoneNumberParsingException e) {
            // else its probably without country code, lets check if it starts with leading zero
            if(phoneNumber.startsWith("00")) {
                return phoneNumber.substring(2);
            }
            if(phoneNumber.startsWith("0")) {
                return phoneNumber.substring(1);
            }
            // else just return original string
            return phoneNumber;
        }
    }

    /*
     * Country code is WITHOUT leading "+" or "00". In phone strings no spaces are allowed.
     * In other words phone numbers consist only from digits, as first digit could not be "0".
     */
    public static String formatPhoneNumber(String countryCode, String national) {
        if (countryCode == null || national == null || national.equals("")) {
            return "";
        } else {
            return getCleanCountryCode(countryCode) + getCleanPhoneNumber(national);
        }
    }

    private static String getCleanCountryCode(String countryCode) {
        String phoneCode = countryCode.replace(" ", "");
        Pattern pattern = Pattern.compile("(\\+|00)(\\d+)");
        Matcher matcher = pattern.matcher(phoneCode);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return "";
    }

    private static String getCleanPhoneNumber(String phoneNumber) {
        String number = phoneNumber.replace(" ", "");
        if (number.startsWith("0")) {
            number = number.substring(1);
        }
        return number;
    }

    /*
     * Will parse the given phone number.
     *
     * If number starts with null, strip it away. If no "null" prefix and number is not a valid phone number,
     * return null country code and the phone number with what every value the phoneNumber parameters has.
     */
    public static PhoneNumberHolder parsePhoneNumberWhichAcceptNonNumbers(String phoneNumber)
        throws PhoneNumberParsingException {
        String countryCode;
        String shortPhoneNumber;
        try {
            countryCode = getCountryCodeWithPlusSignFromFullPhoneNumber(phoneNumber);
            shortPhoneNumber = getPhoneNumberWithoutCountryCodeFromFullPhoneNumber(phoneNumber);

            return new PhoneNumberHolder(countryCode, shortPhoneNumber);
        } catch (PhoneNumberParsingException e) {
            if (phoneNumber.trim().equalsIgnoreCase(EMPTY_COUNTRY_CODE)) {
                return new PhoneNumberHolder(null, "");
            }
            if (phoneNumber.startsWith(EMPTY_COUNTRY_CODE)) {
                shortPhoneNumber = phoneNumber.split(EMPTY_COUNTRY_CODE)[1];
                return new PhoneNumberHolder(null, shortPhoneNumber);
            }
            else {
                return new PhoneNumberHolder(null, phoneNumber);
            }
        }
    }

    /**
     * Returns a nicely printed string representing the list give as input
     * @param numbers a list of numbers
     * @return nicely printed string like: +4745037118, +90630185
     */
    public static String prettyPrintNumbers(List<String> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            return "";
        }

        StringBuilder str = new StringBuilder();
        for (int i = 0; i < numbers.size(); i++) {
            str.append(numbers.get(i));
            if (i != numbers.size() - 1) {
                str.append(", ");
            }
        }
        return str.toString();
    }

    /*
     * Replace all non int characters with "" except for +
     */
    public static String removeNonInteger(String myStr) {
        if (myStr == null) {
            return null;
        }
        return myStr.replaceAll( "[^+0-9]", "" );
    }

    public static Long removeAllNonNumeric(String myStr) {
        String s = myStr.replaceAll( "[^\\d]", "" );
        if (s.trim().isEmpty()) {
            return null;
        }

        try {
            return Long.valueOf(s);
        } catch (NumberFormatException e) {
            log.error(e.getMessage());
            return null;
        }
    }
}
