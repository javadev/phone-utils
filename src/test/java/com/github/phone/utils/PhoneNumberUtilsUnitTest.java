package com.github.phone.utils;

import com.github.phone.utils.PhoneNumberUtils;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.Phonenumber;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.github.phone.utils.PhoneNumberUtils.appendCountryCodeIfMissingAndNormalize;
import static com.github.phone.utils.PhoneNumberUtils.formatPhoneNumberHolder;
import static com.github.phone.utils.PhoneNumberUtils.isPossibleFullPhoneNumber;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class PhoneNumberUtilsUnitTest {

    //list of valid phone numbers for test. Dash used to separate prefix and number.
    private List<String> phones = Arrays.asList( "+47-92610885" , "+46-15007500",
        "+47-92610886", "+380-672341136", "+1-7033196366", "+1-9173456789", "+47-45037118", "+44-7511758131",
        "+44-7511758131", "+44-7956185515", "+39-06111122221");

    private List<String> specialPhones = Arrays.asList( "+39-055555555" , "+39-01111111",
        "+47-08888888","+44-07956185515");

    @Test
    public void shouldTellTheNumberHasTheCountryCode() {
        for (String number : phones) {
            number = number.replaceAll("-", "");
            boolean hasCountryCode = PhoneNumberUtils.hasCountryCode(number);
            assertThat(hasCountryCode, is(true));
        }

        for (String number : specialPhones) {
            number = number.replaceAll("-", "");
            boolean hasCountryCode = PhoneNumberUtils.hasCountryCode(number);
            assertThat(hasCountryCode, is(true));
        }

        assertFalse(PhoneNumberUtils.hasCountryCode(""));
        assertFalse(PhoneNumberUtils.hasCountryCode(null));

    }

    @Test
    public void shouldTellTheNumberWithLeadingZeroDoesNotHaveTheCountryCode() {
        boolean hasCountryCode = PhoneNumberUtils.hasCountryCode("0672341136");
        assertThat(hasCountryCode, is(false));
    }

    @Test
    public void shouldTellTheNumberDoesNotHaveTheCountryCode() {
        for (String number : phones) {
            String phone = number.substring(number.indexOf('-')+1);
            boolean hasCountryCode = PhoneNumberUtils.hasCountryCode(phone);
            assertThat(hasCountryCode, is(false));
        }

        for (String number : specialPhones) {
            String phone = number.substring(number.indexOf('-')+1);
            try {
                phone = PhoneNumberUtils.normalizePhoneNumber(phone);
                fail();
            } catch (Exception ex){
                //ex.printStackTrace();
            }
            boolean hasCountryCode = PhoneNumberUtils.hasCountryCode(phone);
            assertThat(hasCountryCode, is(false));
        }

    }

    @Test
    public void getPhoneNumberObjFromFullPhoneNumber() {

        String fullPhoneNumber = "+4745037118";

        Phonenumber.PhoneNumber phoneNumber = PhoneNumberUtils.getPhoneNumberObjFromFullPhoneNumber(fullPhoneNumber);
        assertEquals(47, phoneNumber.getCountryCode());
        assertEquals(45037118L, phoneNumber.getNationalNumber());
        assertTrue(phoneNumber.hasCountryCode());
    }

    @Test(expected = PhoneNumberParsingException.class)
    public void getPhoneNumberObjFromFullPhoneNumberButNumberNotFull() {

        String fullPhoneNumber = "45037118";
        PhoneNumberUtils.getPhoneNumberObjFromFullPhoneNumber(fullPhoneNumber);
    }

    @Test
    public void getPhoneNumberObjFromFullPhoneNumberAddPlusPrefixIfNotExist() {
        {
            String fullPhoneNumber = "4745037118";
            try{
                PhoneNumberUtils.getPhoneNumberObjFromFullPhoneNumberAddPlusPrefixIfNotExist(fullPhoneNumber);
                fail("should fail");
            }catch(PhoneNumberParsingException e){
                assertNotNull(e);
            }
        }
        {
            String fullPhoneNumber = "+4745037118";
            Phonenumber.PhoneNumber phoneNumber = PhoneNumberUtils.getPhoneNumberObjFromFullPhoneNumberAddPlusPrefixIfNotExist(fullPhoneNumber);
            assertEquals(47, phoneNumber.getCountryCode());
            assertEquals(45037118L, phoneNumber.getNationalNumber());
            assertTrue(phoneNumber.hasCountryCode());
        }

        {
            String fullPhoneNumber = "16507139923";
            Phonenumber.PhoneNumber phoneNumber = PhoneNumberUtils.getPhoneNumberObjFromFullPhoneNumberAddPlusPrefixIfNotExist(fullPhoneNumber);
            assertEquals(1, phoneNumber.getCountryCode());
            assertEquals(6507139923L, phoneNumber.getNationalNumber());
            assertTrue(phoneNumber.hasCountryCode());
        }
        {
            String fullPhoneNumber = "+16507139923";
            Phonenumber.PhoneNumber phoneNumber = PhoneNumberUtils.getPhoneNumberObjFromFullPhoneNumberAddPlusPrefixIfNotExist(fullPhoneNumber);
            assertEquals(1, phoneNumber.getCountryCode());
            assertEquals(6507139923L, phoneNumber.getNationalNumber());
            assertTrue(phoneNumber.hasCountryCode());
        }
    }

    @Test
    public void getNationalNumber() {

        {
            String fullPhoneNumber = "6507139923";
            long nationalNumber = PhoneNumberUtils.getNationalNumber(fullPhoneNumber);
            assertEquals(6507139923L, nationalNumber);
        }
        {
            String fullPhoneNumber = "4745037118";
            long nationalNumber = PhoneNumberUtils.getNationalNumber(fullPhoneNumber);
            assertEquals(4745037118L, nationalNumber);
        }
        {
            String fullPhoneNumber = "+4745037118";
            long nationalNumber = PhoneNumberUtils.getNationalNumber(fullPhoneNumber);
            assertEquals(45037118L, nationalNumber);
        }
        {
            String fullPhoneNumber = "16507139923";
            long nationalNumber = PhoneNumberUtils.getNationalNumber(fullPhoneNumber);
            assertEquals(6507139923L, nationalNumber);
        }
        {
            String fullPhoneNumber = "+16507139923";
            long nationalNumber = PhoneNumberUtils.getNationalNumber(fullPhoneNumber);
            assertEquals(6507139923L, nationalNumber);
        }
        {
            String fullPhoneNumber = "01(650)-713(9923)";
            long nationalNumber = PhoneNumberUtils.getNationalNumber(fullPhoneNumber);
            assertEquals(16507139923L, nationalNumber);
        }
        {
            Long nationalNumber = PhoneNumberUtils.getNationalNumber("Per Vervik");
            assertNull(nationalNumber);
        }
        {
            assertNull(PhoneNumberUtils.getNationalNumber(null));
            assertNull(PhoneNumberUtils.getNationalNumber(""));
            assertNull(PhoneNumberUtils.getNationalNumber("  "));
        }
    }

    @Test
    public void areNationalNumbersSame() {

        assertTrue(PhoneNumberUtils.areNationalNumbersSame("16507139923", "+16507139923"));
        assertTrue(PhoneNumberUtils.areNationalNumbersSame("+4745037118", "45037118"));
        assertFalse(PhoneNumberUtils.areNationalNumbersSame("4745037118", "45037118"));
        assertTrue(PhoneNumberUtils.areNationalNumbersSame("+4745037118", "+4745037118"));
        assertFalse(PhoneNumberUtils.areNationalNumbersSame("4745037118", "90630185"));

        assertTrue(PhoneNumberUtils.areNationalNumbersSame("6507139923", "+16507139923"));
        assertTrue(PhoneNumberUtils.areNationalNumbersSame("+4745037118", "+4745037118"));
        assertFalse(PhoneNumberUtils.areNationalNumbersSame(null, null));
        assertFalse(PhoneNumberUtils.areNationalNumbersSame("", ""));
        assertFalse(PhoneNumberUtils.areNationalNumbersSame(null, ""));
        assertFalse(PhoneNumberUtils.areNationalNumbersSame("", null));

        assertFalse(PhoneNumberUtils.areNationalNumbersSame("Check Balance", "16508393890"));
        assertFalse(PhoneNumberUtils.areNationalNumbersSame("16508393890", "Check Balance"));
        assertTrue(PhoneNumberUtils.areNationalNumbersSame("+380968340152", "+380968340152"));

        // assertTrue(com.bipper.phone.utils.PhoneNumberUtils.areNationalNumbersSame("+*$(),#p", "+*$(),#p"));
    }

    @Test(expected = PhoneNumberParsingException.class)
    public void getPhoneNumberObjFromFullPhoneNumberAddPlusPrefixIfNotExistButPhoneNumberIsWithoutCountryCode() {

        String fullPhoneNumber = "45037118";
        PhoneNumberUtils.getPhoneNumberObjFromFullPhoneNumberAddPlusPrefixIfNotExist(fullPhoneNumber);
    }

    @Test
    public void shouldAppendTheCountryCodeIfMissing() {
        for (String number : phones) {
            shouldAppendTheCountryCodeIfMissingHelper(number);
        }

        for (String number : specialPhones) {
            shouldAppendTheCountryCodeIfMissingHelper(number);
        }
    }

    @Test
    public void ronk() {
        shouldAppendTheCountryCodeIfMissingHelper("+47-08888888");
    }

    private void shouldAppendTheCountryCodeIfMissingHelper(String number){
        String prefix = number.substring(0, number.indexOf('-'));
        String phone = number.substring(number.indexOf('-')+1);
        String phoneNumber = appendCountryCodeIfMissingAndNormalize(phone, prefix);
        assertNotNull(phoneNumber);
    }

    @Test(expected = PhoneNumberParsingException.class)
    public void appendCountryCodeIfMissingAndNormalizeFail1() {
        appendCountryCodeIfMissingAndNormalize("Thomas Ukraine", "+47");
    }

    @Test(expected = PhoneNumberParsingException.class)
    public void appendCountryCodeIfMissingAndNormalizeFail2() {
        appendCountryCodeIfMissingAndNormalize("636977529", "+47");
    }

    @Test(expected = PhoneNumberParsingException.class)
    public void appendCountryCodeIfMissingAndNormalize_null() {
        appendCountryCodeIfMissingAndNormalize(null, null);
    }

    @Test(expected = PhoneNumberParsingException.class)
    public void appendCountryCodeIfMissingAndNormalize_empty() {
        appendCountryCodeIfMissingAndNormalize("", "");
    }

    @Test
    public void appendCountryCodeIfMissingAndNormalize_norwegianNumberWithDouble00AndSuggestAmericanCountryCode() {
        assertEquals("+4790022909", appendCountryCodeIfMissingAndNormalize("004790022909", "1"));
    }

    @Test
    public void appendCountryCodeIfMissingAndNormalize_norwegianNumberWithDouble00AndSuggestNorwegianCountryCode() {
        assertEquals("+4790022909", appendCountryCodeIfMissingAndNormalize("004790022909", "+47"));
    }

    @Test
    public void appendCountryCodeIfMissingAndNormalizeAmericanNumberWithoutPlusPrefix() {
        Phonenumber.PhoneNumber phone =
            PhoneNumberUtils.getPhoneNumberObjFromFullPhoneNumberAddPlusPrefixIfNotExist("16504301634");
        assertEquals("+16504301634", "+" + phone.getCountryCode() + phone.getNationalNumber());
    }

    @Test
    public void appendCountryCodeIfMissingAndNormalizeWithNorwegianNumbers() {
        assertEquals("+4745037118", appendCountryCodeIfMissingAndNormalize("45037118", "+47"));
        assertEquals("+380636977529", appendCountryCodeIfMissingAndNormalize("636977529", "+380"));
    }

    @Test
    public void isItalianOrUnknownNumber() throws NumberParseException {
        assertTrue(PhoneNumberUtils.isItalianOrUnknownNumber("+39-055555555"));
        assertTrue(PhoneNumberUtils.isItalianOrUnknownNumber("+39-01111111"));
        assertTrue(PhoneNumberUtils.isItalianOrUnknownNumber("+47-08888888"));
        assertFalse(PhoneNumberUtils.isItalianOrUnknownNumber("+44-07956185515"));
        assertTrue(PhoneNumberUtils.isItalianOrUnknownNumber("055555555"));
    }

    @Test
    public void getNationalNumberFromFullPhoneNumber() {

        for (String number : phones) {
            getNationalNumberFromFullPhoneNumberHelper(number);
        }

        for (String number : specialPhones) {
            getNationalNumberFromFullPhoneNumberHelper(number);
        }
    }

    private void getNationalNumberFromFullPhoneNumberHelper(String number){
        String prefix = number.substring(0, number.indexOf('-'));
        String phone = number.substring(number.indexOf('-')+1);

        Phonenumber.PhoneNumber phoneNumber = PhoneNumberUtils.getPhoneNumberObjFromFullPhoneNumber(number);

        String nationalNumber = PhoneNumberUtils.getPhoneNumberWithoutCountryCodeFromFullPhoneNumber(number);
        if(PhoneNumberUtils.isItalianOrUnknownNumber(number)){
            assertEquals(phone, nationalNumber);
        }
        else{
            assertEquals(String.valueOf(phoneNumber.getNationalNumber()), nationalNumber);
        }
    }

    @Test
    public void shouldGetPhoneNumberWithoutCountryCode() {
        for (String number : phones) {
            String prefix = number.substring(0, number.indexOf('-'));
            String phone = number.substring(number.indexOf('-')+1);
            String phoneNumber = PhoneNumberUtils.getPhoneWithoutCountryCode(number, prefix);
            assertThat(phoneNumber, is(phone));
        }

        for (String number : specialPhones) {
            String prefix = number.substring(0, number.indexOf('-'));
            String phone = number.substring(number.indexOf('-')+1);
            String phoneNumber = PhoneNumberUtils.getPhoneWithoutCountryCode(number, prefix);
            assertNotNull(phoneNumber);
        }
    }

    @Test
    public void isValidFullPhoneNumberHelper_checkLandLineNumbers() {
        assertTrue(PhoneNumberUtils.isValidFullPhoneNumberHelper("+18005555555"));
    }

    @Test
    public void testPossibleAndValidNumbers() {

        assertTrue(PhoneNumberUtils.isValidFullPhoneNumberHelper("+4645454545"));
        assertTrue(PhoneNumberUtils.isValidFullPhoneNumberHelper("+4745454545"));

        {
            assertTrue(isPossibleFullPhoneNumber("+15555566666"));
            assertFalse(PhoneNumberUtils.isValidFullPhoneNumberHelper("+15555566666"));
            assertEquals("+15555566666", appendCountryCodeIfMissingAndNormalize("+15555566666", "+47"));
            assertEquals("+15555566666", appendCountryCodeIfMissingAndNormalize("+15555566666", null));
            assertEquals("+15555566666", appendCountryCodeIfMissingAndNormalize("+15555566666", ""));
        }

        assertTrue(isPossibleFullPhoneNumber("+380666113577"));
        assertFalse(isPossibleFullPhoneNumber("-2"));

        {
            assertFalse(isPossibleFullPhoneNumber("+475555566666"));
            assertFalse(PhoneNumberUtils.isValidFullPhoneNumberHelper("+475555566666"));
            try{
                appendCountryCodeIfMissingAndNormalize("+475555566666", "+47");
                fail("should fail");
            } catch(PhoneNumberParsingException e){
                // should come here
            }
        }

        {
            try{
                appendCountryCodeIfMissingAndNormalize("*/()#/", "+47");
                fail("should fail");
            } catch(PhoneNumberParsingException e){
                // should come here
            }
        }

        {
            assertFalse(isPossibleFullPhoneNumber("45037118"));
            assertFalse(PhoneNumberUtils.isValidFullPhoneNumberHelper("45037118"));
            assertEquals("+4745037118", appendCountryCodeIfMissingAndNormalize("45037118", "+47"));
            try{
                appendCountryCodeIfMissingAndNormalize("45037118", null);
                fail("Should fail");
            } catch(PhoneNumberParsingException e){
                // should come here
            }
        }
    }

    @Test
    public void testInvalidAmericanNumber() {
        boolean isValidPhoneNumber = isPossibleFullPhoneNumber("+11323456716");
        assertTrue(isValidPhoneNumber);
    }

    @Test
    public void testValidBritishNumber() {
        boolean isValidPhoneNumber = isPossibleFullPhoneNumber("+4407956185515");
        assertTrue(isValidPhoneNumber);
    }

    @Test
    public void testInvalidUkrainianNumber() {
        boolean isValidPhoneNumber = isPossibleFullPhoneNumber("+380555555");
        assertTrue(isValidPhoneNumber);

        String normalized = PhoneNumberUtils.normalizePhoneNumber("+380555555");
        assertEquals("+380555555", normalized);

        assertFalse(PhoneNumberUtils.isValidFullPhoneNumberHelper("+380555555"));
    }

    @Test
    public void testIvoCoastnNumber() {
        boolean isValidPhoneNumber = isPossibleFullPhoneNumber("+22507015299");
        assertTrue(isValidPhoneNumber);

        String normalized = PhoneNumberUtils.normalizePhoneNumber("++22507015299");
        assertEquals("+22507015299", normalized);

        assertFalse(PhoneNumberUtils.isValidFullPhoneNumberHelper("+2257015299"));
    }

    @Test
    public void testFullIvory() {
        String fullIvoryCoastNumber = "+22507015299";
        // the test
        String national = PhoneNumberUtils.getPhoneNumberWithoutCountryCodeFromFullPhoneNumber(fullIvoryCoastNumber);
        assertEquals("07015299", national);
        // test two
        String normalized = PhoneNumberUtils.normalizePhoneNumber(fullIvoryCoastNumber);
        assertEquals(fullIvoryCoastNumber, normalized);
    }

    @Test
    public void testValidAustralian() {
        String fullAustralianNumber = "+610474459334";
        boolean isValid = PhoneNumberUtils.isValidFullPhoneNumberHelper(fullAustralianNumber);
        assertTrue(isValid);
    }

    @Test
    public void testValidUkrainianNumber() {
        boolean isValidPhoneNumber = isPossibleFullPhoneNumber("+380972609090");
        assertTrue(isValidPhoneNumber);
    }

    @Test
    public void testInvalidUkrainianNumber1() {
        boolean isValidPhoneNumber = isPossibleFullPhoneNumber("+380ffffff");
        assertFalse(isValidPhoneNumber);
    }

    @Test
    public void testInvalidUkrainianNumber2() {
        boolean isValidPhoneNumber = isPossibleFullPhoneNumber("+380FFFFFF");
        assertFalse(isValidPhoneNumber);
    }

    @Test
    public void testInvalidNullNumber() {
        boolean isValidPhoneNumber = isPossibleFullPhoneNumber(null);
        assertFalse(isValidPhoneNumber);
    }

    /**
     * see BPPR-5742
     */
    @Test
    public void testValidCotedIvoireNumber() {
        boolean isValidPhoneNumber = isPossibleFullPhoneNumber("+22507015299");
        assertTrue(isValidPhoneNumber);
    }

    /**
     * see BPPR-5742
     */
    @Test
    public void testInvalidCotedIvoireNumber() {
        boolean isValidPhoneNumber = isPossibleFullPhoneNumber("+2257015299");
        assertFalse(isValidPhoneNumber);
    }

    @Test
    public void shouldHandlePrefixCorrect() {
        for (String number : phones) {
            number = number.replaceAll("-", "");
            String normalizedPhoneNumber = PhoneNumberUtils.normalizePhoneNumber(number);
            assertThat(normalizedPhoneNumber, is(number));
        }

        for (String number : specialPhones) {
            number = number.replaceAll("-", "");
            String normalizedPhoneNumber = PhoneNumberUtils.normalizePhoneNumber(number);
            assertNotNull(normalizedPhoneNumber);
        }

    }

    @Test
    public void shouldNormalizeBritishNumberWithLeadingZero() {
        String normalizedPhone = PhoneNumberUtils.normalizePhoneNumber("+4407956185515");
        assertEquals("+447956185515", normalizedPhone);
    }

    @Test(expected = PhoneNumberParsingException.class)
    public void cantNormalizeNumberWithoutCountryPrefix() {
        PhoneNumberUtils.normalizePhoneNumber("45037118");
    }

    @Test(expected = PhoneNumberParsingException.class)
    public void shouldNotNormalizeBritishNumberWithoutCountryCodeButWithLeadingZero() {
        PhoneNumberUtils.normalizePhoneNumber("07956185515");
    }

    @Test(expected = PhoneNumberParsingException.class)
    public void shouldNotNormalizeBritishNumberWithoutCountryCode() {
        PhoneNumberUtils.normalizePhoneNumber("7956185515");
    }

    @Test
    public void shouldHandlePrefixZeroCorrectAsWell() {
        String phoneCountryCode = "+44";
        String phoneNumberWithoutCountryCode = "07956185515";
        String phoneNumber = PhoneNumberUtils.getPhoneNumberWithoutCountryCodeFromFullPhoneNumber(phoneCountryCode
                + phoneNumberWithoutCountryCode);
        assertThat(phoneNumber, is("7956185515"));
    }

    @Test
    public void shouldHandelBritishNumberWithLeadingZero() {
        String phoneCountryCode = "+44";
        String phoneNumberWithoutCountryCode = "931732508";
        String phoneNumber = PhoneNumberUtils.getPhoneNumberWithoutCountryCodeFromFullPhoneNumber(phoneCountryCode
                + phoneNumberWithoutCountryCode);
        assertThat(phoneNumber, is("931732508"));
    }

    @Test
    public void shouldHandelNorwegianNumberWithLeadingZero() {
        String phoneCountryCode = "+47";
        String phoneNumberWithoutCountryCode = "08888888";
        String phoneNumber = PhoneNumberUtils.getPhoneNumberWithoutCountryCodeFromFullPhoneNumber(phoneCountryCode
                + phoneNumberWithoutCountryCode);
        assertThat(phoneNumber, is("08888888"));
    }

    @Test
    public void shouldHandelUkrainianNumberWithLeadingZero() {
        String phoneCountryCode = "+380";
        String phoneNumberWithoutCountryCode = "0636977529";
        String phoneNumber = PhoneNumberUtils.getPhoneNumberWithoutCountryCodeFromFullPhoneNumber(phoneCountryCode
                + phoneNumberWithoutCountryCode);
        assertThat(phoneNumber, is("636977529"));
    }

    @Test
    public void removeNationalLeadingZeroNull() {
        assertNull(PhoneNumberUtils.removeNationalLeadingZero(null));
    }

    @Test
    public void removeNationalLeadingZeroEmpty() {
        assertTrue(PhoneNumberUtils.removeNationalLeadingZero("").isEmpty());
    }

    @Test
    public void removeNationalLeadingBritishNumberWithCountryPrefixAndLeadingZero() {
        assertEquals("+447956185515", PhoneNumberUtils.removeNationalLeadingZero("+4407956185515"));
    }

    @Test
    public void removeNationalLeadingBritishNumberWithCountryPrefixAndWithoutLeadingZero() {
        assertEquals("+447956185515", PhoneNumberUtils.removeNationalLeadingZero("+447956185515"));
    }

    @Test
    public void removeNationalLeadingBritishNumberWithoutCountryPrefixAndLeadingZero() {
        assertEquals("7956185515", PhoneNumberUtils.removeNationalLeadingZero("07956185515"));
    }

    @Test
    public void removeNationalLeadingBritishNumberWithoutCountryPrefixAndWithoutLeadingZero() {
        assertEquals("7956185515", PhoneNumberUtils.removeNationalLeadingZero("7956185515"));
    }

    @Test
    public void norwegianNumberWithoutCountryCodeValid() {
        assertFalse(isPossibleFullPhoneNumber("45037118"));
    }

    @Test
    public void getCountryCodeWithPlusSignFromFullPhoneNumber() {
        assertEquals("+47", PhoneNumberUtils.getCountryCodeWithPlusSignFromFullPhoneNumber("+4745037118"));
    }

    @Test(expected = PhoneNumberParsingException.class)
    public void getCountryCodeWithPlusSignFromNotFullPhoneNumber() {
        PhoneNumberUtils.getCountryCodeWithPlusSignFromFullPhoneNumber("45037118");
    }

    @Test
    public void formatPhoneNumber_NorwegianNumber() {
        String formatedNumber = PhoneNumberUtils.formatPhoneNumber("+47", "45037118");
        assertEquals("4745037118", formatedNumber);
    }

    @Test
    public void formatPhoneNumber_BritishNumberWithLeadingZero() {
        String formatedNumber = PhoneNumberUtils.formatPhoneNumber("+44", "07956185515");
        assertEquals("447956185515", formatedNumber);
    }

    @Test
    public void formatPhoneNumber_ukrainian() {
        String formatedNumber = PhoneNumberUtils.formatPhoneNumber("+380", "0636977529");
        assertEquals("380636977529", formatedNumber);
    }

    @Test
    public void formatPhoneNumber_Null() {
        String formatedNumber = PhoneNumberUtils.formatPhoneNumber(null, null);
        assertTrue(formatedNumber.isEmpty());
    }

    @Test
    public void formatPhoneNumber_Empty() {
        String formatedNumber = PhoneNumberUtils.formatPhoneNumber("", "");
        assertTrue(formatedNumber.isEmpty());
    }

    @Test
    public void parsePhoneNumberWhichAcceptNonNumbersStartingOnNullNotValidNumber() {
        PhoneNumberHolder holder = PhoneNumberUtils.parsePhoneNumberWhichAcceptNonNumbers("abcdefghijklmn");
        assertNull(holder.getPrefix());
        assertEquals("abcdefghijklmn", holder.getNational());
    }

    @Test
    public void parsePhoneNumberWhichAcceptNonNumbersStartingOnNullValidNumber() {
        PhoneNumberHolder holder = PhoneNumberUtils.parsePhoneNumberWhichAcceptNonNumbers("+4745037118");
        assertEquals("+47", holder.getPrefix());
        assertEquals("45037118", holder.getNational());
    }

    @Test
    public void parsePhoneNumberWhichAcceptNonNumbersStartingOnNullValidNullPrefix() {
        PhoneNumberHolder holder = PhoneNumberUtils.parsePhoneNumberWhichAcceptNonNumbers("nullBsafe");
        assertNull(holder.getPrefix());
        assertEquals("Bsafe", holder.getNational());
    }

    @Test
    public void parsePhoneNumberWhichAcceptNonNumbersNullString() {
        PhoneNumberHolder holder = PhoneNumberUtils.parsePhoneNumberWhichAcceptNonNumbers("null");
        assertNull(holder.getPrefix());
        assertTrue(holder.getNational().isEmpty());
    }

    @Test
    public void parsePhoneNumberWithoutCountryCode() {

        PhoneNumberHolder holder = PhoneNumberUtils.parsePhoneNumberWhichAcceptNonNumbers("45037118");
        assertNull(holder.getPrefix());
        assertEquals("45037118", holder.getNational());
    }

    @Test
    public void parsePhoneNumberWhichAcceptNonNumbersNullStringWithEndingSpaces() {
        PhoneNumberHolder holder = PhoneNumberUtils.parsePhoneNumberWhichAcceptNonNumbers("null  ");
        assertNull(holder.getPrefix());
        assertTrue(holder.getNational().isEmpty());
    }

    @Test
    public void removeNonIntegerWithNullOrEmptyInput() {
        assertEquals("", PhoneNumberUtils.removeNonInteger("  "));
        assertEquals("", PhoneNumberUtils.removeNonInteger(""));
        assertNull(PhoneNumberUtils.removeNonInteger(null));
    }

    @Test
    public void removeNonInteger() {
        String str = PhoneNumberUtils.removeNonInteger("415-971-6486");
        assertEquals("4159716486", str);
    }

    @Test
    public void removeNonInteger2() {
        String str = PhoneNumberUtils.removeNonInteger("(415)-971-6486");
        assertEquals("4159716486", str);
    }

    @Test
    public void removeNonInteger3() {
        String str = PhoneNumberUtils.removeNonInteger("+1 (415)-971-64 86");
        assertEquals("+14159716486", str);
    }

    @Test
    public void removeNonInteger4() {
        String str = PhoneNumberUtils.removeNonInteger("+47 45037118");
        assertEquals("+4745037118", str);
    }

    @Test
    public void checkValidNumberTestNumber() {
        assertTrue(isPossibleFullPhoneNumber("+1 1111122222"));
    }

    @Test
    public void isValidFullPhoneNumberHelper() {
        String phoneNumber = "+476508393890";
        assertFalse(PhoneNumberUtils.isValidFullPhoneNumberHelper(phoneNumber));
        assertFalse(isPossibleFullPhoneNumber(phoneNumber));

        Phonenumber.PhoneNumber phoneNumber1 = PhoneNumberUtils.getPhoneNumberObjFromFullPhoneNumberAddPlusPrefixIfNotExist(phoneNumber);
        assertNotNull(phoneNumber1);
    }


    @Test(expected = PhoneNumberParsingException.class)
    public void shouldParseAsAmericanConnecticutNumber() {
        String phoneNumber = "2038101279";
        assertFalse(PhoneNumberUtils.isValidFullPhoneNumberHelper(phoneNumber));
        assertFalse(isPossibleFullPhoneNumber(phoneNumber));

        // should throw exception
        PhoneNumberUtils.getPhoneNumberObjFromFullPhoneNumberAddPlusPrefixIfNotExist(phoneNumber);
    }

    @Test(expected = PhoneNumberParsingException.class)
    public void getPhoneNumberObjFromFullPhoneNumberConnecticut() {

        String phoneNumber = "2038101279";
        // the test - should throw exception
        PhoneNumberUtils.getPhoneNumberObjFromFullPhoneNumber(phoneNumber);
    }

    @Test
    public void invalidButPossibleItalianNumberWithLeadingZero() {
        String phoneNumber = "+39055555555";
        assertTrue(PhoneNumberUtils.isValidFullPhoneNumberHelper(phoneNumber));
        assertTrue(isPossibleFullPhoneNumber(phoneNumber));
    }

    @Test
    public void invalidButPossibleNorwegianNumberWithLeadingZero() {
        String phoneNumber = "+4708888888";
        assertFalse(PhoneNumberUtils.isValidFullPhoneNumberHelper(phoneNumber));
        assertTrue(isPossibleFullPhoneNumber(phoneNumber));
    }

    @Test
    public void bothValidAndPossibleBritishNumberWithLeadingZero() {
        String phoneNumber = "+4407956185515";
        assertTrue(PhoneNumberUtils.isValidFullPhoneNumberHelper(phoneNumber));
        assertTrue(isPossibleFullPhoneNumber(phoneNumber));
    }

    @Test
    public void bothValidAndPossibleNorwegianNumber() {
        String phoneNumber = "+4745037118";
        assertTrue(PhoneNumberUtils.isValidFullPhoneNumberHelper(phoneNumber));
        assertTrue(isPossibleFullPhoneNumber(phoneNumber));
    }

    @Test
    public void bothValidAndPossibleUkrainanNumber() {
        String phoneNumber = "+380636977529";
        assertTrue(PhoneNumberUtils.isValidFullPhoneNumberHelper(phoneNumber));
        assertTrue(isPossibleFullPhoneNumber(phoneNumber));
    }

    @Test
    public void neitherValidNorPossibleUkrainanNumberSinceItIsWithoutPlus() {
        String phoneNumber = "380636977529";
        assertFalse(PhoneNumberUtils.isValidFullPhoneNumberHelper(phoneNumber));
        assertFalse(isPossibleFullPhoneNumber(phoneNumber));
    }

    @Test
    public void isPossibleButNotValidUkrainanPhoneNumber() {
        String phoneNumber = "+380555555 ";
        assertFalse(PhoneNumberUtils.isValidFullPhoneNumberHelper(phoneNumber));
        assertTrue(isPossibleFullPhoneNumber(phoneNumber));
    }

    @Test
    public void isPossibleButNotValidUkrainanPhoneNumber2() {
        String phoneNumber = "+14156682 ";
        assertFalse(PhoneNumberUtils.isValidFullPhoneNumberHelper(phoneNumber));
        assertTrue(isPossibleFullPhoneNumber(phoneNumber));
    }

    @Test
    public void normalizePhoneNumberAndAddPlusPrefixIfMissing() {
        assertEquals("+16507139923", PhoneNumberUtils.normalizePhoneNumber("+1 (650) - 713 (9923)"));
    }

    @Test
    public void removeAllNonNumeric() {
        assertEquals(new Long(4745037118l), PhoneNumberUtils.removeAllNonNumeric("+4745037118"));
        assertNull(PhoneNumberUtils.removeAllNonNumeric("6607130507876641740295491524545"));
    }

    @Test
    public void isKhazakstanPhoneNumberPossible() {
        assertTrue(isPossibleFullPhoneNumber("+77012118888"));
    }

    @Test
    public void getCountryCodeFromFullPhoneNumber() {
        assertEquals(1, PhoneNumberUtils.getCountryCodeFromFullPhoneNumber("+13037269121"));
        assertEquals(1, PhoneNumberUtils.getCountryCodeFromFullPhoneNumber("+14022458773"));
        assertEquals(46, PhoneNumberUtils.getCountryCodeFromFullPhoneNumber("+4645037118"));
        assertEquals(47, PhoneNumberUtils.getCountryCodeFromFullPhoneNumber("+4790630185"));
    }

    @Test(expected = PhoneNumberParsingException.class)
    public void getCountryCodeFromFullPhoneNumber_failNotFullPhoneNumber() {
        PhoneNumberUtils.getCountryCodeFromFullPhoneNumber("3037261054");
    }

    @Test(expected = PhoneNumberParsingException.class)
    public void getCountryCodeFromFullPhoneNumber_failMissingPlus() {
        PhoneNumberUtils.getCountryCodeFromFullPhoneNumber("4790630185");
    }

    @Test
    public void generateFullPhoneNumber() {

        assertEquals("+4745454545", PhoneNumberUtils.generateFullPhoneNumber("+47", "45 45 45 45"));
        assertEquals("+4745454545", PhoneNumberUtils.generateFullPhoneNumber("+47", "+47 45 45 45 45"));
        assertEquals("+4745454545", PhoneNumberUtils.generateFullPhoneNumber("+47", "+4745454545"));
        assertEquals("+4645454545", PhoneNumberUtils.generateFullPhoneNumber("+47", "+46 45 45 45 45"));
        assertEquals("+4745454545", PhoneNumberUtils.generateFullPhoneNumber("+47", "4745 45 45 45"));
        assertEquals("+380985777268", PhoneNumberUtils.generateFullPhoneNumber("+47", "+380985777268"));
        // not valid number
        assertEquals("+4545454565565665", PhoneNumberUtils.generateFullPhoneNumber("+47", "+45 45 45 45 65 565665"));

        assertTrue(PhoneNumberUtils.generateFullPhoneNumber("+47", "a lot of scrambed text").isEmpty());
        assertTrue(PhoneNumberUtils.generateFullPhoneNumber("+47", "").isEmpty());
        assertTrue(PhoneNumberUtils.generateFullPhoneNumber("+47", "  ").isEmpty());
        assertTrue(PhoneNumberUtils.generateFullPhoneNumber("", "  ").isEmpty());
        assertNull(PhoneNumberUtils.generateFullPhoneNumber("+47", null));

        assertNull(PhoneNumberUtils.generateFullPhoneNumber(null, null));
    }

    @Test
    public void parseNumber() {

        parseNumberSuccessHelper("+47", "45 45 45 45", 47, 45454545);
        parseNumberSuccessHelper("+47", "+47 45 45 45 45", 47, 45454545);
        parseNumberSuccessHelper("+47", "+4745454545", 47, 45454545);
        parseNumberSuccessHelper("+47", "+46 45 45 45 45", 46, 45454545);
        parseNumberSuccessHelper("+47", "4745 45 45 45", 47, 45454545);

        parseNumberFailureHelper("+47", "+45 45 45 45 65 565665");
        parseNumberFailureHelper("+47", "a lot of scrambed text");
        parseNumberFailureHelper("+47", "");
        parseNumberFailureHelper("+47", "  ");
        parseNumberFailureHelper("", "  ");
        parseNumberFailureHelper("+47", null);
        parseNumberFailureHelper(null, null);
    }

    private void parseNumberSuccessHelper(String defaultPhonePrefix, String inputPhoneNumber,
        int prefix, long national) {
        Phonenumber.PhoneNumber n5 = PhoneNumberUtils.parseNumber(defaultPhonePrefix, inputPhoneNumber);
        assertEquals(prefix, n5.getCountryCode());
        assertEquals(national, n5.getNationalNumber());
    }

    private void parseNumberFailureHelper(String defaultPhonePrefix, String inputPhoneNumber) {
        try {
            PhoneNumberUtils.parseNumber(defaultPhonePrefix, inputPhoneNumber); // not valid number
            fail("Should fail");
        } catch (PhoneNumberParsingException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void isPossiblePhoneNumber() {
        assertTrue(PhoneNumberUtils.isValidPhoneNumber("+47", "45 45 45 45"));
        assertTrue(PhoneNumberUtils.isValidPhoneNumber("+47", "+47 45 45 45 45"));
        assertTrue(PhoneNumberUtils.isValidPhoneNumber("+47", "+4745454545"));
        assertTrue(PhoneNumberUtils.isValidPhoneNumber("+47", "+46 45 45 45 45"));
        assertTrue(PhoneNumberUtils.isValidPhoneNumber("+47", "4745 45 45 45"));
        assertFalse(PhoneNumberUtils.isValidPhoneNumber("+47", "+45 45 45 45 65 565665")); // not valid number 565665
        assertFalse(PhoneNumberUtils.isValidPhoneNumber("+47", "565665"));
        assertFalse(PhoneNumberUtils.isValidPhoneNumber("+47", ""));
        assertFalse(PhoneNumberUtils.isValidPhoneNumber("+47", null));
    }

    @Test
    public void validatePhoneNumbers() {
        List<String> list = new ArrayList<>();
        list.add("45 45 45 45");
        list.add("+47 45 45 45 46");
        list.add(null);
        list.add("");
        list.add("TotallyInvalid");
        list.add("+2365");
        list.add("90630");

        // the test
        List<String> numbers = PhoneNumberUtils.validatePhoneNumbers(list);

        assertEquals(2, numbers.size());
        assertEquals("+4745454545", numbers.get(0));
        assertEquals("+4745454546", numbers.get(1));
    }

    @Test
    public void validatePhoneNumbers_nullAndEmpy() {

        // the test
        {
            List<String> numbers = PhoneNumberUtils.validatePhoneNumbers(null);
            assertTrue(numbers.isEmpty());
        }
        {
            // the test
            List<String> numbers = PhoneNumberUtils.validatePhoneNumbers(new ArrayList<>());
            assertTrue(numbers.isEmpty());
        }
    }

    @Test
    public void prettyPrintNumbers() {
        assertEquals("+4745037118", PhoneNumberUtils.prettyPrintNumbers(createList("+4745037118")));
        assertEquals("+4745037118, +4790630185", PhoneNumberUtils.prettyPrintNumbers(
            createList("+4745037118", "+4790630185")));
        assertEquals("+4745037118, +4790630185, +4745454545", PhoneNumberUtils.prettyPrintNumbers(
            createList("+4745037118", "+4790630185", "+4745454545")));
        assertEquals("", PhoneNumberUtils.prettyPrintNumbers(null));
        assertEquals("", PhoneNumberUtils.prettyPrintNumbers(new ArrayList<>()));
    }

    @Test
    public void formatPhoneNumber() {
        assertEquals("+4745037118", PhoneNumberUtils.formatPhoneNumber(
            PhoneNumberUtils.parseNumber("+47", "45037118")));
        assertEquals("+380636977529", PhoneNumberUtils.formatPhoneNumber(
            PhoneNumberUtils.parseNumber("+380", "0636977529")));
        assertEquals("+380636977529", PhoneNumberUtils.formatPhoneNumber(
            PhoneNumberUtils.parseNumber("380", "0636977529")));

        {
            PhoneNumberHolder holder = formatPhoneNumberHolder(PhoneNumberUtils.parseNumber("+47", "45037118"));
            assertEquals("+47", holder.getPrefix());
            assertEquals("45037118", holder.getNational());
            assertEquals("+4745037118", holder.getPhoneNumber());
        }

        {
            PhoneNumberHolder holder = formatPhoneNumberHolder(PhoneNumberUtils.parseNumber("380", "0636977529"));
            assertEquals("+380", holder.getPrefix());
            assertEquals("636977529", holder.getNational());
            assertEquals("+380636977529", holder.getPhoneNumber());
        }
    }

    @Test
    public void parseNumber_threeParameters() {
        {
            Phonenumber.PhoneNumber number = PhoneNumberUtils.parseNumber("+3800636977529", "+47", "45037118");
            assertEquals(380, number.getCountryCode());
            assertEquals(636977529, number.getNationalNumber());
        }
        {
            Phonenumber.PhoneNumber number = PhoneNumberUtils.parseNumber("+3800636977529", null, null);
            assertEquals(380, number.getCountryCode());
            assertEquals(636977529, number.getNationalNumber());
        }

        {
            Phonenumber.PhoneNumber number = PhoneNumberUtils.parseNumber(null, "+380", "0636977529");
            assertEquals(380, number.getCountryCode());
            assertEquals(636977529, number.getNationalNumber());
        }
    }

    @Test
    public void isThisValidNumber() {
        assertTrue(PhoneNumberUtils.isValidFullPhoneNumberHelper("+40705394226"));
    }

    @Test
    public void testIsValid() {
        assertTrue(PhoneNumberUtils.isValidFullPhoneNumberHelper("+46793470020"));
        assertTrue(PhoneNumberUtils.isValidFullPhoneNumberHelper("+61484128126"));
        assertTrue(PhoneNumberUtils.isValidFullPhoneNumberHelper("+27820403626"));
    }

    @Test
    public void testIsPossible() {
        assertTrue(isPossibleFullPhoneNumber("+46793470020"));
    }

    @Test
    public void normalizeNumber() {
        String normalized = PhoneNumberUtils.appendCountryCodeIfMissingAndNormalize("45 03(7118)", "+47");
        assertEquals("+4745037118", normalized);
    }

    @Test
    public void testValidNumber() {
        assertFalse(PhoneNumberUtils.isValidPhoneNumber("+47", "+4736985214"));
        assertTrue(PhoneNumberUtils.isPossibleFullPhoneNumber("+4736985214"));
        assertTrue(PhoneNumberUtils.isValidPhoneNumber("+47", "45037118"));
    }

    @Test
    public void testHasCountryCode() {
        assertTrue(PhoneNumberUtils.hasCountryCode(47, "+4736985214"));
        assertFalse(PhoneNumberUtils.hasCountryCode(46, "+4736985214"));
    }

    @Test
    public void testHasCountryCode_failNotFullNumber() {
        assertFalse(PhoneNumberUtils.hasCountryCode(47, "45454545"));
    }

    @Test
    public void testHasCountryCode_failNotPossibleNumber() {
        assertFalse(PhoneNumberUtils.hasCountryCode(47, "+478587845454545"));
    }

    @Test
    public void testGenerateFullNorwegianPhoneNumber() {
        assertEquals("+4745037118", PhoneNumberUtils.generateFullNorwegianPhoneNumber("45037118"));
        assertEquals("+4790630185", PhoneNumberUtils.generateFullNorwegianPhoneNumber("906 (30) 185"));
        assertEquals("+4790630185", PhoneNumberUtils.generateFullNorwegianPhoneNumber("+47 906 (30) 185"));
        assertEquals("+4790630185", PhoneNumberUtils.generateFullNorwegianPhoneNumber("+4790630185"));
    }

    @Test(expected = PhoneNumberParsingException.class)
    public void testGenerateFullNorwegianPhoneNumber_tooShort() {
        PhoneNumberUtils.generateFullNorwegianPhoneNumber("4503711");
    }

    @Test(expected = PhoneNumberParsingException.class)
    public void testGenerateFullNorwegianPhoneNumber_notValidPrefix() {
        PhoneNumberUtils.generateFullNorwegianPhoneNumber("65037118");
    }

    @Test(expected = PhoneNumberParsingException.class)
    public void testGenerateFullNorwegianPhoneNumber_notValidPrefix2() {
        PhoneNumberUtils.generateFullNorwegianPhoneNumber("80630185");
    }

    @Test
    public void testIsValidNorwegianPhoneNumber() {
        assertTrue(PhoneNumberUtils.isValidNorwegianPhoneNumber("45037118"));
        assertTrue(PhoneNumberUtils.isValidNorwegianPhoneNumber("906 (30) 185"));
        assertTrue(PhoneNumberUtils.isValidNorwegianPhoneNumber("+47 906 (30) 185"));
        assertTrue(PhoneNumberUtils.isValidNorwegianPhoneNumber("+4790630185"));
        assertTrue(PhoneNumberUtils.isValidNorwegianPhoneNumber("004741499915"));


        assertFalse(PhoneNumberUtils.isValidNorwegianPhoneNumber("800185"));
        assertFalse(PhoneNumberUtils.isValidNorwegianPhoneNumber("80630185"));
        assertFalse(PhoneNumberUtils.isValidNorwegianPhoneNumber("+47 80630185"));
    }

    @Test
    public void testRonk() {
        assertFalse(PhoneNumberUtils.isValidNorwegianPhoneNumber("4"));
    }

    private static List<String> createList(String...strings) {
        List<String> list = new ArrayList<>();
        Collections.addAll(list, strings);
        return list;
    }
}
